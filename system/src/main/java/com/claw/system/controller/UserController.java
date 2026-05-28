package com.claw.system.controller;

import com.claw.common.tool.LoginUtil;
import com.claw.common.vo.LoginUserInfoVO;
import com.claw.system.param.*;
import com.claw.system.service.UserService;
import com.claw.system.vo.UserListVO;
import com.claw.system.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import com.claw.common.base.BaseController;
import com.claw.common.api.ApiResult;
import com.claw.common.pagination.Paging;
import com.claw.common.log.Module;
import com.claw.common.log.OperationLog;
import com.claw.common.enums.OperationLogType;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 控制器
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Module("claw")
@Api(value = "用户API", tags = {"用户管理"})
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    @OperationLog(name = "用户注册", type = OperationLogType.ADD)
    @ApiOperation(value = "用户注册", response = ApiResult.class)
    public ApiResult<Boolean> register(@Validated @RequestBody UserRegisterParam userRegisterParam) throws Exception {
        boolean flag = userService.register(userRegisterParam);
        return ApiResult.result(flag);
    }

    /**
     * 用户登录（账号密码）
     */
    @PostMapping("/login/password")
    @OperationLog(name = "用户登录（账号密码）", type = OperationLogType.ADD)
    @ApiOperation(value = "用户登录（账号密码）", response = LoginUserInfoVO.class)
    public ApiResult<LoginUserInfoVO> passwordLogin(@Validated @RequestBody PasswordLoginParam passwordLoginParam) throws Exception {
        LoginUserInfoVO loginUserInfoVo = userService.passwordLogin(passwordLoginParam);
        return ApiResult.ok(loginUserInfoVo);
    }

    /**
     * 用户登录（短信验证码）
     */
    @PostMapping("/login/sms")
    @OperationLog(name = "用户登录（短信验证码）", type = OperationLogType.ADD)
    @ApiOperation(value = "用户登录（短信验证码）", response = LoginUserInfoVO.class)
    public ApiResult<LoginUserInfoVO> smsLogin(@Validated @RequestBody SMSLoginParam smsLoginParam) throws Exception {
        LoginUserInfoVO loginUserInfoVo = userService.smsLogin(smsLoginParam);
        return ApiResult.ok(loginUserInfoVo);
    }

    /**
     * 修改用户（当前用户）
     */
    @PostMapping("/update")
    @OperationLog(name = "修改用户（当前用户）", type = OperationLogType.UPDATE)
    @ApiOperation(value = "修改用户（当前用户）", response = ApiResult.class)
    public ApiResult<Boolean> update(@Validated @RequestBody UserParam userParam) throws Exception {
        boolean flag = userService.update(userParam);
        return ApiResult.result(flag);
    }

    /**
     * 新增用户（管理端）
     */
    @PostMapping("/add")
    @OperationLog(name = "新增用户（管理端）", type = OperationLogType.UPDATE)
    @ApiOperation(value = "新增用户（管理端）", response = ApiResult.class)
    public ApiResult<Boolean> addUser(@Validated @RequestBody AddUserParam addUserParam) throws Exception {
        boolean flag = userService.addUser(addUserParam);
        return ApiResult.result(flag);
    }

    /**
     * 修改用户（管理端）
     */
    @PostMapping("/updateUser")
    @OperationLog(name = "修改用户（管理端）", type = OperationLogType.UPDATE)
    @ApiOperation(value = "修改用户（管理端）", response = ApiResult.class)
    public ApiResult<Boolean> updateUser(@Validated @RequestBody UpdateUserParam updateUserParam) throws Exception {
        boolean flag = userService.updateUser(updateUserParam);
        return ApiResult.result(flag);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete/{id}")
    @OperationLog(name = "删除用户", type = OperationLogType.DELETE)
    @ApiOperation(value = "删除用户", response = ApiResult.class)
    public ApiResult<Boolean> deleteUser(@PathVariable("id") Long id) throws Exception {
        boolean flag = userService.deleteUser(id);
        return ApiResult.result(flag);
    }

    /**
     * 用户详情（管理端）
     */
    @GetMapping("/info/{id}")
    //@OperationLog(name = "用户详情（管理端）", type = OperationLogType.INFO)
    @ApiOperation(value = "用户详情（管理端）", response = UserVO.class)
    public ApiResult<UserVO> getUser(@PathVariable("id") Long id) throws Exception {
        UserVO userVO = userService.getUser(id);
        return ApiResult.ok(userVO);
    }

    /**
     * 用户详情（当前用户）
     */
    @GetMapping("/userInfo")
    //@OperationLog(name = "用户详情（当前用户）", type = OperationLogType.INFO)
    @ApiOperation(value = "用户详情（当前用户）", response = UserVO.class)
    public ApiResult<UserVO> getUser() throws Exception {
        UserVO userVO = userService.getUser(LoginUtil.getUserId());
        return ApiResult.ok(userVO);
    }

    /**
     * 用户分页列表
     */
    @PostMapping("/getPageList")
    //@OperationLog(name = "用户分页列表", type = OperationLogType.PAGE)
    @ApiOperation(value = "用户分页列表", response = UserListVO.class)
    public ApiResult<Paging<UserListVO>> getUserPageList(@Validated @RequestBody UserPageParam userPageParam) throws Exception {
        Paging<UserListVO> paging = userService.getUserPageList(userPageParam);
        return ApiResult.ok(paging);
    }

    /**
     * 禁用账号
     */
    @PostMapping("/disable/{id}")
    @OperationLog(name = "禁用账号", type = OperationLogType.DELETE)
    @ApiOperation(value = "禁用账号", response = ApiResult.class)
    public ApiResult<Boolean> disable(@PathVariable("id") Long id) throws Exception {
        boolean flag = userService.disable(id);
        return ApiResult.result(flag);
    }

    /**
     * 冻结
     */
    @PostMapping("/freeze/{id}")
    @OperationLog(name = "冻结账号", type = OperationLogType.DELETE)
    @ApiOperation(value = "冻结账号", response = ApiResult.class)
    public ApiResult<Boolean> freeze(@PathVariable("id") Long id) throws Exception {
        boolean flag = userService.freeze(id);
        return ApiResult.result(flag);
    }

    /**
     * 恢复账号
     */
    @PostMapping("/restore/{id}")
    @OperationLog(name = "恢复账号（账号被禁用或冻结）", type = OperationLogType.DELETE)
    @ApiOperation(value = "恢复账号（账号被禁用或冻结）", response = ApiResult.class)
    public ApiResult<Boolean> restore(@PathVariable("id") Long id) throws Exception {
        boolean flag = userService.restore(id);
        return ApiResult.result(flag);
    }

    @PostMapping("/updatePassword")
    @OperationLog(name = "修改密码（当前用户）", type = OperationLogType.OTHER)
    @ApiOperation(value = "修改密码（当前用户）", response = ApiResult.class)
    public ApiResult<Boolean> updatePassword(@Validated @RequestBody UpdatePasswordParam updatePasswordParam) throws Exception {
        boolean flag = userService.updatePassword(updatePasswordParam);
        return ApiResult.result(flag);
    }

    @PostMapping("/resetPassword")
    @OperationLog(name = "重置密码（管理端）", type = OperationLogType.OTHER)
    @ApiOperation(value = "重置密码（管理端）", response = ApiResult.class)
    public ApiResult<Boolean> resetPassword(@Validated @RequestBody ResetPasswordParam resetPasswordParam) throws Exception {
        boolean flag = userService.resetPassword(resetPasswordParam);
        return ApiResult.result(flag);
    }

    @PostMapping("/logOut")
    @OperationLog(name = "退出登录（当前用户）", type = OperationLogType.LOGOUT)
    @ApiOperation(value = "退出登录（当前用户）", response = ApiResult.class)
    public ApiResult<Boolean> logOut() throws Exception {
        LoginUtil.logout();
        return ApiResult.result(true);
    }

}

