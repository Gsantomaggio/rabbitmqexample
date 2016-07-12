package com.perfmessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.perfmessage.thread.ThreadPublisher;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Created by gabriele on 26/09/2015.
 */
public class PerfMessages {


    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static Connection getRandomConnectionList(List<Connection> list) {

        return list.get(randInt(0, list.size() - 1));


    }

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Server ip list divided by ';' (192.168.99.100):");
        String server_ip_list = br.readLine();
        if (server_ip_list.equalsIgnoreCase("")) {
        	server_ip_list = "192.168.99.100";
        } 
        final String[] servers=server_ip_list.split(";");
        
        System.out.print("server port (5672):");
        String server_port = br.readLine();
        if (server_port.equalsIgnoreCase("")) server_port = "5672";
        final int server_port_int = Integer.parseInt(server_port);
        
        System.out.print("use vHost (Test):");
        String use_vhost = br.readLine();
        if (use_vhost.equalsIgnoreCase("")) use_vhost = "Test";
        final String virtualHost = use_vhost;


        System.out.print("interactionMessages (10000):");
        String interactionMessages_s = br.readLine();
        if (interactionMessages_s.equalsIgnoreCase(""))
            interactionMessages_s = "10000";
        final int interactionMessages = Integer.parseInt(interactionMessages_s);


        System.out.print("Thread Numbers (5):");
        String threadNumbers_s = br.readLine();
        if (threadNumbers_s.equalsIgnoreCase("")) threadNumbers_s = "5";
        final int threadNumbers = Integer.parseInt(threadNumbers_s);

        System.out.print("Number Queues (10):");
        String numberQueues_s = br.readLine();
        if (numberQueues_s.equalsIgnoreCase("")) numberQueues_s = "10";
        final int numberQueues = Integer.parseInt(numberQueues_s);


        System.out.print("use Lazy (false):");
        String use_lazy_s = br.readLine();
        if (use_lazy_s.equalsIgnoreCase("")) use_lazy_s = "false";
        final boolean use_lazy = Boolean.parseBoolean(use_lazy_s);


        System.out.print("Body size (1024):");
        String body_size_s = br.readLine();
        if (body_size_s.equalsIgnoreCase("")) body_size_s = "1024";
        final byte[] body = new byte[Integer.parseInt(body_size_s)];
        
        System.out.print("Publish delay ms (500):");
        String publish_delay_ms = br.readLine();
        if (publish_delay_ms.equalsIgnoreCase("")) publish_delay_ms = "500";
        final int publish_delay = Integer.parseInt(publish_delay_ms);

        System.out.print("Consumers start delay (seconds) (30):");
        String consumers_delay_s = br.readLine();
        if (consumers_delay_s.equalsIgnoreCase("")) consumers_delay_s = "30";
        final int consumers_delay = Integer.parseInt(consumers_delay_s);
        
        System.out.printf("Server provided: %d \n", servers.length );
        for (String server_ip : servers){
        	System.out.printf("Server ip: %s, Server Port : %d \n",server_ip, server_port_int);
        }
        System.out.printf("Interaction: %d, Thread Numbers: %d, Queue Numbers: %d \n", interactionMessages, threadNumbers, numberQueues);
        System.out.printf("Messages to send: %d \n", (servers.length * interactionMessages * threadNumbers));
        System.out.printf("Each queue will have: %d \n", (interactionMessages * threadNumbers));
        System.out.printf("Body Size : %d \n", body.length);

        System.out.print("Enter to start:");
        br.readLine();
        System.out.println("starting..");
        
        Map<String, ConnectionFactory> serversConnections = new HashMap<>();
        int index=0;
        
        for(String server_ip  : servers){
        	
        	System.out.printf("Setting queues on server %s and port %d ",server_ip,server_port_int);
        	
        	
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost(server_ip);
	        factory.setPort(server_port_int);
	        factory.setVirtualHost(virtualHost);
	        factory.setUsername("adminMQ");
	        factory.setPassword("adminMQ");
	                		
	        System.out.println("Connections Done");
	        final String exName = "Topic_test";
	        final Channel channel = factory.newConnection().createChannel();
	        channel.exchangeDeclare(exName, "topic", true);
	        String binding_key = "server"+"_"+index;
	        String queue_name = "q_server_" +  index + "_n_";
	        System.out.println("binding:" + binding_key);
	        for (int i = 0; i < numberQueues; i++) {
	            System.out.println("declare queue:" + queue_name +  i);

	            if (use_lazy) {
	                Map<String, Object> argsM = new HashMap<String, Object>();
	                argsM.put("x-queue-mode", "lazy");
	                channel.queueDeclare("test_"+ index + i, true, false, false, argsM);
	            } else channel.queueDeclare(queue_name + i, true, false, false, null);


	            channel.queueBind(queue_name+i, exName, binding_key);
	        }
	        channel.close();
	        serversConnections.put(server_ip,factory);
	        index++;
	        
        }
        
        System.out.println("Queues created..");

        int totalThreadNumbers = servers.length * threadNumbers;
        final String exName = "Topic_test";
        ExecutorService threadChannels = Executors.newFixedThreadPool(totalThreadNumbers);
        System.out.println("Start publishing..");
        String routing_key;
        int k=0;
        for (String server_ip : servers){
        	routing_key = "server_"+k;
	        for (int i = 0; i < threadNumbers; i++) {
	            threadChannels.submit(new ThreadPublisher(serversConnections.get(server_ip).newConnection(), publish_delay, interactionMessages, exName,routing_key, body));
	        }
	        k++;
        }

	    System.out.println("Waiting before start consumers, Seconds:" + consumers_delay);
	    Thread.sleep(consumers_delay * 1000);
	
	    for (String server_ip : servers){
	    	int key_queue = 0;
	    	System.out.println("Preparing on server .." + server_ip);
	        for (int i = 0; i < numberQueues; i++) {
	            System.out.println("Preparing consumers .." + i);
	            Channel channel_consumer = serversConnections.get(server_ip).newConnection().createChannel();
	           
	            String queue_name = "q_server_" +  key_queue + "_n_"+i;
	            System.out.println("Connecting queue .." + queue_name);
	            channel_consumer.basicConsume(queue_name, false, new DefaultConsumer(channel_consumer) {
	                @Override
	                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
	                    String message = new String(body, "UTF-8");
	
	                    channel_consumer.basicAck(envelope.getDeliveryTag(), false);
	                    
	                    
	                }
	            });
	
	        }
	        key_queue++;
	    }

        System.out.println("Enter to stop");
        br.readLine();
 
        threadChannels.shutdown();
        threadChannels.awaitTermination(1, TimeUnit.SECONDS);
        threadChannels.shutdownNow();
    }
    
    
    
}