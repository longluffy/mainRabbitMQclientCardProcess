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
import cardprocess.hibernate.CardresponseRobux;
import cardprocess.hibernate.Cardservices;

public class CheckcardServiceListener_DirectProcess {
	private final static String CHECKCARD_RESPONSE_QUEUE_NAME = "CheckcardService_response";
	private final static String RESPONSE_QUEUE_NAME = "robux_result";
	private final static String DIRECT_QUEUE_NAME = "QUEUE_DIRECT_PROCESS";

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
		System.out.println(" [*] Direct card Process listener is waiting for messages on "
				+ CHECKCARD_RESPONSE_QUEUE_NAME + ". To exit press CTRL+C");
		Cardservices cs = new Cardservices();

		
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				CardProcess cardChecked = SerializationUtils.deserialize(body);

				CardProcess cardSaved = cs.updatecardinfo(cardChecked);
				System.out.println(" [x] checkcard listener Received card id :" + cardChecked.getId());

				// xu ly ket qua the

				if (cardChecked.getCardavailable() == 1) {
					// the hop le
					try {
						sendtoDirectProcess(factory, cardChecked);
						System.out.println("card sent to direct");
						Thread.sleep(30000);

						cardSaved.setCardprocesssuccess(1);
						cardSaved.setCardprocessresult("Thẻ Nạp thành công direct"); 
						
						

						responseToRobux(factory, cardSaved);

					} catch (TimeoutException e) {
						cardSaved.setCardprocessresult("Không thể nạp thẻ");
						System.out.println("lỗi kết nối đến direct queue");
						cardSaved.setCardprocesssuccess(0);
						responseToRobux(factory, cardSaved);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						cardSaved.setCardprocessresult("Không thể nạp thẻ");
						cardSaved.setCardprocesssuccess(0);
						responseToRobux(factory, cardSaved);
						e.printStackTrace();
					}finally {
						cs.updateNapTheInfo(cardSaved);
					}

				} else {
					// the khong hop le
					System.out.println("card not available");
					if (null == cardSaved.getCardcheckresult() || cardSaved.getCardcheckresult().isEmpty()) {
						cardSaved.setCardcheckresult("Không thể check thẻ");
						responseToRobux(factory, cardSaved);
					} else if (cardSaved.getCardcheckresult().equalsIgnoreCase("Không có thông tin tổng đài")) {
						cardSaved.setCardcheckresult("Thẻ không hợp lệ");
						responseToRobux(factory, cardSaved);
					} else {
						responseToRobux(factory, cardSaved);
					}
				}

			}

		};
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);
		channel.basicConsume(CHECKCARD_RESPONSE_QUEUE_NAME, true, consumer);
	}

	private static void sendtoDirectProcess(ConnectionFactory factory, CardProcess cardChecked)
			throws IOException, TimeoutException {
		Connection connection_directprocess = factory.newConnection();

		String message = cardChecked.getId() + "_" + cardChecked.getSerial() + "_" + cardChecked.getPin() + "_"
				+ cardChecked.getCardvalue();

		Channel channel_res = connection_directprocess.createChannel();
		channel_res.queueDeclare(DIRECT_QUEUE_NAME, false, false, false, null);
		channel_res.basicPublish("", DIRECT_QUEUE_NAME, null, message.getBytes());
		System.out.println("result sent to " + DIRECT_QUEUE_NAME + ": " + message);
		channel_res.close();
		connection_directprocess.close();
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
			
			CardresponseRobux res = new CardresponseRobux();
			res.setId(result_msg.getSrc_msg());
			res.setSerial(result_msg.getSerial());
			res.setCardvalue(result_msg.getCardvalue());
			res.setCardprocesssuccess(1==result_msg.getCardprocesssuccess()?"success":"failed");
			res.setDetail(1==result_msg.getCardavailable()?result_msg.getCardprocessresult():result_msg.getCardcheckresult());
			
			Gson gson = new Gson();
			String jsonInString = gson.toJson(res);

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
