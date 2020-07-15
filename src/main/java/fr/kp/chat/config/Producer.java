package fr.kp.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.*;
@Component
@Slf4j
public class Producer {
    @Value("${spring.activemq.broker-url}")
    private static String bokerUrl;
    public static void produce(){
        System.out.println(" url :");
        try{
            log.info("brokerUrl :"+bokerUrl);
            ConnectionFactory
                    connectionFactory =
                    new ActiveMQConnectionFactory("tcp://localhost:6616");
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("kp.queue");
            MessageProducer messageProducer = session.createProducer(destination);
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(" Hello ...");
            log.info("envoie message ");
            messageProducer.send(textMessage);
            session.close();
            connection.close();

        }catch (JMSException ex){

        }
    }
}
