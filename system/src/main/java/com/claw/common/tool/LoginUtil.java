package com.claw.common.tool;

import com.claw.common.constant.CommonConstant;
import com.claw.common.exception.BusinessException;
import com.claw.common.redis.RedisUtil;
import com.claw.common.vo.LoginUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Sakura
 * @date 2023/8/21 15:31
 */
@Slf4j
@Component
public class LoginUtil {

    private static RedisUtil redisUtil;

    public LoginUtil(RedisUtil redisUtil) {
        LoginUtil.redisUtil = redisUtil;
    }

    public static LoginUserInfoVO getLoginUserInfoVo() {
        // 先获取登录token
        String token = TokenUtil.getToken();

        // 获取登录用户权限信息
        LoginUserInfoVO loginUserInfoVo = (LoginUserInfoVO)redisUtil.get(token);
        if (loginUserInfoVo == null) {
            throw new BusinessException(500, "用户信息异常");
        }
        return loginUserInfoVo;
    }

    public static Long getUserId() {
        // 获取登录用户权限信息
        LoginUserInfoVO loginUserInfoVo = getLoginUserInfoVo();

        return loginUserInfoVo.getId();
    }

    public static String getUserNo() {
        // 获取登录用户权限信息
        LoginUserInfoVO loginUserInfoVo = getLoginUserInfoVo();

        return loginUserInfoVo.getUserNo();
    }

    public static String getMobile() {
        // 获取登录用户权限信息
        LoginUserInfoVO loginUserInfoVo = getLoginUserInfoVo();

        return loginUserInfoVo.getMobile();
    }

    public static String getUserName() {
        // 获取登录用户权限信息
        LoginUserInfoVO loginUserInfoVo = getLoginUserInfoVo();

        return loginUserInfoVo.getUsername();
    }

    /**
     * 获取当前用户昵称，用于留言/献花等展示；无昵称时回退为账号
     */
    public static String getNickname() {
        LoginUserInfoVO loginUserInfoVo = getLoginUserInfoVo();
        String nickname = loginUserInfoVo.getNickname();
        return (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : loginUserInfoVo.getUsername();
    }

    /** 是否超级管理员（角色ID=1），可查看全部数据 */
    public static boolean isAdmin() {
        LoginUserInfoVO vo = getLoginUserInfoVo();
        return "admin".equals(vo.getRole());
    }

    public static void refreshToken() {
        redisUtil.expire(TokenUtil.getToken(), CommonConstant.USER_TOKEN_VALIDITY);
        redisUtil.expire(CommonConstant.USER_TOKEN_SET + LoginUtil.getUserId(), CommonConstant.USER_TOKEN_VALIDITY);
    }

    public static void saveUserLoginToken(Long userId, String token) {
        // 先更新之前登录的token，将失效token删除
        // 目前没有找到可以自动维护token集合的方法，此办法为折中方法，临时使用
        if (redisUtil.hasKey(CommonConstant.USER_TOKEN_SET + userId)) {
            Set<Object> tokens = redisUtil.sGet(CommonConstant.USER_TOKEN_SET + userId);
            tokens.forEach(obj -> {
                // 如果当前token已失效则删除
                if (!redisUtil.hasKey(obj.toString())) {
                    redisUtil.setRemove(CommonConstant.USER_TOKEN_SET + userId, obj.toString());
                }
            });
        }
        // 记录用户登录token，当用户被冻结删除或重置密码等操作时需要清空所有设备上的登录token
        redisUtil.sSetAndTime(CommonConstant.USER_TOKEN_SET + userId, CommonConstant.USER_TOKEN_VALIDITY, token);
    }

    // 退出当前登录
    public static void logout() {
        // 先获取登录token
        String token = TokenUtil.getToken();
        if (redisUtil.hasKey(token)) {
            redisUtil.del(token);
        }
    }

    // 退出用户所有设备登录
    public static void logoutAll(Long userId) {
        // 获取当前登录用户所有登录token
        if (redisUtil.hasKey(CommonConstant.USER_TOKEN_SET + userId)) {
            Set<Object> tokens = redisUtil.sGet(CommonConstant.USER_TOKEN_SET + userId);
            tokens.forEach(obj -> {
                // 删除这些token
                if (redisUtil.hasKey(obj.toString())) {
                    redisUtil.del(obj.toString());
                }
            });

            redisUtil.del(CommonConstant.USER_TOKEN_SET + userId);
        }

    }

}
