package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void putTradeCode(String tradeCode, String userId) {

        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+userId+":tradeCode",60*30,tradeCode);

        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {

        boolean b= false;

        Jedis jedis = redisUtil.getJedis();

        String tradeCodeCache = jedis.get("user:" + userId + ":tradeCode");
        if(StringUtils.isNotBlank(tradeCodeCache)){
            if(tradeCodeCache.equals(tradeCode)){
                jedis.del("user:" + userId + ":tradeCode");
                b =  true;
            }
        }
        jedis.close();
        return b;


    }

    @Override
    public void saveOrder(OrderInfo orderInfo) {
        // 新增订单
        orderInfoMapper.insertSelective(orderInfo);
        String orderId = orderInfo.getId();
        // 新增订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }

    }

    @Override
    public void deleteCheckedCart(List<String> delList) {
        String delStr = StringUtils.join(delList, ",");
        orderInfoMapper.deleteCheckedCart(delStr);
    }

    @Override
    public OrderInfo getOrderByOutTradeNo(String outTradeNo) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfo1 = orderInfoMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo1.getId());
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        orderInfo1.setOrderDetailList(orderDetails);

        return orderInfo1;
    }

    @Override
    public void updateProcessStatus(String out_trade_no, String result,String trade_no) {

        // 更新订单的状态，流程状态，支付宝流水号
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setProcessStatus("订单已支付");
        orderInfo.setTrackingNo(trade_no);
        orderInfoMapper.updateByExampleSelective(orderInfo,example);
    }

    @Override
    public void sendOrderResult(String out_trade_no) {

        // 发送订单结果的消息通知给系统
        // 发送订单支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("ORDER_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();

            OrderInfo order = getOrderByOutTradeNo(out_trade_no);

            textMessage.setText(JSON.toJSONString(order));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
