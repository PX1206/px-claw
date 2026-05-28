package com.claw.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.claw.common.base.BaseServiceImpl;
import com.claw.common.exception.BusinessException;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.Menu;
import com.claw.system.mapper.MenuMapper;
import com.claw.system.param.MenuParam;
import com.claw.system.service.MenuService;
import com.claw.system.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<MenuVO> getMenuTree() throws Exception {
        List<MenuVO> allMenus = menuMapper.getAllMenus();
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<MenuVO> getUserMenuTree(Long userId) throws Exception {
        List<MenuVO> userMenus = menuMapper.getMenusByUserId(userId);
        return buildTree(userMenus, 0L);
    }

    @Override
    public List<String> getUserPermissions(Long userId) throws Exception {
        return menuMapper.getPermissionsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMenu(MenuParam menuParam) throws Exception {
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuParam, menu);
        if (menu.getPid() == null) {
            menu.setPid(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        menu.setCreateBy(LoginUtil.getUserId());
        menu.setCreateTime(new Date());
        menu.setDelFlag(false);
        menuMapper.insert(menu);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(MenuParam menuParam) throws Exception {
        Menu menu = menuMapper.selectById(menuParam.getId());
        if (ObjectUtil.isNull(menu)) {
            throw new BusinessException("菜单不存在");
        }
        BeanUtils.copyProperties(menuParam, menu);
        menu.setUpdateBy(LoginUtil.getUserId());
        menu.setUpdateTime(new Date());
        menuMapper.updateById(menu);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long id) throws Exception {
        Menu menu = menuMapper.selectById(id);
        if (ObjectUtil.isNull(menu)) {
            throw new BusinessException("菜单不存在");
        }
        Integer childCount = menuMapper.selectCount(Wrappers.lambdaQuery(Menu.class)
                .eq(Menu::getPid, id)
                .eq(Menu::getDelFlag, false));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不能删除");
        }

        menu.setDelFlag(true);
        menu.setUpdateBy(LoginUtil.getUserId());
        menu.setUpdateTime(new Date());
        menuMapper.updateById(menu);
        return true;
    }

    private List<MenuVO> buildTree(List<MenuVO> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getPid(), parentId))
                .peek(m -> m.setChildren(buildTree(menus, m.getId())))
                .collect(Collectors.toList());
    }
}
