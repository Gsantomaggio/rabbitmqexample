import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gabriele on 18/09/2017.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        //// TODO: 18/09/2017
        // add sender
        // private vs public using header
        // add service database
        // add log reatime


        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("RabbitMQ host (localhost):");
        String rabbitmq_host = br.readLine();
        if (rabbitmq_host.equalsIgnoreCase("")) rabbitmq_host = "localhost";


        System.out.print("User Name:");
        final String user_name = br.readLine();


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmq_host);
        factory.setPassword("test");
        factory.setUsername("test");
        final Connection connection = factory.newConnection();

        Configure.init(connection);
        Configure.bind_user_queue(connection, user_name);


        final Channel consumer_channel = connection.createChannel();

        ExecutorService pool = Executors.newFixedThreadPool(3);

        pool.submit(new Runnable() {
            public void run() {
                try {
                    consumer_channel.basicConsume(Configure.get_queue_name_private(user_name), true, new ReadMessages(consumer_channel));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        pool.submit(new Runnable() {
            public void run() {
                try {
                    consumer_channel.basicConsume(Configure.get_queue_name_public(user_name), true, new ReadMessages(consumer_channel));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {

                try {
                    Channel channel_publish = connection.createChannel();
                    String command = br.readLine();
                    while (command != "quit") {
                        System.out.print("destination (p for public):");
                        String destination = br.readLine();
                        System.out.print("Message to Send:");
                        String message_to_send = br.readLine();
                        if (destination.equalsIgnoreCase("p")) {
                            channel_publish.basicPublish(Configure.EXCHANGE_CHAT, "public", null, message_to_send.getBytes());
                        } else
                            channel_publish.basicPublish(Configure.EXCHANGE_CHAT, destination, null, message_to_send.getBytes());

                        command = br.readLine();
                    }
                    channel_publish.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }


}
