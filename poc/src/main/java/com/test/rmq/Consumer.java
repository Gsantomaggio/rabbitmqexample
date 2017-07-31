package com.test.rmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Consumer {
	
	
	
	Connection connection;
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	private Channel channel =null;
	private String consumerTag ;
	ActualConsumer consumer;
	public  void StartConsumer() {
		
		try 
		{
			channel = connection.createChannel();
			channel.basicQos(1);
			consumer = new ActualConsumer(channel);
			consumerTag = channel.basicConsume(Constants.queue, false, consumer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void StopConsumer(){
		
		try {
			channel.basicCancel(consumerTag);
			channel.close();
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
