package com.test.rmq;

import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.tools.json.JSONWriter;

public class ActualConsumer extends DefaultConsumer {



	private Map<Integer,Book> localDbEmulation; 
	public ActualConsumer(Channel channel) {
		super(channel);
		localDbEmulation = Book.buildSimpleBookList();
	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, 
			byte[] body) throws java.io.IOException {

		String bookkey = new String(body);

		//  System.out.println("input search bookkey: "+message);
		//// here you should have a threadPool, in order to execute more concurrent search
		/// we put a sleep to simulate complex operations, so we can see the load balance benefits 
		/*try {
    	 Random r = new Random();
    	 int i1=r.nextInt(4-2) + 2;
    	 System.out.println("Sleep time: "+ i1);
    	 Thread.sleep(i1*1000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}*/

		// in order to improuve the performance here you should have a thread pool to manage the message 
		Object uuidRequest=""; 
		String routingKey = "";
		String jsonResult="{}";

		try {
			uuidRequest= properties.getHeaders().get("uuidrequest"); 
			routingKey = properties.getCorrelationId();


			int idx = Integer.parseInt(bookkey);
			Book res= localDbEmulation.get(idx);
			if (res!=null){
				JSONWriter jsonWriter = new JSONWriter();
				jsonResult= jsonWriter.write(res);

			}
		} catch (Exception e) {
			jsonResult="{back-end error:"+e.getMessage()+"}";
			e.printStackTrace();
		} 

		try {
			Producer send_back =  new Producer();
			send_back.setConnection(getChannel().getConnection());
			send_back.SendResponse(uuidRequest.toString(),  jsonResult,routingKey);

		} catch (Exception e) {
			System.err.println("error publish");
			e.printStackTrace();
		}

		getChannel().basicAck(envelope.getDeliveryTag(), false);
	}




}
