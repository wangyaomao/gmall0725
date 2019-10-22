package com.atguigu.gmall.payment.paymentMq;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PayStatusCheckListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        // 获取检查次数
        int count = mapMessage.getInt("count");
        String out_trade_no = mapMessage.getString("out_trade_no");

        // 检查支付状态
        System.err.println("开始检查支付状态：第"+(6-count)+"次");
        Map<String,String> success = paymentService.checkAlipayPayment(out_trade_no);
        String trade_status = success.get("trade_status");
        String trade_no = success.get("trade_no");
        String queryString = success.get("queryString");

        count --;

        // 继续发送延迟队列(1 count值大于0，2 查询到的支付状态为未支付成功)
        if(null!=trade_status&&(trade_status.equals("TRADE_SUCCESS")||trade_status.equals("TRADE_FINISHED"))){

            // 幂等性检查
            boolean b = paymentService.checkPayStatus(out_trade_no);
            if(!b){
                // 支付成功，// 更新支付信息
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setAlipayTradeNo(trade_no);
                paymentInfo.setCallbackContent(queryString);
                paymentInfo.setOutTradeNo(out_trade_no);
                paymentService.updatePayment(paymentInfo);

                // 发送支付成功的队列
                paymentService.sendPaymentSuccessQueue(paymentInfo);

            }else{
                System.out.println("支付状态已更新，不再重复更新");
            }
        }else{
            if(count>0){
                System.err.println("未支付成功，继续发送下一次的延迟检查任务，剩余次数："+count);
                PaymentInfo paymentInfo= paymentService.getPaymentByOutTradeNo(out_trade_no);
                paymentService.sendDelayPaymentResult(paymentInfo,count);
            }else{
                System.err.println("次数用尽，停止延迟检查");
            }
        }




    }



}
