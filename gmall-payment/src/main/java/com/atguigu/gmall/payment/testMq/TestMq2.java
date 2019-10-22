package com.atguigu.gmall.payment.testMq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMq2 {

    public static void main(String[] args) {

        // 建立简介工厂
        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Topic topic = session.createTopic("Boss Shout");

            MessageProducer producer = session.createProducer(topic);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("为尚硅谷的伟大复兴，而努力奋斗！");

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
