package com.test.rmq;

import java.io.IOException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Constants.HEADER);
		String RabbitmqHost = "localhost";
		if (args.length > 0)
			RabbitmqHost = args[0];
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(RabbitmqHost);
		
		try 
		{
			Connection connection = factory.newConnection();
			System.out.println("Connected: " + RabbitmqHost);
			Consumer searchConsumer = new Consumer();
			searchConsumer.setConnection(connection);
			searchConsumer.StartConsumer();
			System.out.println("press any key to terminate");
			System.in.read();
			searchConsumer.StopConsumer();
			System.out.println("DONE" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
