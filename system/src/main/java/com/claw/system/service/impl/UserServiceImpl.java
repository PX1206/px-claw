package com.claw.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.claw.common.constant.CommonConstant;
import com.claw.common.exception.BusinessException;
import com.claw.common.redis.RedisUtil;
import com.claw.common.tool.*;
import com.claw.common.vo.LoginUserInfoVO;
import com.claw.common.constant.SyncQuotaConstants;
import com.claw.system.entity.User;
import com.claw.system.entity.UserRole;
import com.claw.system.mapper.*;
import com.claw.system.param.*;
import com.claw.system.service.MenuService;
import com.claw.system.service.RoleService;
import com.claw.system.service.UserService;
import com.claw.common.base.BaseServiceImpl;
import com.claw.common.pagination.Paging;
import com.claw.common.pagination.PageInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.system.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 用户 服务实现类
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Slf4j
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Value("${rsa.data.private-key-str}")
    private String privateKeyStr;

    @Value("${user.password-error-num}")
    private Integer PASSWORD_ERROR_NUM;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private LocalFileMapper localFileMapper;

    /**
     * 校验短信验证码（保留 666666 测试后门）
     */
    private void validateSmsCode(String mobile, String smsCode) throws Exception {
        if ("666666".equals(smsCode)) {
            return;
        }
        if (!commonUtil.checkCode(CommonConstant.SMS_CODE + mobile, smsCode)) {
            throw new BusinessException(500, "短信验证码错误");
        }
    }

    /**
     * 解密并校验密码复杂度
     * 前端加密格式：13位时间戳 + 密码，需剥离时间戳后再校验和存储
     */
    private String decryptAndValidatePassword(String encryptedPassword) throws Exception {
        String decryptStr = RSAUtil.decrypt(encryptedPassword, privateKeyStr);
        String password;
        // 若解密后长度>13且前13位为数字，则为"时间戳+密码"格式，取后部分为真实密码
        if (decryptStr.length() > 13 && decryptStr.substring(0, 13).matches("\\d{13}")) {
            password = decryptStr.substring(13);
        } else {
            password = decryptStr;
        }
        validatePasswordRules(password);
        return password;
    }

    /**
     * 密码复杂度校验
     */
    private void validatePasswordRules(String password) {
        if (password.length() < 8) {
            throw new BusinessException("密码长度不能小于8位");
        }
        if (!PwdCheckUtil.checkContainDigit(password) || !PwdCheckUtil.checkContainCase(password)) {
            throw new BusinessException("密码必须由数字和字母组成");
        }
        if (PwdCheckUtil.checkSequentialSameChars(password, 4)) {
            throw new BusinessException("存在四个或以上连续相同字符");
        }
    }

    @Override
    @Transactional
    public boolean register(UserRegisterParam userRegisterParam) throws Exception {
        // 一个手机号只能注册一个账号
        Integer checkMobile = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, userRegisterParam.getMobile())
                .ne(User::getStatus, 0));
        if (checkMobile > 0) {
            throw new BusinessException("当前手机号已注册，请直接登录");
        }
        // 还需要验证用户名是否已存在
        Integer checkUsername = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, userRegisterParam.getUsername())
                .ne(User::getStatus, 0));
        if (checkUsername > 0) {
            throw new BusinessException("当前用户名已存在");
        }
        // 获取前端传过来的密码解密成明文并校验复杂度
        String password = decryptAndValidatePassword(userRegisterParam.getPassword());

        // 校验短信验证码（支持测试码）
        validateSmsCode(userRegisterParam.getMobile(), userRegisterParam.getSmsCode());

        // 如果昵称为空则直接取账号
        if (StringUtil.isBlank(userRegisterParam.getNickname())) {
            userRegisterParam.setNickname(userRegisterParam.getUsername());
        }

        // 开始保存用户
        User user = new User();
        BeanUtils.copyProperties(userRegisterParam, user);
        // 生成用户ID
        user.setUserNo("88" + StringUtil.getStrNo());
        // 生成盐
        user.setSalt(SHA256Util.getSHA256Str(UUID.randomUUID().toString()));
        // HA256S加密密码
        user.setPassword(SHA256Util.getSHA256Str(password + user.getSalt()));
        user.setStatus(1); // 状态为正常
        user.setRole("user"); // 注册用户默认普通用户角色
        user.setSyncQuotaBytes(SyncQuotaConstants.DEFAULT_QUOTA_BYTES);
        userMapper.insert(user);

        // 自动分配普通用户角色（roleId=2）
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L);
        userRoleMapper.insert(userRole);

        return true;
    }

    @Override
    public LoginUserInfoVO passwordLogin(PasswordLoginParam passwordLoginParam) throws Exception {

        // 根据用户账号获取用户信息
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, passwordLoginParam.getUsername())
                .ne(User::getStatus, 0));

        if (ObjectUtil.isNull(user)) {
            throw new BusinessException(500, "用户名或密码错误");
        }

        // ==============================
        // 1 RSA解密
        // ==============================

        String decryptStr = RSAUtil.decrypt(passwordLoginParam.getPassword(), privateKeyStr);

        if (decryptStr.length() <= 13) {
            throw new BusinessException(500, "非法请求");
        }

        // ==============================
        // 2 解析时间戳 + 密码
        // ==============================

        String timestampStr = decryptStr.substring(0, 13);
        String password = decryptStr.substring(13);

        long timestamp = Long.parseLong(timestampStr);
        long now = System.currentTimeMillis();

        // ==============================
        // 3 时间戳校验（5秒）
        // ==============================

        if (Math.abs(now - timestamp) > 30000) {
            throw new BusinessException(500, "请求已过期");
        }

        // ==============================
        // 4 Redis防重放
        // ==============================

        String redisKey = "LOGIN_REPLAY_" + timestampStr + passwordLoginParam.getUsername();

        if (redisUtil.hasKey(redisKey)) {
            throw new BusinessException(500, "请求重复");
        }

        redisUtil.set(redisKey, "1", 30); // 5秒过期

        // ==============================
        // 5 多次登录错误拦截
        // ==============================

        if (redisUtil.hasKey(CommonConstant.PASSWORD_ERROR_NUM + user.getId())) {

            Integer errorNum = (Integer) redisUtil.get(CommonConstant.PASSWORD_ERROR_NUM + user.getId());

            if (errorNum > PASSWORD_ERROR_NUM) {
                throw new BusinessException(500, "密码输入错误次数过多账号已冻结，请重置密码或次日自行解冻");
            }
        }

        // ==============================
        // 6 密码校验
        // ==============================

        if (!user.getPassword().equals(SHA256Util.getSHA256Str(password + user.getSalt()))) {

            long errorNum = redisUtil.incr(CommonConstant.PASSWORD_ERROR_NUM + user.getId(), 1);

            redisUtil.expire(CommonConstant.PASSWORD_ERROR_NUM + user.getId(), DateUtil.timeToMidnight());

            if (errorNum > PASSWORD_ERROR_NUM) {

                user.setStatus(4);
                userMapper.updateById(user);

                throw new BusinessException(500, "密码输入错误次数过多账号已冻结，请重置密码或次日自行解冻");
            }

            throw new BusinessException(500, "用户名或密码错误");
        }

        // ==============================
        // 7 登录成功清空错误次数
        // ==============================

        if (redisUtil.hasKey(CommonConstant.PASSWORD_ERROR_NUM + user.getId())) {
            redisUtil.del(CommonConstant.PASSWORD_ERROR_NUM + user.getId());
        }

        return login(user, null);
    }

    private LoginUserInfoVO login(User user, String token) throws Exception {
        // 验证用户状态
        if (user.getStatus() == 2) {
            throw new BusinessException("账号已被禁用，请联系平台客服");
        }

        if (user.getStatus() == 3 || user.getStatus() == 4) {
            throw new BusinessException("账号已被冻结，请联系平台客服");
        }

        // 更新登录时间
        user.setLoginTime(new Date());
        userMapper.updateById(user);

        // 封装登录信息返回
        LoginUserInfoVO loginUserInfoVO = new LoginUserInfoVO();
        BeanUtils.copyProperties(user, loginUserInfoVO);
        // 根据 UserRole 设置 role（角色 1=超级管理员 admin，2=普通用户 user）
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(user.getId());
        if (roleIds != null && roleIds.contains(1L)) {
            loginUserInfoVO.setRole("admin");
        } else {
            loginUserInfoVO.setRole("user");
        }

        // 查询用户权限列表
        try {
            List<String> permissions = menuService.getUserPermissions(user.getId());
            loginUserInfoVO.setPermissions(permissions);
        } catch (Exception e) {
            log.error("查询用户权限失败", e);
            loginUserInfoVO.setPermissions(new java.util.ArrayList<>());
        }

        // 登录成功保存token信息
        if (StringUtil.isBlank(token)) {
            token = UUID.randomUUID().toString();
        }
        loginUserInfoVO.setToken(token);
        redisUtil.set(token, loginUserInfoVO, CommonConstant.USER_TOKEN_VALIDITY);

        // 保存用户登录token，用户修改密码后删除
        LoginUtil.saveUserLoginToken(user.getId(), token);

        return loginUserInfoVO;
    }

    @Override
    public LoginUserInfoVO smsLogin(SMSLoginParam smsLoginParam) throws Exception {
        // 测试用，正式上线需要删除
        if (!"666666".equals(smsLoginParam.getSmsCode())) {
            // 校验短信验证码是否正确
            if (!commonUtil.checkCode(CommonConstant.SMS_CODE + smsLoginParam.getMobile(),
                    smsLoginParam.getSmsCode())) {
                throw new BusinessException(500, "短信验证码错误");
            }
        }

        // 根据用户手机号获取用户信息
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getMobile, smsLoginParam.getMobile())
                .ne(User::getStatus, 0));
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException(500, "当前手机号还未注册");
        }

        return login(user, null);
    }

    @Override
    @Transactional
    public boolean update(UserParam userParam) throws Exception {
        // 直接获取当前登录用户信息
        User user = userMapper.selectById(LoginUtil.getUserId());
        if (user == null) {
            throw new BusinessException("用户信息异常");
        }

        updateUserInfo(user, userParam);

        return true;
    }

    @Override
    @Transactional
    public boolean addUser(AddUserParam addUserParam) throws Exception {
        // 一个手机号只能注册一个账号
        Integer checkMobile = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, addUserParam.getMobile())
                .ne(User::getStatus, 0));
        if (checkMobile > 0) {
            throw new BusinessException("当前手机号已存在");
        }
        // 还需要验证用户名是否已存在
        Integer checkUsername = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, addUserParam.getUsername())
                .ne(User::getStatus, 0));
        if (checkUsername > 0) {
            throw new BusinessException("当前用户名已存在");
        }
        // 获取前端传过来的密码解密成明文并校验复杂度
        String password = decryptAndValidatePassword(addUserParam.getPassword());

        // 如果昵称为空则直接取账号
        if (StringUtil.isBlank(addUserParam.getNickname())) {
            addUserParam.setNickname(addUserParam.getUsername());
        }

        // 开始保存用户
        User user = new User();
        BeanUtils.copyProperties(addUserParam, user);
        // 生成用户ID
        user.setUserNo("88" + StringUtil.getStrNo());
        // 生成盐
        user.setSalt(SHA256Util.getSHA256Str(UUID.randomUUID().toString()));
        // HA256S加密密码
        user.setPassword(SHA256Util.getSHA256Str(password + user.getSalt()));
        user.setStatus(1); // 状态为正常
        user.setCreateBy(LoginUtil.getUserId());
        if (addUserParam.getSyncQuotaGb() != null) {
            int gb = addUserParam.getSyncQuotaGb();
            if (gb < SyncQuotaConstants.MIN_GB || gb > SyncQuotaConstants.MAX_GB) {
                throw new BusinessException("同步空间配额须在 1～2048 GB 之间");
            }
            user.setSyncQuotaBytes(SyncQuotaConstants.gbToBytes(gb));
        } else {
            user.setSyncQuotaBytes(SyncQuotaConstants.DEFAULT_QUOTA_BYTES);
        }
        userMapper.insert(user);

        // 保存用户角色，未传则默认普通用户（roleId=2）
        List<Long> roleIds = addUserParam.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = Collections.singletonList(2L); // 普通用户
        }
        roleService.saveUserRoles(new UserRoleParam().setUserId(user.getId()).setRoleIds(roleIds));

        return true;
    }

    private void updateUserInfo(User user, UserParam userParam) {
        if (userParam.getNickname() != null && !userParam.getNickname().trim().isEmpty()) {
            user.setNickname(userParam.getNickname().trim());
        }
        if (userParam.getHeadImg() != null) {
            user.setHeadImg(userParam.getHeadImg());
        }
        if (userParam.getSex() != null) {
            user.setSex(userParam.getSex());
        }
        if (userParam.getBirthday() != null) {
            user.setBirthday(userParam.getBirthday());
        }
        if (userParam.getAddress() != null) {
            user.setAddress(userParam.getAddress());
        }
        user.setUpdateBy(LoginUtil.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUser(UpdateUserParam updateUserParam) throws Exception {
        // 先获取用户信息
        User user = userMapper.selectById(updateUserParam.getId());
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException("用户信息异常");
        }
        // 判断用户名是否修改
        if (!user.getUsername().equals(updateUserParam.getUsername())) {
            Integer checkUsername = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                    .eq(User::getUsername, updateUserParam.getUsername())
                    .ne(User::getStatus, 0));
            if (checkUsername > 0) {
                throw new BusinessException("当前用户名已存在");
            }
        }

        // 判断手机号是否修改
        if (!user.getMobile().equals(updateUserParam.getMobile())) {
            Integer checkMobile = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                    .eq(User::getMobile, updateUserParam.getMobile())
                    .ne(User::getStatus, 0));

            if (checkMobile > 0) {
                throw new BusinessException("当前手机号已存在");
            }
        }

        user.setUsername(updateUserParam.getUsername());
        user.setMobile(updateUserParam.getMobile());
        if (updateUserParam.getSyncQuotaGb() != null) {
            int gb = updateUserParam.getSyncQuotaGb();
            if (gb < SyncQuotaConstants.MIN_GB || gb > SyncQuotaConstants.MAX_GB) {
                throw new BusinessException("同步空间配额须在 1～2048 GB 之间");
            }
            user.setSyncQuotaBytes(SyncQuotaConstants.gbToBytes(gb));
        }
        updateUserInfo(user, updateUserParam);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteUser(Long id) throws Exception {
        return super.removeById(id);
    }

    @Override
    public UserVO getUser(Long id) throws Exception {
        UserVO vo = userMapper.getUserVO(id);
        if (vo == null) {
            return null;
        }
        // 与登录态一致：按 user_role 判定管理员（user 表 role 列可能未维护）
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(id);
        if (roleIds != null && roleIds.contains(1L)) {
            vo.setRole("admin");
        } else {
            vo.setRole("user");
        }
        Long used = localFileMapper.sumSyncFileBytesByUserId(id);
        vo.setSyncUsedBytes(used != null ? used : 0L);
        return vo;
    }

    @Override
    public Paging<UserListVO> getUserPageList(UserPageParam userPageParam) throws Exception {
        Page<User> page = new PageInfo<>(userPageParam);
        IPage<UserListVO> iPage = userMapper.getUserList(page, userPageParam);
        return new Paging<UserListVO>(iPage);
    }

    @Override
    public boolean disable(Long id) throws Exception {
        // 获取用户信息
        User user = userMapper.selectById(id);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException("用户信息异常");
        }
        user.setStatus(2); // 禁用
        user.setUpdateBy(LoginUtil.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);

        // 禁用账号后需要退出用户所有登录
        LoginUtil.logoutAll(user.getId());

        return true;
    }

    @Override
    public boolean freeze(Long id) throws Exception {
        // 获取用户信息
        User user = userMapper.selectById(id);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException("用户信息异常");
        }
        user.setStatus(3); // 冻结
        user.setUpdateBy(LoginUtil.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);

        // 冻结账号后需要退出用户所有登录
        LoginUtil.logoutAll(user.getId());

        return true;
    }

    @Override
    public boolean restore(Long id) throws Exception {
        // 获取用户信息
        User user = userMapper.selectById(id);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException("用户信息异常");
        }
        // 判断用户状态是否是禁用/冻结
        if (user.getStatus() != 2 && user.getStatus() != 3 && user.getStatus() != 4) {
            throw new BusinessException("用户状态异常");
        }

        user.setStatus(1); // 正常
        user.setUpdateBy(LoginUtil.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);

        // 还需要删除因密码错误次数过多冻结的用户
        redisUtil.del(CommonConstant.PASSWORD_ERROR_NUM + user.getId());

        return true;
    }

    @Override
    public boolean updatePassword(UpdatePasswordParam updatePasswordParam) throws Exception {
        // 先验证短信验证码是否正确（支持测试码）
        validateSmsCode(updatePasswordParam.getMobile(), updatePasswordParam.getSmsCode());

        // 获取当前登录用户信息
        User user = userMapper.selectById(LoginUtil.getUserId());
        if (user == null) {
            throw new BusinessException(500, "用户信息异常");
        }
        // 判断手机号是否一致
        if (!user.getMobile().equals(updatePasswordParam.getMobile())) {
            throw new BusinessException(500, "手机号不一致");
        }

        // 获取前端传过来的密码解密成明文并校验复杂度
        String password = decryptAndValidatePassword(updatePasswordParam.getPassword());

        // 重新生成盐
        user.setSalt(SHA256Util.getSHA256Str(UUID.randomUUID().toString()));
        // HA256S加密
        user.setPassword(SHA256Util.getSHA256Str(password + user.getSalt()));
        user.setUpdateTime(new Date());
        userMapper.updateById(user);

        // 退出所有登录
        LoginUtil.logoutAll(user.getId());

        return true;
    }

    @Override
    public boolean resetPassword(ResetPasswordParam resetPasswordParam) throws Exception {
        // 获取当前登录用户信息
        User user = userMapper.selectById(resetPasswordParam.getUserId());
        if (user == null) {
            throw new BusinessException(500, "用户信息异常");
        }

        // 获取前端传过来的密码解密成明文并校验复杂度
        String password = decryptAndValidatePassword(resetPasswordParam.getPassword());

        // 重新生成盐
        user.setSalt(SHA256Util.getSHA256Str(UUID.randomUUID().toString()));
        // HA256S加密
        user.setPassword(SHA256Util.getSHA256Str(password + user.getSalt()));
        user.setUpdateTime(new Date());
        userMapper.updateById(user);

        // 退出所有登录
        LoginUtil.logoutAll(user.getId());

        return true;
    }

}
