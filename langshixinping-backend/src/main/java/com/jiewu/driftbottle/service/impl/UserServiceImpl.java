package com.jiewu.driftbottle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jiewu.driftbottle.common.ErrorCode;
import com.jiewu.driftbottle.exception.BusinessException;
import com.jiewu.driftbottle.mapper.UserMapper;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.service.UserService;
import com.jiewu.driftbottle.utlis.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jiewu.driftbottle.contant.UserConstant.ADMIN_ROLE;
import static com.jiewu.driftbottle.contant.UserConstant.USER_LOGIN_STATE;
import static com.jiewu.driftbottle.utlis.AlgorithmUtils.cosineSimilarity;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "jiewu";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 获取有效登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user == null || user.getUserRole() != ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long id = user.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (!isAdmin(loginUser) && id != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User currentUser = this.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);

        if (user.getTags() != null) {
            wrapper.set("tags", user.getTags());
        } else if (user.getUsername() != null) {
            wrapper.set("username", user.getUsername());
        } else if (user.getProfile() != null) {
            wrapper.set("profile", user.getProfile());
        } else if (user.getGender() != null) {
            wrapper.set("gender", user.getGender());
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有要更新的内容");
        }

        return userMapper.update(null, wrapper);
    }


/*
    public int updateUser(User user, User loginUser) {
        long id = user.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 可补充校验, 如果用户没有传任何需要更新的值, 就直接报错, 不执行update语句

        if (!isAdmin(loginUser) && id != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User currentUser = this.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }
*/

    /**
     * 获取默认匹配的用户 (未登录)
     *
     * @param num
     * @param loginUser
     * @return
     */
    // 线程池：CPU 密集型推荐使用 CPU 核心数 + 1
    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    @Override
    public List<User> defaultMatchUsers(long num, double rate, User loginUser) {
        // 1. 查询所有有标签的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);

        Gson gson = new Gson();
        // 2. 当前用户标签转 List
        List<String> loginTagList = gson.fromJson(loginUser.getTags(), new TypeToken<List<String>>() {}.getType());

        // 3. 线程池并行计算
        int threadCount = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<Pair<User, Double>>>> futures = new ArrayList<>();

        int batchSize = 2000; // 每批处理用户数，调大减少任务切分开销
        for (int i = 0; i < userList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, userList.size());
            List<User> subList = userList.subList(start, end);

            futures.add(executor.submit(() -> {
                List<Pair<User, Double>> localList = new ArrayList<>();
                for (User user : subList) {
                    if (StringUtils.isBlank(user.getTags()) || user.getId()==loginUser.getId()) {
                        continue;
                    }

                    List<String> userTagList = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());

                    // 使用你写的 cosineSimilarity
                    double similarity = cosineSimilarity(loginTagList, userTagList);
                    if (similarity > rate) {
                        localList.add(new ImmutablePair<>(user, similarity));
                    }
                }
                return localList;
            }));
        }

        // 4. 汇总结果
        List<Pair<User, Double>> pairList = new ArrayList<>();
        try {
            for (Future<List<Pair<User, Double>>> future : futures) {
                pairList.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("并行计算出错", e);
        } finally {
            executor.shutdown();
        }

        // 5. 按相似度排序（比随机 shuffle 更合理）
        pairList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 6. 取前 num 个
        List<Pair<User, Double>> topUserPairList = pairList.stream()
                .limit(num)
                .collect(Collectors.toList());

        // 7. 查完整用户信息
        List<Long> userIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 计算两个用户标签的相似度
     */
    private double calculateSimilarity(String tags1, String tags2) {
        // 如果 User.tags 存储格式是 JSON 或者逗号分隔字符串，需要先转成 List
        List<String> list1 = Arrays.asList(tags1.split(","));
        List<String> list2 = Arrays.asList(tags2.split(","));

        return cosineSimilarity(list1, list2); // 调用你写的算法
    }


    /**
     * 推荐匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> bestMatchUsers(long num, User loginUser) {
//        如果因为电脑内存问题，没有办法像大佬电脑那样可以存放100万数据，可以直接运行。可以选择运行5万条数据。
//        不然的话会报 OOM（内存）的问题
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.last("limit 50000");
//        List<User> userList = this.list(queryWrapper);
//         或者用page分页查询，自己输入或默认数值，但这样匹配就有限制了
//        List<User> userList = this.page(new Page<>(pageNum,pageSize),queryWrapper);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);

        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
//        System.out.println(tagList);
        // 用户列表的下表 => 相似度
//        List<Pair<User,Long>> list = new ArrayList<>();
        List<Pair<User, Double>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            //无标签的 或当前用户为自己 跳过
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 使用编辑距离算法计算分数
//            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
//            list.add(new ImmutablePair<>(user,distance));
            // 使用余弦相似度算法替换编辑距离
            double similarity = cosineSimilarity(tagList, userTagList);
            list.add(new ImmutablePair<>(user, similarity));
        }
        //按编辑距离有小到大排序
//        List<Pair<User, Long>> topUserPairList = list.stream()
//                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
//                .limit(num)
//                .collect(Collectors.toList());
        // 按余弦相似度由大到小排序 (注意排序顺序改为降序)
        List<Pair<User, Double>> topUserPairList = list.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 降序排序
                .limit(num)
                .collect(Collectors.toList());
        //有顺序的userID列表
        List<Long> userListVo = topUserPairList.stream().map(pari -> pari.getKey().getId()).collect(Collectors.toList());

        //根据id查询user完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userListVo);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userListVo) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 匹配用户 ( 标签相近度 ?% )
     *
     */
    // 9.15 / 100w
/*
    @Override
    public List<User> partMatchUsers(long num,double rate, User loginUser) {
//        如果因为电脑内存问题，没有办法像大佬电脑那样可以存放100万数据，可以直接运行。可以选择运行5万条数据。
//        不然的话会报 OOM（内存）的问题
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.last("limit 50000");
//        List<User> userList = this.list(queryWrapper);
//         或者用page分页查询，自己输入或默认数值，但这样匹配就有限制了
//        List<User> userList = this.page(new Page<>(pageNum,pageSize),queryWrapper);
        // 标签近似度阈值, 获取当前用户的标签
        // 获取当前用户的标签
//        double threshold = 1.0 * size / loginUser.getTags().length();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);

        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
//        System.out.println(tagList);
        // 用户列表的下表 => 相似度
//        List<Pair<User,Long>> list = new ArrayList<>();
        List<Pair<User, Double>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            //无标签的 或当前用户为自己 跳过
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 使用编辑距离算法计算分数
//            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
//            list.add(new ImmutablePair<>(user,distance));
            // 使用余弦相似度算法替换编辑距离
            double similarity = cosineSimilarity(tagList, userTagList);
            list.add(new ImmutablePair<>(user, similarity));
        }
        //按编辑距离有小到大排序
//        List<Pair<User, Long>> topUserPairList = list.stream()
//                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
//                .limit(num)
//                .collect(Collectors.toList());
        // 按余弦相似度由大到小排序 (注意排序顺序改为降序)
//        List<Pair<User, Double>> topUserPairList = list.stream()
//                .filter(pair -> pair.getValue() > threshold)
////                .sorted((a, b) -> Double.compare(b.getVaflue(), a.getValue())) // 降序排序
////                .limit(num)
//                .collect(Collectors.toList());

        // 首先过滤出超过阈值的用户
        List<Pair<User, Double>> filteredList = list.stream()
                .filter(pair -> pair.getValue() > rate)
                .collect(Collectors.toList());

        // 然后随机打乱顺序
        Collections.shuffle(filteredList);

        // 最后限制返回数量
        List<Pair<User, Double>> topUserPairList = filteredList.stream()
                .limit(num)
                .collect(Collectors.toList());

        //有顺序的userID列表
        List<Long> userListVo = topUserPairList.stream()
                .map(pari -> pari.getKey().getId())
                .collect(Collectors.toList());

        //根据id查询user完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userListVo);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userListVo) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }
*/


     // 匹配用户 - 改进版 - 线程池   5.62 / 100w
    @Override
    public List<User> partMatchUsers(long num, double rate, User loginUser) {
        // 1. 查询所有有标签的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);

        Gson gson = new Gson();

        // 2. 当前用户标签转 BitSet
        List<String> loginTagList = gson.fromJson(loginUser.getTags(), new TypeToken<List<String>>() {}.getType());
        BitSet loginBitSet = AlgorithmUtils.toBitSet(loginTagList);

        // 3. 线程池并行计算
        int threadCount = Runtime.getRuntime().availableProcessors() * 2; // 建议开多一倍核数
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<Pair<User, Double>>>> futures = new ArrayList<>();

        int batchSize = 1000; // 每批处理用户数
        for (int i = 0; i < userList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, userList.size());
            List<User> subList = userList.subList(start, end);

            futures.add(executor.submit(() -> {
                List<Pair<User, Double>> localList = new ArrayList<>();
                for (User user : subList) {
                    if (StringUtils.isBlank(user.getTags()) || user.getId()==(loginUser.getId())) {
                        continue;
                    }
                    List<String> userTagList = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());
                    BitSet userBitSet = AlgorithmUtils.toBitSet(userTagList);

                    double similarity = AlgorithmUtils.cosineSimilarity(loginBitSet, userBitSet);
                    if (similarity > rate) {
                        localList.add(new ImmutablePair<>(user, similarity));
                    }
                }
                return localList;
            }));
        }

        // 4. 汇总结果
        List<Pair<User, Double>> pairList = new ArrayList<>();
        try {
            for (Future<List<Pair<User, Double>>> future : futures) {
                pairList.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("并行计算出错", e);
        } finally {
            executor.shutdown();
        }

        // 5. 打乱并截取
        Collections.shuffle(pairList);
        List<Pair<User, Double>> topUserPairList = pairList.stream()
                .limit(num)
                .collect(Collectors.toList());

        // 6. 按顺序查询完整用户信息
        List<Long> userIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }


    // 匹配用户 - 改进版 - 线程池、BitMap版   5.61 / 100w
