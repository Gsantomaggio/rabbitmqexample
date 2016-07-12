package com.perfmessage.thread;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ThreadPublisher implements Runnable{
	
	  private Connection connection;
	  
	  private int publish_delay=500;
	  private int interactionMessages = 1;
	  private String exName;
	  private byte[] body;
	  private String routing_key;
	  
	   public void run() {
		    try {
            	
                Channel internalChannel = connection.createChannel();
                for (int j = 0; j < interactionMessages; j++) {
                	try {
                	    Thread.sleep(publish_delay);                
                	} catch(InterruptedException ex) {
                	    Thread.currentThread().interrupt();
                	}
                    AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
                    propsBuilder.deliveryMode(2);
                   
                    internalChannel.basicPublish(exName, routing_key, propsBuilder.build(), body);

                }
                internalChannel.close();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
	   }

	public ThreadPublisher(Connection connection, int publish_delay,
			int interactionMessages, String exName, String routing_key,  byte[] body) {
		super();
		this.connection = connection;
		this.publish_delay = publish_delay;
		this.interactionMessages = interactionMessages;
		this.exName = exName;
		this.body = body;
		this.routing_key = routing_key;
	}
	
}
