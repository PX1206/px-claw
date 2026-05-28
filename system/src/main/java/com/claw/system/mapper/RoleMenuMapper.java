package com.claw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.claw.system.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    List<Long> getMenuIdsByRoleId(@Param("roleId") Long roleId);
}