/*    @Override
    public List<User> partMatchUsers(long num, double rate, User loginUser) {
        // 1. 查询所有有标签的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);

        // 2. 当前用户标签转 List
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(loginUser.getTags(), new TypeToken<List<String>>() {}.getType());

        // 3. 线程池并行计算
        int threadCount = Runtime.getRuntime().availableProcessors(); // CPU 核数
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<Pair<User, Double>>>> futures = new ArrayList<>();

        int batchSize = 10000; // 每批任务量，可调节
        for (int start = 0; start < userList.size(); start += batchSize) {
            int end = Math.min(start + batchSize, userList.size());
            List<User> subList = userList.subList(start, end);

            futures.add(executor.submit(() -> {
                List<Pair<User, Double>> localList = new ArrayList<>();
                for (User user : subList) {
                    if (StringUtils.isBlank(user.getTags()) || user.getId() == loginUser.getId()) {
                        continue;
                    }
                    List<String> userTagList = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());
                    double similarity = cosineSimilarity(tagList, userTagList);
                    if (similarity > rate) {
                        localList.add(new ImmutablePair<>(user, similarity));
                    }
                }
                return localList;
            }));
        }

        // 4. 汇总结果
        List<Pair<User, Double>> pairList = new ArrayList<>();
        try {
            for (Future<List<Pair<User, Double>>> future : futures) {
                pairList.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("并行计算出错", e);
        } finally {
            executor.shutdown();
        }

        // 5. 打乱并截取
        Collections.shuffle(pairList);
        List<Pair<User, Double>> topUserPairList = pairList.stream()
                .limit(num)
                .collect(Collectors.toList());

        // 6. 按顺序查询完整用户信息
        List<Long> userIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }*/
    public List<Long> roughFilterByTags(Long loginUserId, int limit) {
        List<String> loginTags = getUserTags(loginUserId);

        Set<Long> candidateUserIds = new HashSet<>();
        for (String tag : loginTags) {
            String key = "tag:" + tag;
            Set<String> members = redisTemplate.opsForSet().members(key);
            if (members != null) {
                candidateUserIds.addAll(
                        members.stream().map(Long::valueOf).collect(Collectors.toSet())
                );
            }
        }
        candidateUserIds.remove(loginUserId);

        // 简单用“共同标签数”来做排序
        Map<Long, Integer> countMap = new HashMap<>();
        for (Long uid : candidateUserIds) {
            int common = countCommonTags(loginUserId, uid);
            countMap.put(uid, common);
        }

        return countMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pair<Long, Double>> calcAccurateSimilarity(Long loginUserId, double rate) {
        List<String> loginTags = getUserTags(loginUserId);
        BitSet loginBitSet = AlgorithmUtils.toBitSet(loginTags);

        int pageSize = 1000;
        int page = 0;
        List<Pair<Long, Double>> result = new ArrayList<>();

        while (true) {
            List<User> batchUsers = userMapper.listUsersByPage(page, pageSize);
            if (batchUsers.isEmpty()) break;

            for (User user : batchUsers) {
                if (user.getId()==loginUserId || StringUtils.isBlank(user.getTags())) continue;

                List<String> tags = getUserTags(user.getId());
                BitSet bitSet = AlgorithmUtils.toBitSet(tags);
                double sim = AlgorithmUtils.cosineSimilarity(loginBitSet, bitSet);

                if (sim > rate) {
                    result.add(new ImmutablePair<>(user.getId(), sim));
                }
            }
            page++;
        }

        result.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return result;
    }

    private int countCommonTags(Long userId1, Long userId2) {
        BitSet bitSet1 = AlgorithmUtils.toBitSet(getUserTags(userId1));
        BitSet bitSet2 = AlgorithmUtils.toBitSet(getUserTags(userId2));

        BitSet clone = (BitSet) bitSet1.clone();
        clone.and(bitSet2); // 求交集
        return clone.cardinality(); // 返回交集元素个数
    }

    private List<String> getUserTags(Long loginUserId) {
        User loginUser = this.getById(loginUserId);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        return tagList;

    }

    @Override
    public List<User> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return this.listByIds(ids);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final int pageSize = 10000;
        int pageNum = 1;
        List<User> resultList = new ArrayList<>();
        Gson gson = new Gson();

        while (true) {
            // 分页拉取，直到没有数据
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("tags");
            for (String tag : tagNameList) {
                queryWrapper.like("tags", tag);
            }
            // 分页查询
            Page<User> page = new Page<>(pageNum, pageSize);
            Page<User> userPage = this.page(page, queryWrapper);

            List<User> userList = userPage.getRecords();
            if (CollectionUtils.isEmpty(userList)) break;

            // 并行流处理
            List<User> matched = userList.parallelStream()
                    .filter(user -> StringUtils.isNotBlank(user.getTags()))
                    .filter(user -> {
                        List<String> userTags = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());
                        return userTags.containsAll(tagNameList);
                    })
                    .map(this::getSafetyUser)
                    .collect(Collectors.toList());

            resultList.addAll(matched);

            // 如果最后一页则退出
            if (userList.size() < pageSize) break;
            pageNum++;
        }

        return resultList;
    }
/*
    // 旧
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        // 预计用户数量高于 1500~5000 条数据，使用 内存 搜索
        if (userList.size() > 2000) {
            return memorySearch(tagNameList);
        }
        return sqlSearch(tagNameList);
    }*/

    /**
     * 根据标签搜索用户  SQL
     *
     * @param tagNameList
     * @return
     */
    public List<User> sqlSearch(List<String> tagNameList) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接tag
        // like '% A %' and like '% B %'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户  内存搜索
     *
     * @param tagNameList
     * @return
     */
    public List<User> memorySearch(List<String> tagNameList) {
        // 1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2.判断内存 过滤出包含标签的用户
        return userList.stream().filter(user -> {
            String tags = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            // 兼容性处理 (去null)
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }
}
