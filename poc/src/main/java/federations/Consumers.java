package federations;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gabriele on 28/07/2017.
 */
public class Consumers {

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws Exception {


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Down Stream server ip (localhost):");
        String dowm_server_ip = br.readLine();
        if (dowm_server_ip.equalsIgnoreCase("")) {
            dowm_server_ip = "localhost";
        }


        System.out.print("Up Stream server ip (t-srv-rabbit-cent06):");
        String up_server_ip = br.readLine();
        if (up_server_ip.equalsIgnoreCase("")) {
            up_server_ip = "t-srv-rabbit-cent06";
        }


        ConnectionFactory factory_down = new ConnectionFactory();
        factory_down.setHost(dowm_server_ip);
        factory_down.setVirtualHost("federation_test_up");
        factory_down.setUsername("test");
        factory_down.setPassword("test");
        Connection connection_down = factory_down.newConnection();
        System.out.println("Connected to Down Stream node: " + dowm_server_ip);
        final Channel channel_down = connection_down.createChannel();
        final String qname = "log.store";
        channel_down.queueDeclare(qname, true, false, false, null);


        ConnectionFactory factory_up = new ConnectionFactory();
        factory_up.setHost(up_server_ip);
        factory_up.setVirtualHost("federation_test");
        factory_up.setUsername("test");
        factory_up.setPassword("test");
        Connection connection_up = factory_up.newConnection();

        System.out.println("Connected to UP Stream node: " + up_server_ip);
        final Channel channel_up = connection_up.createChannel();


        ExecutorService pool = Executors.newFixedThreadPool(3);

        pool.submit(new Runnable() {
            public void run() {
                try {
                    channel_down.basicQos(1);
                    channel_down.basicConsume(qname, false, new DefaultConsumer(channel_down) {

                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            sleep(1000);
                            System.out.println("Down stream consumer. Message: " + new String(body));
                            channel_down.basicAck(envelope.getDeliveryTag(), false); // Can be done in other threads
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                try {
                   channel_up.basicQos(1);
                    channel_up.basicConsume(qname, false, new DefaultConsumer(channel_up) {

                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            sleep(2000);
                            System.out.println("UP stream consumer. Message: " + new String(body));
                            channel_up.basicAck(envelope.getDeliveryTag(), false); // Can be done in other threads
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        sleep(3000);
        final Channel channel_up_publiher = connection_up.createChannel();
        pool.submit(new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        channel_up_publiher.basicPublish("", qname, MessageProperties.PERSISTENT_BASIC, ("UP_STREAM_MESSAGE_" + i).getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                System.out.println("Publish terminated");
            }
        });

        System.out.print("Any Key to stop");
        br.readLine();
        connection_down.close();
        connection_up.close();
        pool.shutdown();

    }


}
