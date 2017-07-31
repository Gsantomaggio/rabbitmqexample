package test.poc;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by gabriele on 31/07/2017.
 */
public class Events {

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
        final String exchange = "service";
        channel_down.exchangeDeclare(exchange, "topic", true);
        channel_down.basicPublish(exchange, "r1.gis", MessageProperties.PERSISTENT_BASIC, "better".getBytes());

    }


}
