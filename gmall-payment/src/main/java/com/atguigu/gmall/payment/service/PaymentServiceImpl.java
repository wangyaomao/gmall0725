package com.atguigu.gmall.payment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo,e);

    }

    /***
     * 提交支付的幂等性检查
     * @param out_trade_no
     * @return
     */
    @Override
    public boolean checkPayStatus(String out_trade_no) {
        boolean b = true;

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);

        if(null!=paymentInfo1&&paymentInfo1.getPaymentStatus().equals("未支付")){
            b = false;
        }

        return b;
    }

    @Override
    public void sendPaymentSuccessQueue(PaymentInfo paymentInfo) {
        // 发送支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOutTradeNo());
            mapMessage.setString("trade_no",paymentInfo.getAlipayTradeNo());
            mapMessage.setString("result",paymentInfo.getPaymentStatus());

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDelayPaymentResult(PaymentInfo paymentInfo,int count) {

        // 发送延迟支付的消息队列
        // 发送支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOutTradeNo());
            mapMessage.setInt("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Map<String,String> checkAlipayPayment(String out_trade_no) {
        Map<String,String> returnMap = new HashMap<>();
        System.out.println("调用检查支付接口");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);

        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            System.out.println("调用成功");
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();
            String queryString = response.toString();
            returnMap.put("trade_status",tradeStatus);
            returnMap.put("trade_no",tradeNo);
            returnMap.put("queryString",queryString);
        } else {
            System.out.println("调用失败");
        }
        return returnMap;
    }

    @Override
    public PaymentInfo getPaymentByOutTradeNo(String out_trade_no) {

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);

        return paymentInfo1;
    }
}
