package test.poc;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by gabriele on 31/07/2017.
 */
public class GIS {


    public static void main(String[] args) throws Exception {


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Down Stream server ip (localhost):");
        String server_ip = br.readLine();
        if (server_ip.equalsIgnoreCase("")) {
            server_ip = "localhost";
        }


        ConnectionFactory factory_down = new ConnectionFactory();
        factory_down.setHost(server_ip);
        factory_down.setUsername("test");
        factory_down.setPassword("test");
        factory_down.setVirtualHost("poc");
        Connection connection_down = factory_down.newConnection();
        System.out.println("Connected to Down Stream node: " + server_ip);
        final Channel channel_down = connection_down.createChannel();
        final String queue = "gis";
        final String exchange = "service";

        channel_down.exchangeDeclare(exchange, "topic", true);
        channel_down.queueDeclare(queue, true, false, false, null);
        channel_down.queueBind(queue, exchange, "r1.gis", null);
        channel_down.basicQos(1);
        channel_down.basicConsume(queue, false, new DefaultConsumer(channel_down) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("Consumer  Message: " + new String(body));
                channel_down.basicAck(envelope.getDeliveryTag(), false); // Can be done in other threads
            }

        });
    }


}
