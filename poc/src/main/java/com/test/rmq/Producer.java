package com.test.rmq;

/*
 * RabbitmqCookBook [TAG_TO_REPLACE]
 * 
 * 
 * Chapter 02 Recipe 01. How to let messages expire on the producer side.
 * */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Producer {

	/**
	 * @param args
	 *            [0] RabbitmqHost
	 */
	
	Connection connection;
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	
	
	public void SendResponse(String uuidrequest,String jsonResponse,String RoutingKey) {
		try 
		{
			//System.out.println("Sending back the result");
			
			Channel channel = connection.createChannel();
			AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
			Map<String, Object> header = new HashMap<String,Object> ();
			header.put("uuidrequest", uuidrequest);
			bob.headers(header);
		//	System.out.println("Request: " + guidRequest);

			channel.basicPublish(Constants.exchange, RoutingKey, bob.build(), jsonResponse.getBytes()); 		
			channel.close();
			System.out.println(new Date()+ " -Done: messages sent: " + uuidrequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
