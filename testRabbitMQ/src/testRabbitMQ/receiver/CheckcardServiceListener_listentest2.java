package testRabbitMQ.receiver;

import java.io.IOException;
import java.sql.Time;
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
import testRabbitMQ.sender.CheckcardServiceRequest;

public class CheckcardServiceListener_listentest2 {
	private final static String CHECKCARD_RESPONSE_QUEUE_NAME = "CheckcardService_response";
	private final static String RESPONSE_QUEUE_NAME = "robux_resultLL";
	private final static String RESPONSE_Exchange_NAME = "robux_resultLL";
	
	private static Cardservices cardService;
	private final static String PATH_TO_EXE_SELENIUM = "C:\\Users\\Longluffy\\git\\mainRabbitMQclientCardProcess\\testRabbitMQ\\tool\\phantomjs2\\phantomjs.exe";

	public static void main(String[] argv)
			throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxreceiver";
		String password = "robuxreceiver";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "192.168.1.3";

		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		if (null == cardService) {
			cardService = new Cardservices();
		}

		channel.queueDeclare(CHECKCARD_RESPONSE_QUEUE_NAME, false, false, false, null);
		
		System.out.println(" [*] Direct card Process listener is waiting for messages on "
				+ CHECKCARD_RESPONSE_QUEUE_NAME + ". To exit press CTRL+C");

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

					LoginDTO loginDto = new LoginDTO(null, null, null);
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

						loginDto.setUsername(myvtAcc.getUsername());
						loginDto.setPassword(myvtAcc.getPassword());
						loginDto.setCategory(accounttype);
						loginDto.setHasLoginLogout(true);
					} catch (Exception e) {
						// Khong co myviettel
						cardSaved.setCardprocessresult("lỗi hệ thống,ko đủ myviettel");
						responseToRobux(factory, cardSaved);
						return;
					}

					System.out.println("charge by account :" + myvtAcc.getUsername());
					NapTheDTO napTheDto = new NapTheDTO(null, null, null);
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
						napTheDto.setMaTheCao(cardSaved.getPin());
						napTheDto.setSoThueBao(receiveAccount.getPhonenumber());
						napTheDto.setServiceType(accounttype);

					} catch (Exception e) {
						// khong co tai khoan nhan
						cardSaved.setCardprocessresult("lỗi hệ thống,ko đủ tài khoản gạch");
						responseToRobux(factory, cardSaved);
						return;

					}

					cardSaved.setChargedby(loginDto.getUsername());
					cardSaved.setChargedto(napTheDto.getSoThueBao());

					// NAP
					ViettelAutoProcessor viettelAuto = new ViettelAutoProcessor(loginDto, napTheDto,
							PATH_TO_EXE_SELENIUM);
					String res = viettelAuto.execute();

					// xử lý res
					System.out.println(res);

					if (res.contains("Bạn đã nạp thẻ mệnh giá")) {
						// nap the thanh cong
						cardSaved.setCardprocesssuccess(1);
						cardSaved.setCardprocessresult("Thẻ Nạp thành công");
						cardSaved.setChargedtime(new Time(System.currentTimeMillis()).toString());
						// tăng charged++
						myvtAcc.setCharged(myvtAcc.getCharged() + 1);
						cardService.updateMyviettelAcc(myvtAcc);
						cardService.updateAmountReceiveAccount(receiveAccount, cardSaved);
					} else if (res.contains("Thuê bao cố định không được nạp thẻ")) {
						// Receive account la thue bao tra truoc
						// nap lai the cao cho charge account khac
						cardService.updateChargeAcctoTratruoc(receiveAccount);
						cardService.updateMyviettelAcc(myvtAcc);

						// nạp lại thẻ này , gửi lại lên check thẻ
						CheckcardServiceRequest checkcardServices = new CheckcardServiceRequest();
						try {
							checkcardServices.checkCard(cardSaved);
							return;

						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println("cannot recheck cards");
						}

					} else if (res.contains("Thẻ cào không hợp lệ hoặc đã được sử dụng.")) {
						cardSaved.setCardavailable(0);
						cardSaved.setCardprocesssuccess(0);
						cardSaved.setCardprocessresult(res);
					} else {
						// lỗi khác
						cardSaved.setCardprocesssuccess(0);
						cardSaved.setCardprocessresult(res);
					}

					cardService.updateMyviettelAcc(myvtAcc);
					cardService.updateReleaseReceiveAccount(receiveAccount);

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
					} else if (cardSaved.getCardcheckresult().contains("Not in GZIP format")) {
						cardSaved.setCardcheckresult("lỗi hệ thống check thẻ");
						responseToRobux(factory, cardSaved);
					}else {
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

	/**
	 * @param factory
	 * @param cardResult
	 * @throws IOException
	 */
	private static void responseToRobux(ConnectionFactory factory, CardProcess cardResult) throws IOException {
		Connection connection_result;
		try {
			ConnectionFactory factory1 = new ConnectionFactory();
			String userName = "longluffy";
			String password = "12345678";
			String virtualHost = "/";
			int portNumber = 5672;
			String hostName = "192.168.1.3";

			factory1.setUsername(userName);
			factory1.setPassword(password);
			factory1.setVirtualHost(virtualHost);
			factory1.setHost(hostName);
			factory1.setPort(portNumber);
			
			connection_result = factory1.newConnection();

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
			channel_res.exchangeDeclare(RESPONSE_Exchange_NAME, "fanout");
			
			
			//channel_res.queueDeclare(RESPONSE_QUEUE_NAME, false, false, false, null);
			channel_res.basicPublish(RESPONSE_Exchange_NAME, "", null, jsonInString.getBytes());
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
