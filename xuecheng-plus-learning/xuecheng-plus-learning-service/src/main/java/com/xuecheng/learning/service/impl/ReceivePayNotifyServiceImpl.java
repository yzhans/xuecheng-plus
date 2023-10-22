package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.ReceivePayNotifyService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xoo
 * @version 1.0
 * @description 接受mq消息处理实现类
 * @date 2023/10/22 5:40
 */
@Slf4j
@Service
public class ReceivePayNotifyServiceImpl implements ReceivePayNotifyService {

    @Resource
    MyCourseTablesService myCourseTablesService;

    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    @Override
    public void receive(Message message) {
        //设置延时
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //解析数据
        byte[] body = message.getBody();
        String jsonString = new String(body);
        //转换对象
        MqMessage mqMessage = JSON.parseObject(jsonString, MqMessage.class);
        //获取选课记录id and 订单类型
        String chooseCourseId = mqMessage.getBusinessKey1();
        String orderType = mqMessage.getBusinessKey2();
        //判断是否为付费课程，学习中心服务只要购买课程类的支付订单的结果
        if ("60201".equals(orderType)) {
            //根据消息内容，更新选课记录、向我的课程表插入记录
            boolean b = myCourseTablesService.saveChooseCourseSuccess(chooseCourseId);
            if (!b) {
                XueChangException.cast("保证选课记录状态失败");
            }
        }
    }
}
