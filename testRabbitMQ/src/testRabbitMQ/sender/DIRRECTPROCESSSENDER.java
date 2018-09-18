package testRabbitMQ.sender;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;

public class DIRRECTPROCESSSENDER {

	private final static String QUEUE_NAME = "QUEUE_DIRECT_PROCESS";

	public static void main(String[] args) throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxsender";
		String password = "robuxsender";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";

		String value = "50000";
		String pin = "413965040589461";
		String serial = "10001196601533";
		String id ="136";
		
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

			String message = id + "_" + serial + "_" + pin + "_" + value;
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());			
			System.out.println(" [x] Sent '" + message + "'");
		

		channel.close();
		connection.close();
	}

}
