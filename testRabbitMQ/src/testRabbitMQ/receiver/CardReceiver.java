package testRabbitMQ.receiver;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

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
import testRabbitMQ.sender.CheckcardServiceRequest;

public class CardReceiver {

	public static Cardservices cardService;

	private final static String RECEIVE_QUEUE_NAME = "robux_requestLL";
	private final static String RESPONSE_QUEUE_NAME = "robux_resultLL";
	private final static String RESPONSE_Exchange_NAME = "robux_resultLL";

	public static void main(String[] argv)
			throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "longluffy";
		String password = "12345678";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		System.out.println(
				" [*] CardReceiver Waiting for messages on " + RECEIVE_QUEUE_NAME + "  . To exit press CTRL+C");

		if (null == cardService) {
			cardService = new Cardservices();
		}
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(RECEIVE_QUEUE_NAME, false, false, false, null);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x]>>>>" + LocalDateTime.now() + " Received '" + message + "'");

				String result_msg = message;
				CardProcess cp = new CardProcess();

				try {

					// get serial and pin from request

					cp.setSrc_msg(message);
					cp.setSrc_msg(message.split("_")[0]);

					cp.setCardavailable(1);

					String serialstring = message.split("_")[1];
					cp.setSerial(serialstring);

					String pinString = message.split("_")[2];
					cp.setPin(pinString);

					// set received time
					cp.setReceivetime(new Time(System.currentTimeMillis()));

					if (serialstring.isEmpty() || serialstring.length() != 14) {
						cp.setCardavailable(0);
						cp.setCardcheckresult("serial phải chứa 14 ký tự số");
					} else {
						try {
							Long serialnum = Long.valueOf(serialstring);
						} catch (Exception e) {
							cp.setCardavailable(0);
							cp.setCardcheckresult("serial format không hợp lệ");
						}
					}

					if (pinString.isEmpty() || pinString.length() != 15) {
						cp.setCardavailable(0);
						cp.setCardcheckresult("pin phải chứa 15 ký tự số");
					} else {
						try {
							Long pinStringnum = Long.valueOf(pinString);
						} catch (Exception e) {
							cp.setCardavailable(0);
							cp.setCardcheckresult("pin format không hợp lệ");
						}
					}

					CardProcess ccp = cardService.checkCardExistsOnsystem(cp);
					if (null != ccp) {
						cp.setCardvalue(ccp.getCardvalue());
						cp.setCardavailable(0);
						cp.setCardcheckresult("thẻ cào đã được nạp trước đó trên hệ thống");
					}
					cp.setCardavailable(0);
					CardProcess cardAdded = cardService.addCardProcess(cp);

					if (null == cp.getCardcheckresult() || cp.getCardcheckresult().isEmpty()) {
						// Check Card value and availability
						CheckcardServiceRequest checkcardServices = new CheckcardServiceRequest();
						checkcardServices.checkCard(cardAdded);
					} else {
						responseToRobux(factory, cp);
					}
					System.out.println("---------------------");
				} catch (IndexOutOfBoundsException e) {
					// TODO update result to db
					e.printStackTrace();
					cp.setCardavailable(0);
					cp.setCardcheckresult("request sai dinh dang");
					responseToRobux(factory, cp);
				} catch (TimeoutException e) {
					// TODO update result to db
					System.out.println("cannot connect to checkcard services");
					cp.setCardcheckresult("lỗi hệ thống check thẻ");
					e.printStackTrace();
					responseToRobux(factory, cp);
				} catch (Exception e) {
					// TODO update result to db
					System.out.println("exceptions" + e.getMessage());
					cp.setCardcheckresult(e.getMessage());
					e.printStackTrace();
					responseToRobux(factory, cp);
				}

			}

		};
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);
		channel.basicConsume(RECEIVE_QUEUE_NAME, true, consumer);
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
			res.setCardprocesssuccess(1 == result_msg.getCardprocesssuccess() ? "success" : "failed");
			res.setDetail(1 == result_msg.getCardavailable() ? result_msg.getCardprocessresult()
					: result_msg.getCardcheckresult());

			Gson gson = new Gson();
			String jsonInString = gson.toJson(res);

			Channel channel_res = connection_result.createChannel();
//			channel_res.queueDeclare(RESPONSE_QUEUE_NAME, false, false, false, null);
//			channel_res.basicPublish("", RESPONSE_QUEUE_NAME, null, jsonInString.getBytes());

			channel_res.exchangeDeclare(RESPONSE_Exchange_NAME, "fanout");
			channel_res.basicPublish(RESPONSE_Exchange_NAME, "", null, jsonInString.getBytes());

			System.out.println("Card Invalid");
			System.out.println("result sent to " + RESPONSE_QUEUE_NAME + ": " + jsonInString);
			channel_res.close();
			connection_result.close();

		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed to response to robux");
		} finally {
			System.out.println("---------------------");
		}
	}

}
