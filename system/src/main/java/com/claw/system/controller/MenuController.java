package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.base.BaseController;
import com.claw.common.enums.OperationLogType;
import com.claw.common.log.Module;
import com.claw.common.log.OperationLog;
import com.claw.common.tool.LoginUtil;
import com.claw.system.param.MenuParam;
import com.claw.system.service.MenuService;
import com.claw.system.vo.MenuVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/menu")
@Module("claw")
@Api(value = "菜单API", tags = {"菜单管理"})
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/getMenuTree")
    @ApiOperation(value = "获取完整菜单树", response = MenuVO.class)
    public ApiResult<List<MenuVO>> getMenuTree() throws Exception {
        List<MenuVO> menuTree = menuService.getMenuTree();
        return ApiResult.ok(menuTree);
    }

    @GetMapping("/getUserMenuTree")
    @ApiOperation(value = "获取当前用户菜单树", response = MenuVO.class)
    public ApiResult<List<MenuVO>> getUserMenuTree() throws Exception {
        List<MenuVO> menuTree = menuService.getUserMenuTree(LoginUtil.getUserId());
        return ApiResult.ok(menuTree);
    }

    @GetMapping("/getUserPermissions")
    @ApiOperation(value = "获取当前用户权限code列表")
    public ApiResult<List<String>> getUserPermissions() throws Exception {
        List<String> permissions = menuService.getUserPermissions(LoginUtil.getUserId());
        return ApiResult.ok(permissions);
    }

    @PostMapping("/add")
    @OperationLog(name = "新增菜单", type = OperationLogType.ADD)
    @ApiOperation(value = "新增菜单", response = ApiResult.class)
    public ApiResult<Boolean> addMenu(@Validated @RequestBody MenuParam menuParam) throws Exception {
        boolean flag = menuService.addMenu(menuParam);
        return ApiResult.result(flag);
    }

    @PostMapping("/update")
    @OperationLog(name = "修改菜单", type = OperationLogType.UPDATE)
    @ApiOperation(value = "修改菜单", response = ApiResult.class)
    public ApiResult<Boolean> updateMenu(@Validated @RequestBody MenuParam menuParam) throws Exception {
        boolean flag = menuService.updateMenu(menuParam);
        return ApiResult.result(flag);
    }

    @PostMapping("/delete/{id}")
    @OperationLog(name = "删除菜单", type = OperationLogType.DELETE)
    @ApiOperation(value = "删除菜单", response = ApiResult.class)
    public ApiResult<Boolean> deleteMenu(@PathVariable("id") Long id) throws Exception {
        boolean flag = menuService.deleteMenu(id);
        return ApiResult.result(flag);
    }
}
