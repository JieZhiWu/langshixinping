package com.jiewu.driftbottle.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiewu.driftbottle.common.BaseResponse;
import com.jiewu.driftbottle.common.DeleteRequest;
import com.jiewu.driftbottle.common.ErrorCode;
import com.jiewu.driftbottle.common.ResultUtils;
import com.jiewu.driftbottle.exception.BusinessException;
import com.jiewu.driftbottle.model.domain.Team;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.model.domain.UserTeam;
import com.jiewu.driftbottle.model.dto.TeamQuery;
import com.jiewu.driftbottle.model.request.TeamAddRequest;
import com.jiewu.driftbottle.model.request.TeamJoinRequest;
import com.jiewu.driftbottle.model.request.TeamQuitRequest;
import com.jiewu.driftbottle.model.request.TeamUpdateRequest;
import com.jiewu.driftbottle.model.vo.TeamUserVO;
import com.jiewu.driftbottle.service.TeamService;
import com.jiewu.driftbottle.service.UserService;
import com.jiewu.driftbottle.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User logininUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, logininUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 获取当前用户创建的队伍列表
     *
     * @param teamQuery 队伍查询条件
     * @param request   HTTP请求对象，用于获取当前登录用户信息
     * @return 我创建的队伍列表
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍列表
     *
     * @param teamQuery 队伍查询条件
     * @param request   HTTP请求对象，用于获取当前登录用户信息
     * @return 我加入的队伍列表（包含加入状态、是否创建者、已加入人数）
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(request);

        // 1. 查询当前用户加入的 UserTeam 记录
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        if (CollectionUtils.isEmpty(userTeamList)) {
            return ResultUtils.success(new ArrayList<>());
        }

        // 2. 提取队伍 ID 列表
        Set<Long> joinTeamIdSet = userTeamList.stream()
                .map(UserTeam::getTeamId)
                .collect(Collectors.toSet());

        teamQuery.setIdList(new ArrayList<>(joinTeamIdSet));

        // 3. 查询队伍信息
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        // 4. 查询这些队伍的成员情况
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> allUserTeamList = userTeamService.list(userTeamJoinQueryWrapper);

        // 队伍 ID -> 加入该队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = allUserTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));

        // 5. 设置额外状态
        teamList.forEach(team -> {
            // 是否已加入（必然 true，因为就是“我加入的队伍”）
            team.setHasJoin(joinTeamIdSet.contains(team.getId()));

            // 是否为当前用户创建
            boolean isCreator = loginUser.getId()==team.getUserId();
            team.setCreator(isCreator);

            // 队伍已加入人数
            int hasJoinNum = teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size();
            team.setHasJoinNum(hasJoinNum);
        });

        return ResultUtils.success(teamList);
    }

/*
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 ID  1 => 1, 2, 3     2 => 1, 3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }
*/

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestBody long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

/*
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }
*/
@GetMapping("/list")
public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
    if (teamQuery == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean isAdmin = userService.isAdmin(request);
    // 1、查询队伍列表
    List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
    final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
    // 2、判断当前用户是否已加入队伍
    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
    try {
        // 已加入的队伍 id 集合
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId(); // 登录用户 ID
        userTeamQueryWrapper.eq("userId", loginUser.getId());
        userTeamQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        // 2.2 新增：判断是否为创建者 （核心逻辑）
        teamList.forEach(team -> {
            // 标记是否已加入
            boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
            team.setHasJoin(hasJoin);

            // 标记是否为当前用户创建（对比队伍的 userId 和登录用户 ID）
            boolean isCreator = loginUserId.equals(team.getUserId()); // 关键：队伍的创建者 ID == 登录用户 ID
            team.setCreator(isCreator);
        });
    }catch (Exception e){}
    QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
    userTeamJoinQueryWrapper.in("teamId",teamIdList);
    List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
    //队伍1d=>加入这个队伍的用户列表
    Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
    teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size()));
    return ResultUtils.success(teamList);
}

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(team, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

}
