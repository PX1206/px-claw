package com.claw.system.mq;

import com.alibaba.fastjson.JSONObject;
import com.claw.common.constant.RocketMqConstant;
import com.claw.system.entity.SysOperationLog;
import com.claw.system.mapper.SysOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Sakura
 * @date 2023/10/24 16:48
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqConstant.SAVE_OPERATION_LOG_TOPIC, consumerGroup = RocketMqConstant.SAVE_OPERATION_LOG_CONSUMER_GROUP)
public class SaveOperationLogMQListener implements RocketMQListener<SysOperationLog> {

    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;

    @Override
    public void onMessage(SysOperationLog sysOperationLog) {
        log.info("MQ收到保存系统操作日志消息：" + JSONObject.toJSONString(sysOperationLog));
        sysOperationLogMapper.insert(sysOperationLog);
    }
}
