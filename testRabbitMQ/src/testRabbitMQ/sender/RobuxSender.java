package testRabbitMQ.sender;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;

public class RobuxSender {

	private final static String QUEUE_NAME = "robux_request";

	public static void main(String[] args) throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxsender";
		String password = "robuxsender";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";

		String idx = "33333";
		String pin = "915295800572464";
		String serial = "10001785731186";

		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		// message format : [serial]_[pin]
		 String message = idx+"_"+serial+"_"+pin;
		
//		String message1 ="371_10001278845119_711993204354040";
//		String message2 ="325_10001357226302_518223892571861";
//		String message3 ="495_10001387143975_919897185276217";
//		String message4 ="206_10001362629648_810057776416267";
//		String message5 ="34_10001362629648_810057776416267" ;
//		String message6 ="314_810057776416267_10001362629648";
//		String message7 ="493_10001362629648_810057776416267";
//		channel.basicPublish("", QUEUE_NAME, null, message1.getBytes());
//		System.out.println(" [x] Sent '" + message1 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message2.getBytes());
//		System.out.println(" [x] Sent '" + message2 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message3.getBytes());
//		System.out.println(" [x] Sent '" + message3 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message4.getBytes());
//		System.out.println(" [x] Sent '" + message4 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message5.getBytes());
//		System.out.println(" [x] Sent '" + message5 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message6.getBytes());
//		System.out.println(" [x] Sent '" + message6 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message7.getBytes());
//		
//		System.out.println(" [x] Sent '" + message7 + "'");

		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");
		
		channel.close();
		connection.close();
		System.exit(0);
	}

}
