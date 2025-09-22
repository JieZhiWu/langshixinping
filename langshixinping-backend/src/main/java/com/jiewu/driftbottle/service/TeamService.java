package com.jiewu.driftbottle.service;

import com.jiewu.driftbottle.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.model.dto.TeamQuery;
import com.jiewu.driftbottle.model.request.TeamJoinRequest;
import com.jiewu.driftbottle.model.request.TeamQuitRequest;
import com.jiewu.driftbottle.model.request.TeamUpdateRequest;
import com.jiewu.driftbottle.model.vo.TeamUserVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author JieWu
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-09-17 08:19:51
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表（包括已加入的，和未加入的）
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     *
     * @param team
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
