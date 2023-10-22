package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @author xoo
 * @version 1.0
 * @description 订单相关的service接口
 * @date 2023/10/8 22:07
 */
public interface OrderService {

    /**
     * @description 创建商品订单
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @author xoo
     * @date 2023/10/8 22:08
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * @description 查询支付交易记录
     * @param payNo  交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @author xoo
     * @date 2023/10/10 0:29
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     * @author xoo
     * @date 2023/10/10 0:39
     */
    public PayRecordDto queryPayResult(String payNo);


    /***
    * @description 保存阿里支付状态
    * @param payStatusDto 付款状态Dto
    * @return void
    * @author xoo
    * @date 2023/10/16 4:12
    */
    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);
}
