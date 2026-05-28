package com.claw.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.claw.common.tool.LoginUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MybatisPlusHandler implements MetaObjectHandler {

    /**
     * 创建时间
     */
    private static final String CREATE_TIME = "createTime";

    /**
     * 修改时间
     */
    private static final String MODIFY_TIME = "updateTime";

    /**
     * 创建人
     */
    private static final String CREATE_BY = "createBy";

    /**
     * 修改人
     */
    private static final String MODIFY_BY = "updateBy";

    /**
     * 删除标识
     */
    private static final String DELETE_FLAG = "delFlag";

    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = LoginUtil.getUserId();
        setFieldValByName(CREATE_BY, userId, metaObject);
        setFieldValByName(MODIFY_BY, userId, metaObject);
        setFieldValByName(CREATE_TIME, new Date(), metaObject);
        setFieldValByName(MODIFY_TIME, new Date(), metaObject);
        setFieldValByName(DELETE_FLAG, false, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = LoginUtil.getUserId();
        setFieldValByName(MODIFY_BY, userId, metaObject);
        setFieldValByName(MODIFY_TIME, new Date(), metaObject);
    }
}
