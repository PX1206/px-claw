package com.claw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.claw.system.entity.User;
import com.claw.system.param.UserPageParam;

import com.claw.system.vo.UserListVO;
import com.claw.system.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    UserVO getUserVO(@Param("id") Long id);

    IPage<UserListVO> getUserList(@Param("page") Page page, @Param("param") UserPageParam param);

    Long getUserIdByUserNo(@Param("userNo") String userNo);

}
