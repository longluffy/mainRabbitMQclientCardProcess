package testRabbitMQ.sender;

import com.rabbitmq.client.ConnectionFactory;

import cardprocess.hibernate.CardProcess;

import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.rabbitmq.client.Channel;

public class CheckcardServiceRequest {

	private final static String QUEUE_NAME = "CheckcardService_request";

	public void checkCard(CardProcess cp) throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxsender";
		String password = "robuxsender";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";

		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		byte[] data = SerializationUtils.serialize(cp);

		channel.basicPublish("", QUEUE_NAME, null, data);
		System.out.println(" <<<<[x] Sent to check card  '" + cp.getSerial() + "'");

		channel.close();
		connection.close();
	}

}
