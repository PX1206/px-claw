package com.claw.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.base.BaseServiceImpl;
import com.claw.common.exception.BusinessException;
import com.claw.common.pagination.PageInfo;
import com.claw.common.pagination.Paging;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.Role;
import com.claw.system.entity.RoleMenu;
import com.claw.system.entity.User;
import com.claw.system.entity.UserRole;
import com.claw.system.mapper.RoleMapper;
import com.claw.system.mapper.RoleMenuMapper;
import com.claw.system.mapper.UserMapper;
import com.claw.system.mapper.UserRoleMapper;
import com.claw.system.param.RoleMenuParam;
import com.claw.system.param.RolePageParam;
import com.claw.system.param.RoleParam;
import com.claw.system.param.UserRoleParam;
import com.claw.system.service.RoleService;
import com.claw.system.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Paging<RoleVO> getRolePageList(RolePageParam rolePageParam) throws Exception {
        Page<Role> page = new PageInfo<>(rolePageParam);
        IPage<RoleVO> iPage = roleMapper.getRoleList(page, rolePageParam);
        return new Paging<>(iPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(RoleParam roleParam) throws Exception {
        Integer count = roleMapper.selectCount(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getCode, roleParam.getCode())
                .eq(Role::getDelFlag, false));
        if (count > 0) {
            throw new BusinessException("角色标识已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleParam, role);
        role.setStatus(1);
        role.setCreateBy(LoginUtil.getUserId());
        role.setCreateTime(new Date());
        role.setDelFlag(false);
        roleMapper.insert(role);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleParam roleParam) throws Exception {
        Role role = roleMapper.selectById(roleParam.getId());
        if (ObjectUtil.isNull(role)) {
            throw new BusinessException("角色不存在");
        }

        Integer count = roleMapper.selectCount(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getCode, roleParam.getCode())
                .eq(Role::getDelFlag, false)
                .ne(Role::getId, roleParam.getId()));
        if (count > 0) {
            throw new BusinessException("角色标识已存在");
        }

        role.setName(roleParam.getName());
        role.setCode(roleParam.getCode());
        role.setDescription(roleParam.getDescription());
        role.setUpdateBy(LoginUtil.getUserId());
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) throws Exception {
        Role role = roleMapper.selectById(id);
        if (ObjectUtil.isNull(role)) {
            throw new BusinessException("角色不存在");
        }
        if ("admin".equals(role.getCode())) {
            throw new BusinessException("超级管理员角色不能删除");
        }

        role.setDelFlag(true);
        role.setUpdateBy(LoginUtil.getUserId());
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);

        roleMenuMapper.delete(Wrappers.lambdaQuery(RoleMenu.class).eq(RoleMenu::getRoleId, id));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleStatus(Long id) throws Exception {
        Role role = roleMapper.selectById(id);
        if (ObjectUtil.isNull(role)) {
            throw new BusinessException("角色不存在");
        }
        role.setStatus(role.getStatus() == 1 ? 0 : 1);
        role.setUpdateBy(LoginUtil.getUserId());
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenus(RoleMenuParam roleMenuParam) throws Exception {
        Long roleId = roleMenuParam.getRoleId();
        roleMenuMapper.delete(Wrappers.lambdaQuery(RoleMenu.class).eq(RoleMenu::getRoleId, roleId));

        if (roleMenuParam.getMenuIds() != null && !roleMenuParam.getMenuIds().isEmpty()) {
            for (Long menuId : roleMenuParam.getMenuIds()) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) throws Exception {
        return roleMenuMapper.getMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserRoles(UserRoleParam userRoleParam) throws Exception {
        Long userId = userRoleParam.getUserId();
        userRoleMapper.delete(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));

        if (userRoleParam.getRoleIds() != null && !userRoleParam.getRoleIds().isEmpty()) {
            for (Long roleId : userRoleParam.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        // 同步 User.role 字段（角色1=admin，否则=user）
        User user = userMapper.selectById(userId);
        if (user != null) {
            List<Long> roleIds = userRoleParam.getRoleIds();
            String role = (roleIds != null && roleIds.contains(1L)) ? "admin" : "user";
            user.setRole(role);
            user.setUpdateBy(LoginUtil.getUserId());
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }
        return true;
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) throws Exception {
        return userRoleMapper.getRoleIdsByUserId(userId);
    }

    @Override
    public List<RoleVO> getAllRoles() throws Exception {
        List<Role> roles = roleMapper.selectList(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getStatus, 1)
                .eq(Role::getDelFlag, false)
                .orderByAsc(Role::getId));
        return roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
