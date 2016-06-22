package test.messagging;


import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by GaS on 21/02/16.
 */
public class StoreServer {

    private String ip;
    private int port;
    private String user;
    private String password;
    private String virtual_host;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtual_host() {
        return virtual_host;
    }

    public void setVirtual_host(String virtual_host) {
        this.virtual_host = virtual_host;
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection connection;


    public void init_connection() {


        ConnectionFactory factory = new ConnectionFactory();

        factory.setPassword(this.getPassword());
        factory.setUsername(this.getUser());
        factory.setHost(this.getIp());
        factory.setVirtualHost(this.getVirtual_host());
        try {
            this.connection = factory.newConnection();
            System.out.println("Connection done: " + this.getIp());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }

    public void start_server() throws Exception {
        final String exchange_name = "my_company";
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange_name, "topic", true);

        final String store_messages_queue = "store.messages";
        channel.queueDeclare(store_messages_queue, true, false, false, null);
        channel.queueBind(store_messages_queue, exchange_name, "#");
        channel.basicQos(1);
        final AtomicInteger totalmessages  = new AtomicInteger();

        channel.basicConsume(store_messages_queue, new DefaultConsumer(channel) {

            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");

                System.out.println("processing message '" + message + "'..." + envelope.getRoutingKey() );
                try {
                    Random random = new Random();
                    int seconds = random.nextInt(10 - 5 + 1) + 5;
                    System.out.print(" waiting ");

                    for (int i = 0; i <seconds ; i++) {
                        System.out.print(".");
                        Thread.sleep(1000);

                    }

                    System.out.println("Insert for '" + message + "' done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Precessed: "+ totalmessages.incrementAndGet());
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }


            }


        });

    }


    public static void main(String[] argv) throws Exception {
        StoreServer storeServer = new StoreServer();
        storeServer.setIp(argv[0]);
        storeServer.setPort(Integer.parseInt(argv[1]));
        storeServer.setUser(argv[2]);
        storeServer.setPassword(argv[3]);
        storeServer.setVirtual_host(argv[4]);
        System.out.println("Starting server..");
        storeServer.init_connection();
        storeServer.start_server();
        System.out.println("Server started yea!");
    }

}
