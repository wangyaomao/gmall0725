package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface OrderService {
    void putTradeCode(String tradeCode, String userId);

    boolean checkTradeCode(String userId, String tradeCode);

    void saveOrder(OrderInfo orderInfo);

    void deleteCheckedCart(List<String> delList);

    OrderInfo getOrderByOutTradeNo(String outTradeNo);

    void updateProcessStatus(String out_trade_no, String result,String trade_no);

    void sendOrderResult(String out_trade_no);
}
