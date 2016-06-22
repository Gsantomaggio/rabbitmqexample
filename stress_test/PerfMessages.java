import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        System.out.print("Server ip (10.164.22.4):");
        String server_ip = br.readLine();
        if (server_ip.equalsIgnoreCase("")) {
            server_ip = "10.164.22.4";
        }


        System.out.print("server port (5673):");
        String server_port = br.readLine();
        if (server_port.equalsIgnoreCase("")) server_port = "5673";
        final int server_port_int = Integer.parseInt(server_port);


        System.out.print("interactionMessages (10000):");
        String interactionMessages_s = br.readLine();
        if (interactionMessages_s.equalsIgnoreCase(""))
            interactionMessages_s = "10000";
        final int interactionMessages = Integer.parseInt(interactionMessages_s);


        System.out.print("Thread Numbers (5):");
        String threadNumbers_s = br.readLine();
        if (threadNumbers_s.equalsIgnoreCase("")) threadNumbers_s = "5";
        final int threadNumbers = Integer.parseInt(threadNumbers_s);

        System.out.print("Connection Numbers (5):");
        String connection_s = br.readLine();
        if (connection_s.equalsIgnoreCase("")) connection_s = "5";
        final int connections = Integer.parseInt(connection_s);


        System.out.print("Number Queues (10):");
        String numberQueues_s = br.readLine();
        if (numberQueues_s.equalsIgnoreCase("")) numberQueues_s = "10";
        final int numberQueues = Integer.parseInt(numberQueues_s);


        System.out.print("Messages For Transaction (5):");
        String messagesForTransaction_s = br.readLine();
        if (messagesForTransaction_s.equalsIgnoreCase(""))
            messagesForTransaction_s = "5";
        final int messagesForTransaction = Integer.parseInt(messagesForTransaction_s);


        System.out.print("use transaction (true):");
        String use_tx_s = br.readLine();
        if (use_tx_s.equalsIgnoreCase("")) use_tx_s = "true";
        final boolean use_tx = Boolean.parseBoolean(use_tx_s);


        System.out.print("use Lazy (false):");
        String use_lazy_s = br.readLine();
        if (use_lazy_s.equalsIgnoreCase("")) use_lazy_s = "false";
        final boolean use_lazy = Boolean.parseBoolean(use_lazy_s);


        System.out.print("Body size (1024):");
        String body_size_s = br.readLine();
        if (body_size_s.equalsIgnoreCase("")) body_size_s = "1024";
        final byte[] body = new byte[Integer.parseInt(body_size_s)];


        System.out.print("Consumers start delay (seconds) (30):");
        String consumers_delay_s = br.readLine();
        if (consumers_delay_s.equalsIgnoreCase("")) consumers_delay_s = "30";
        final int consumers_delay = Integer.parseInt(consumers_delay_s);


        System.out.printf("Server ip: %s, Server Port : %d \n", server_ip, server_port_int);
        System.out.printf("Interaction: %d, Thread Numbers: %d, Queue Numbers: %d, messages for transaction: %d, use tx: %s \n", interactionMessages, threadNumbers, numberQueues, messagesForTransaction, use_tx);
        System.out.printf("Messages to send: %d \n", (interactionMessages * threadNumbers * messagesForTransaction));
        System.out.printf("Body Size : %d \n", body.length);

        System.out.print("Enter to start:");
        br.readLine();


        System.out.println("starting..");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(server_ip);
        factory.setPort(server_port_int);
        factory.setUsername("test");
        factory.setPassword("test");
        final List<Connection> listConnections = new ArrayList<Connection>();

        for (int i = 0; i < connections; i++) {
            listConnections.add(factory.newConnection());
            System.out.println("Connection done " + i);


        }


        System.out.println("Connections Done");
        final String exName = "Topic_test";
        final Channel channel = getRandomConnectionList(listConnections).createChannel();
        channel.exchangeDeclare(exName, "topic", true);
        for (int i = 0; i < numberQueues; i++) {
            System.out.println("declare queue:" + "test_" + i);

            if (use_lazy) {
                Map<String, Object> argsM = new HashMap<String, Object>();
                argsM.put("x-queue-mode", "lazy");
                channel.queueDeclare("test_" + i, true, false, false, argsM);
            } else channel.queueDeclare("test_" + i, true, false, false, null);


            channel.queueBind("test_" + i, exName, "#");
        }

        System.out.println("Queues created..");


        final AtomicInteger atomicInteger = new AtomicInteger();
        final AtomicInteger totalmessages = new AtomicInteger();
        ExecutorService threadChannels = Executors.newFixedThreadPool(threadNumbers + numberQueues);
        final Date dThread = new Date();
        System.out.println("Start publishing..");
        for (int i = 0; i < threadNumbers; i++) {
            threadChannels.submit(new Runnable() {
                public void run() {
                    try {

                        Channel internalChannel = getRandomConnectionList(listConnections).createChannel();
                        if (use_tx) internalChannel.txSelect();
                        for (int j = 0; j < interactionMessages; j++) {

                            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
                            propsBuilder.deliveryMode(2);

                            int msg_for_transaction = randInt(1, 10);

                            for (int k = 0; k < msg_for_transaction; k++) {
                                internalChannel.basicPublish(exName, "", propsBuilder.build(), body);
                                totalmessages.addAndGet(1);
                            }
                            if (use_tx) internalChannel.txCommit();


                            if (atomicInteger.addAndGet(1) == (interactionMessages * threadNumbers)) {
                                Date d2 = new Date();
                                long seconds = (d2.getTime() - dThread.getTime()) / 1000;
                                System.out.println("**************************************************************");
                                System.out.println("" + new Date());
                                System.out.printf("Interaction: %d, Thread Numbers: %d, Queue Numbers: %d, messages for transaction: %d, use tx: %s \n", interactionMessages, threadNumbers, numberQueues, messagesForTransaction, use_tx);
                                System.out.println("Seconds: " + seconds + ", Total Messages sent:" + (totalmessages.get()) + ", Message size:" + body.length);
                                System.out.println("**************************************************************");

                            }

                            if ((totalmessages.get() % 1000) == 0) {
                                System.out.printf("Sent: %d\n", (totalmessages.get()));
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });
        }


        System.out.println("Waiting before start consumers, Seconds:" + consumers_delay);
        Thread.sleep(consumers_delay * 1000);


        for (int i = 0; i < numberQueues; i++) {
            System.out.println("Preparing consumers .." + i);
            final Channel channel_consumer = getRandomConnectionList(listConnections).createChannel();
            channel_consumer.basicConsume("test_" + i, false, new DefaultConsumer(channel_consumer) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");

                    channel_consumer.basicAck(envelope.getDeliveryTag(), false);
                }
            });

        }

        System.out.println("Enter to stop");
        br.readLine();

        threadChannels.shutdown();
        threadChannels.awaitTermination(80, TimeUnit.SECONDS);

        for (Connection listConnection : listConnections) {
            listConnection.close();
        }

    }
}
