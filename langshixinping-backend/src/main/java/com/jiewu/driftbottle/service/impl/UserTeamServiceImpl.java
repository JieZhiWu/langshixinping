package com.jiewu.driftbottle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiewu.driftbottle.model.domain.UserTeam;
import com.jiewu.driftbottle.service.UserTeamService;
import com.jiewu.driftbottle.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author JieWu
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-09-17 08:37:00
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




