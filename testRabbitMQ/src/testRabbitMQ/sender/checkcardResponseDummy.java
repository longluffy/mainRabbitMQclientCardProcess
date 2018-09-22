package testRabbitMQ.sender;

import com.rabbitmq.client.ConnectionFactory;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.Cardservices;

import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.rabbitmq.client.Channel;

public class checkcardResponseDummy {

	private final static String QUEUE_NAME = "CheckcardService_response";

	public static void main(String[] args) throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxsender";
		String password = "robuxsender";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";

		String value = "100000";
		String pin = "4172055505694213";
		String serial = "1000118191521423";
		String id ="548";
		
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Cardservices cs = new Cardservices();
		CardProcess cp = null;
		cp= cs.getCardInfobyId(550);
		cp.setCardvalue(BigInteger.valueOf(10000l));
		byte[] data = SerializationUtils.serialize(cp);
		
		
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// message format : [serial]_[pin]
		// String message = "10001782520180_816915527572774";
		String message = id + "_" + serial + "_" + pin + "_" + id;
		channel.basicPublish("", QUEUE_NAME, null, data);
		System.out.println(" [x] Sent '" + message + "'");

		channel.close();
		connection.close();
		System.exit(0);
	}

}
