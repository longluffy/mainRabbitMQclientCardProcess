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
import com.vt.ViettelAutoProcessor;
import com.vt.dto.LoginDTO;
import com.vt.dto.LoginMethodEnum;
import com.vt.dto.NapTheDTO;
import com.vt.dto.NapTheMethodEnum;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.CardresponseRobux;
import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.ChargeAccount;
import cardprocess.hibernate.MyViettelAccount;

public class CheckcardServiceListener_listentest {
	private final static String CHECKCARD_RESPONSE_QUEUE_NAME = "CheckcardService_response";
	private final static String RESPONSE_QUEUE_NAME = "robux_result_Res";
	private static Cardservices cardService;

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

		if (null == cardService) {
			cardService = new Cardservices();
		}

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				CardProcess cardChecked = SerializationUtils.deserialize(body);
				CardProcess cardSaved = cardService.updatecardinfo(cardChecked);
				System.out.println(" [x] Card Checked Received, card id :" + cardSaved.getId());

				// xu ly ket qua check the
				if (cardSaved.getCardavailable() == 1) {
					// the hop le
					LoginDTO loginDto = null;
					MyViettelAccount myvtAcc = null;
					try {
						myvtAcc = cardService.selectAccountForNapThe();

						if (null == myvtAcc) {
							throw new Exception("noMyviettelAccount");
						}

						LoginMethodEnum accounttype;
						if (myvtAcc.getUsername().contains("_")) {
							accounttype = LoginMethodEnum.ADSL_FTTH_NEXTTV;
						} else {
							accounttype = LoginMethodEnum.MOBILE_HOMEPHONE_DCOM;
						}

						loginDto = new LoginDTO(myvtAcc.getUsername(), myvtAcc.getPassword(), accounttype);
					} catch (Exception e) {
						// Khong co myviettel
						cardSaved.setCardprocessresult("lỗi hệ thống,ko đủ myviettel");
						responseToRobux(factory, cardSaved);
						return;
					}

					System.out.println("charge by account :" + myvtAcc.getUsername());
					NapTheDTO napTheDto = null;
					ChargeAccount receiveAccount = null;
					try {
						receiveAccount = cardService.selectReceiveAccount(cardSaved.getCardvalue());
						if (null == receiveAccount) {
							throw new Exception("noReceiveAccount");
						}
						NapTheMethodEnum accounttype;
						if (receiveAccount.getPhonenumber().contains("_")) {
							accounttype = NapTheMethodEnum.ADSL_FTTH_NEXTTV;
						} else {
							accounttype = NapTheMethodEnum.DIDONG_DCOM;
						}
						napTheDto = new NapTheDTO(NapTheMethodEnum.DIDONG_DCOM, receiveAccount.getPhonenumber(),
								cardSaved.getPin());
					} catch (Exception e) {
						// khong co tai khoan nhan
						cardSaved.setCardprocessresult("lỗi hệ thống,ko đủ tài khoản gạch");
						responseToRobux(factory, cardSaved);
						return;

					}

					cardSaved.setChargedby(loginDto.getUsername());
					cardSaved.setChargedto(napTheDto.getSoThueBao());

					// Napthe
					String res = ViettelAutoProcessor.execute(loginDto, napTheDto);

					System.out.println(res);

					if (res.contains("Bạn đã nạp thẻ mệnh giá")) {
						// nap the thanh cong
						cardSaved.setCardprocesssuccess(1);
						cardSaved.setCardprocessresult("Thẻ Nạp thành công");

						// tăng charged++
						cardService.updateReceiveAccount(receiveAccount, cardSaved);

						myvtAcc.setCharged(myvtAcc.getCharged() + 1);
						cardService.updateMyviettelAcc(myvtAcc);
					} else if (res.contains("Thuê bao cố định không được nạp thẻ")) {
						// nap lai the cao
					} else {
						cardSaved.setCardprocesssuccess(0);
						cardSaved.setCardprocessresult(res);
					}

					responseToRobux(factory, cardSaved);

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

				cardService.updatecardinfo(cardSaved);

			}

		};
		int prefetchCount = 1;
		channel.basicQos(prefetchCount);
		channel.basicConsume(CHECKCARD_RESPONSE_QUEUE_NAME, true, consumer);
	}

	protected static NapTheDTO selectReceiveAccount(CardProcess cardSaved) throws Exception {

		ChargeAccount receiveAccount = cardService.selectReceiveAccount(cardSaved.getCardvalue());
		if (null == receiveAccount) {
			throw new Exception("noReceiveAccount");
		}
		NapTheMethodEnum accounttype;
		if (receiveAccount.getPhonenumber().contains("_")) {
			accounttype = NapTheMethodEnum.ADSL_FTTH_NEXTTV;
		} else {
			accounttype = NapTheMethodEnum.DIDONG_DCOM;
		}
		NapTheDTO napthe = new NapTheDTO(NapTheMethodEnum.DIDONG_DCOM, receiveAccount.getPhonenumber(),
				cardSaved.getPin());
		return napthe;
	}

	protected static LoginDTO selectMyviettelAccount() throws Exception {
		MyViettelAccount myvtAcc = cardService.selectAccountForNapThe();

		if (null == myvtAcc) {
			throw new Exception("noMyviettelAccount");
		}

		LoginMethodEnum accounttype;
		if (myvtAcc.getUsername().contains("_")) {
			accounttype = LoginMethodEnum.ADSL_FTTH_NEXTTV;
		} else {
			accounttype = LoginMethodEnum.MOBILE_HOMEPHONE_DCOM;
		}

		return new LoginDTO(myvtAcc.getUsername(), myvtAcc.getPassword(), accounttype);
	}

	/**
	 * @param factory
	 * @param cardResult
	 * @throws IOException
	 */
	private static void responseToRobux(ConnectionFactory factory, CardProcess cardResult) throws IOException {
		Connection connection_result;
		try {
			connection_result = factory.newConnection();

			CardresponseRobux res = new CardresponseRobux();
			res.setId(cardResult.getSrc_msg());
			res.setSerial(cardResult.getSerial());
			res.setCardvalue(cardResult.getCardvalue());
			res.setCardprocesssuccess(1 == cardResult.getCardprocesssuccess() ? "success" : "failed");
			res.setDetail(1 == cardResult.getCardavailable() ? cardResult.getCardprocessresult()
					: cardResult.getCardcheckresult());

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
		} finally {
			// save object card saved
			cardService.updatecardinfo(cardResult);

		}
	}
}
