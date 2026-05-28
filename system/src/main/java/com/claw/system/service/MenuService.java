package com.claw.system.service;

import com.claw.common.base.BaseService;
import com.claw.system.entity.Menu;
import com.claw.system.param.MenuParam;
import com.claw.system.vo.MenuVO;

import java.util.List;

public interface MenuService extends BaseService<Menu> {

    List<MenuVO> getMenuTree() throws Exception;

    List<MenuVO> getUserMenuTree(Long userId) throws Exception;

    List<String> getUserPermissions(Long userId) throws Exception;

    boolean addMenu(MenuParam menuParam) throws Exception;

    boolean updateMenu(MenuParam menuParam) throws Exception;

    boolean deleteMenu(Long id) throws Exception;
}
