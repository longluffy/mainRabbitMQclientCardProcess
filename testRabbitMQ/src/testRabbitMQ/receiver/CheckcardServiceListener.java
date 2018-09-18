package testRabbitMQ.receiver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.ChargeAccount;
import cardprocess.hibernate.MyViettelAccount;

public class CheckcardServiceListener {
	private final static String CHECKCARD_RESPONSE_QUEUE_NAME = "CheckcardService_response";
	private final static String RESPONSE_QUEUE_NAME = "robux_result";
	private final static String  selectedDevice = "18caac9f";
	private static ChargeAccount selectchargeaccount() {
		ChargeAccount ca = new ChargeAccount();
		//ca.setPhonenumber("01693340991");
		ca.setPhonenumber("0961005566");
		return ca;
	}

	public static void main(String[] argv)
			throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxreceiver";
		String password = "robuxreceiver";
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

		channel.queueDeclare(CHECKCARD_RESPONSE_QUEUE_NAME, false, false, false, null);
		System.out.println(" [*] 18caac9f Check card result listener is waiting for messages on " + CHECKCARD_RESPONSE_QUEUE_NAME
				+ ". To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				CardProcess cardChecked = SerializationUtils.deserialize(body);

				System.out.println(" [x] robux receiver  Received '" + cardChecked + "'");

				// xu ly ket qua the
				Cardservices cs = new Cardservices();
				CardProcess cardSaved = cs.updatecardinfo(cardChecked);

				String message = cardSaved.getSrc_msg();
				String result_msg = message + "_";

				if (cardChecked.getCardavailable() == 1) {

					ChargeAccount accountreceiver = selectchargeaccount();

					MyViettelAccount mvtaccNapthe = cs.selectAccountForNapThe();

					if (null == mvtaccNapthe) {
						cardChecked.setCardprocessresult("nạp thất bại do không đủ tài khoản nạp");
						responseToRobux(factory, cardChecked);
					} else {
						System.out.println("using charge account: " + mvtaccNapthe.getUsername() + " __ "
								+ mvtaccNapthe.getPassword());

						cardSaved.setChargedby(mvtaccNapthe.getUsername());
						cardSaved.setChargedto(accountreceiver.getPhonenumber());
						cs.updateNapTheDetailCharger(cardSaved);

						NaptheSshClient naptheClient = new NaptheSshClient();

						// process charge card
						int exitcode = naptheClient.nap(cardSaved, mvtaccNapthe, accountreceiver, selectedDevice);
						System.out.println("exit code = " + exitcode);
					}

				} else {
					// card not available
					System.out.println("card not available");
					if (null== cardSaved.getCardcheckresult() || cardSaved.getCardcheckresult().isEmpty()) {
						cardSaved.setCardcheckresult("Không thể check thẻ");
						responseToRobux(factory, cardSaved);
					}else if (cardSaved.getCardcheckresult().equalsIgnoreCase("Không có thông tin tổng đài")) {
						cardSaved.setCardcheckresult("Thẻ không hợp lệ");
						responseToRobux(factory, cardSaved);
					} else {
						responseToRobux(factory,cardSaved);
					}
				}

			}
		};
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);
		channel.basicConsume(CHECKCARD_RESPONSE_QUEUE_NAME, true, consumer);
	}

	/**
	 * @param factory
	 * @param result_msg
	 * @throws IOException
	 */
	private static void responseToRobux(ConnectionFactory factory, CardProcess result_msg) throws IOException {
		Connection connection_result;
		try {
			connection_result = factory.newConnection();
			Gson gson = new Gson();
			String jsonInString = gson.toJson(result_msg);
			
			Channel channel_res = connection_result.createChannel();
			channel_res.queueDeclare(RESPONSE_QUEUE_NAME, false, false, false, null);
			channel_res.basicPublish("", RESPONSE_QUEUE_NAME, null, jsonInString.getBytes());
			System.out.println("result sent to " + RESPONSE_QUEUE_NAME + ": " + jsonInString);
			channel_res.close();
			connection_result.close();

		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed to response to robux");
		}
	}
}
