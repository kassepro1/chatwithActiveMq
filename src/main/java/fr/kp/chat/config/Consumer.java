package fr.kp.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
@Slf4j
public class Consumer {
    @Value("${spring.activemq.broker-url}")
   private static  String bokerUrl;

    public static void consume() {
        try {
            //Creation of the connection
            ConnectionFactory connectionFactory
                    = new ActiveMQConnectionFactory("tcp://localhost:6616");
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("kp.queue");
            MessageConsumer messageConsumer = session.createConsumer(destination);
            messageConsumer.setMessageListener(
                    new MessageListener() {
                        @Override
                        public void onMessage(Message message) {
                            if (message instanceof TextMessage) {
                                try {
                                    TextMessage textMessage = (TextMessage) message;
                                    log.info("Message =" + textMessage.getText());
                                } catch (JMSException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
        } catch (JMSException ex) {

        }
    }
}
