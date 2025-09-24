package com.jiewu.driftbottle.mapper;

import com.jiewu.driftbottle.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表
     * @param page
     * @param pageSize
     * @return
     */
    @Select("SELECT * FROM user LIMIT #{page}, #{pageSize}")
    List<User> listUsersByPage(int page, int pageSize);
}



