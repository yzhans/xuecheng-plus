package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author xoo
 * @version 1.0
 * @description 订单实现类
 * @date 2023/10/8 22:10
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    XcOrdersMapper xcOrdersMapper;

    @Resource
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Resource
    XcPayRecordMapper xcPayRecordMapper;

    @Resource
    OrderServiceImpl orderService;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Value("${pay.alipay.qrcodeurl}")
    String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //添加订单信息
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //添加支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        //创建二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //二维码的url
        String url = String.format(qrcodeurl, payNo);
        //二维码base数据
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChangException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    /***
     * @description 保存订单信息
     * @param userId 用户id
     * @param addOrderDto  订单信息表
     * @return com.xuecheng.orders.model.po.XcOrders
     * @author xoo
     * @date 2023/10/8 22:22
     */
    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        //插入订单表
        //幂等性处理，同一个选课记录只能有一个订单 存在则直接返回
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (xcOrders != null) {
            return xcOrders;
        }
        //走到这里说明没有订单，创建订单类插入订单主表
        xcOrders = new XcOrders();
        //id使用雪花算法
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付状态
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());
        int insert = xcOrdersMapper.insert(xcOrders);
        if (insert <= 0) {
            XueChangException.cast("添加订单失败");
        }
        //获取订单id
        Long id = xcOrders.getId();
        //插入订单明细表
        //将前端传入的明细json转成List
        String orderDetail = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        //遍历插入订单明细表
        xcOrdersGoods.forEach(goods -> {
            goods.setOrderId(id);
            //插入表
            int insert1 = xcOrdersGoodsMapper.insert(goods);
        });
        return xcOrders;
    }

    /***
     * @description 保存支付记录
     * @param orders 订单表
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @author xoo
     * @date 2023/10/9 0:41
     */
    public XcPayRecord createPayRecord(XcOrders orders) {
        Long orderId = orders.getId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        //如果此账单不存在则不能添加支付记录
        if (xcOrders == null) {
            XueChangException.cast("订单不存在");
        }
        //账单状态，避免重复支付
        if ("601002".equals(xcOrders.getStatus())) {
            XueChangException.cast("订单已支付");
        }
        //添加支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//支付记录号，传给支付宝
        xcPayRecord.setOrderId(orderId);
        xcPayRecord.setOrderName(orders.getOrderName());
        xcPayRecord.setTotalPrice(orders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");
        xcPayRecord.setUserId(orders.getUserId());
        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if (insert <= 0) {
            XueChangException.cast("插入支付交易记录失败");
        }
        return xcPayRecord;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }

    /***
     * @description 根据业务id查询订单
     * @param businessId 选课记录表主键
     * @return com.xuecheng.orders.model.po.XcOrders
     * @author xoo
     * @date 2023/10/8 22:34
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        //调用支付宝的接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        if (payStatusDto == null) {
            XueChangException.cast("支付记录为空");
        }
        //拿到支付结果更新支付记录表和订单表的支付状态
        orderService.saveAliPayStatus(payStatusDto);

        //获取到更新完成后的订单记录
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno, payRecordDto);
        return payRecordDto;
    }


    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {
        //查询支付结果
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        //bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        //支付宝返回的信息
        String body = null;
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                XueChangException.cast("请求支付宝查询结果失败");
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChangException.cast("请求支付查询查询失败");
        }
        //转map
        Map map = JSON.parseObject(body, Map.class);
        Map<String, String> result = (Map) map.get("alipay_trade_query_response");

        //解析支付结果
        String tradeNo = result.get("trade_no");
        String tradeStatus = result.get("trade_status");
        String totalAmount = result.get("total_amount");
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);//支付宝交易单号
        payStatusDto.setTrade_no(tradeNo);
        payStatusDto.setTrade_status(tradeStatus);//交易状态
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(totalAmount);//总金额

        return payStatusDto;
    }

    /**
     * @param payStatusDto 支付结果信息
     * @return void
     * @description 保存支付宝支付结果
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            XueChangException.cast("支付记录找不到");
        }
        //拿到相关联的订单id
        Long orderId = payRecord.getOrderId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if (xcOrders == null) {
            XueChangException.cast("找不到相关的订单号");
        }
        String status = payRecord.getStatus();
        //如果数据库支付的状态已经是成功了，不再处理了
        if ("601002".equals(status)) {
            return;
        }
        //支付宝支付状态
        String tradeStatus = payStatusDto.getTrade_status();
        //判断支付宝返回的状态是否支付成功
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            //更新支付记录表的状态为支付成功
            payRecord.setStatus("601002");
            payRecord.setOutPayNo(payStatusDto.getTrade_no());
            payRecord.setOutPayChannel("Alipay");
            payRecord.setPaySuccessTime(LocalDateTime.now());
            xcPayRecordMapper.updateById(payRecord);
            // 更新订单表的状态为支付成功
            xcOrders.setStatus("600002");
            xcOrdersMapper.updateById(xcOrders);

            //将消息写到数据库
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);

            //发送消息
            notifyPayResult(mqMessage);
        }
    }


    @Override
    public void notifyPayResult(MqMessage message) {
        //1、消息体，转json
        String msg = JSON.toJSONString(message);
        //设置消息持久化
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);

    }



}
