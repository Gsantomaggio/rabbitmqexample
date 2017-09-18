import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by gabriele on 18/09/2017.
 */
public class DBManager {


    public static void main(String[] args) throws Exception {

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("RabbitMQ host (localhost):");
        String rabbitmq_host = br.readLine();
        if (rabbitmq_host.equalsIgnoreCase("")) rabbitmq_host = "localhost";


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmq_host);
        factory.setPassword("test");
        factory.setUsername("test");
        final Connection connection = factory.newConnection();


        final Channel channel = connection.createChannel();
        channel.queueDeclare(Configure.QUEUE_DATABASE, true, false, false, null);
        channel.queueBind(Configure.QUEUE_DATABASE, Configure.EXCHANGE_CHAT, "#");

        channel.basicConsume(Configure.QUEUE_DATABASE, false, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                System.out.println(" Going to insert this message:   '" + new String(body) + "'  - " + new Date());

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });

    }
}
