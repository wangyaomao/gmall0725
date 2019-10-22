package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @RequestMapping("index")
    public String paymentIndex(String outTradeNo, BigDecimal totalAmount, ModelMap map){
        map.put("outTradeNo",outTradeNo);
        map.put("totalAmount",totalAmount);
        return "index";
    }

    @RequestMapping(value="alipay/submit")
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, ModelMap map){
        map.put("outTradeNo",outTradeNo);
        map.put("totalAmount",totalAmount);
        OrderInfo orderInfo = orderService.getOrderByOutTradeNo(outTradeNo);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setOrderId(orderInfo.getId());
        paymentService.savePayment(paymentInfo);

        // 对接支付宝的pagePay接口，公共参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        // 填充业务参数
        Map<String,String> aliMap = new HashMap<>();
        aliMap.put("out_trade_no",outTradeNo);
        aliMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        aliMap.put("total_amount","0.01");
        aliMap.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());

        alipayRequest.setBizContent(JSON.toJSONString(aliMap));

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(form);

        // 启动延迟检查支付状态的队列
        paymentService.sendDelayPaymentResult(paymentInfo,5);

        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String callbackReturn(HttpServletRequest request){
        // 付款成功 // 修改支付状态
        String notify_time = (String)request.getParameter("notify_time");
        String app_id = (String)request.getParameter("app_id");
        String sign = (String)request.getParameter("sign");
        String trade_no = (String)request.getParameter("trade_no");
        String out_trade_no = (String)request.getParameter("out_trade_no");
        String trade_status = (String)request.getParameter("trade_status");
        String total_amount = (String)request.getParameter("total_amount");


        // 幂等性检查
        boolean b = paymentService.checkPayStatus(out_trade_no);

        // 验证阿里的签名
        if(!b){
            try {
                Map<String ,String> testMap = new HashMap<>();
                boolean signVerified = AlipaySignature.rsaCheckV1(testMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type) ;//调用SDK验证签名
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }

            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentService.updatePayment(paymentInfo);

            // 通知订单系统，修改订单状态
            paymentService.sendPaymentSuccessQueue(paymentInfo);
        }


        return "finish";
    }
}
