package com.jiewu.driftbottle.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiewu.driftbottle.common.BaseResponse;
import com.jiewu.driftbottle.common.ErrorCode;
import com.jiewu.driftbottle.common.ResultUtils;
import com.jiewu.driftbottle.exception.BusinessException;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.model.request.UserLoginRequest;
import com.jiewu.driftbottle.model.request.UserRegisterRequest;
import com.jiewu.driftbottle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jiewu.driftbottle.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUsers() {
        String redisKey = "langshi:user:recommend";
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 1. 缓存存在，直接返回
        List<User> cachedUsers = (List<User>) valueOperations.get(redisKey);
        if (cachedUsers != null && !cachedUsers.isEmpty()) {
            return ResultUtils.success(cachedUsers);
        }

        // 2. 缓存不存在，走随机ID查询
        // 2.1 获取最大ID
        Long maxId = userService.getBaseMapper()
                .selectObjs(new QueryWrapper<User>().select("MAX(id)"))
                .stream()
                .map(o -> (Long) o)
                .findFirst()
                .orElse(0L);

        if (maxId == 0) {
            return ResultUtils.success(Collections.emptyList());
        }

        Random random = new Random();
        Set<Long> randomIds = new HashSet<>();
        while (randomIds.size() < 10) {
            long id = 1 + ThreadLocalRandom.current().nextLong(maxId);
            randomIds.add(id);
        }

        // 2.2 查询用户
        List<User> users = userService.list(
                new QueryWrapper<User>().in("id", randomIds)
        );

        // 3. 查询结果写入缓存（兜底60秒）
        try {
            valueOperations.set(redisKey, users, 60L, TimeUnit.SECONDS);
        } catch (Exception e) {
            // log.error("redis set key error", e);
        }

        return ResultUtils.success(users);
    }


    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (tagNameList == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, Double rate, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
//        if (type == 0) {
//            response = userService.bestMatchUsers(num, loginUser);
//            return ResultUtils.success(userService.partMatchUsers(num, rate, loginUser));
//            return ResultUtils.success(userService.partMatchUsers(pageSize, pageNum, rate, loginUser));
//        } else if (type == 1) {

//            if (size == null) {
//                throw new BusinessException(ErrorCode.PARAMS_ERROR);
//            }
//            response = userService.partMatchUsers(num, size, loginUser);

            return ResultUtils.success(userService.partMatchUsers(num,rate, loginUser));
//        }else {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
    }

}
