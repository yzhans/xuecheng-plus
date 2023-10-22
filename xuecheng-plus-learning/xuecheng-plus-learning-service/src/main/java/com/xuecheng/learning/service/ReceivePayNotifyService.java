package com.xuecheng.learning.service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

/**
 * @author xoo
 * @version 1.0
 * @description 接受mq消息处理接口
 * @date 2023/10/22 5:39
 */
public interface ReceivePayNotifyService {

    public void receive(Message message);

}
