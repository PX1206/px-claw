package com.claw.system.service;

import com.claw.common.base.BaseService;
import com.claw.common.pagination.Paging;
import com.claw.system.entity.Role;
import com.claw.system.param.RoleMenuParam;
import com.claw.system.param.RolePageParam;
import com.claw.system.param.RoleParam;
import com.claw.system.param.UserRoleParam;
import com.claw.system.vo.RoleVO;

import java.util.List;

public interface RoleService extends BaseService<Role> {

    Paging<RoleVO> getRolePageList(RolePageParam rolePageParam) throws Exception;

    boolean addRole(RoleParam roleParam) throws Exception;

    boolean updateRole(RoleParam roleParam) throws Exception;

    boolean deleteRole(Long id) throws Exception;

    boolean toggleStatus(Long id) throws Exception;

    boolean saveRoleMenus(RoleMenuParam roleMenuParam) throws Exception;

    List<Long> getRoleMenuIds(Long roleId) throws Exception;

    boolean saveUserRoles(UserRoleParam userRoleParam) throws Exception;

    List<Long> getUserRoleIds(Long userId) throws Exception;

    List<RoleVO> getAllRoles() throws Exception;
}
