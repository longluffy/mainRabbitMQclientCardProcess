package testRabbitMQ.receiver;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.ChargeAccount;
import cardprocess.hibernate.MyViettelAccount;

public class testNapthe {

	private final static String CHECKCARD_RESPONSE_QUEUE_NAME = "CheckcardService_response";
	private final static String RESPONSE_QUEUE_NAME = "robux_result";

	private static ChargeAccount selectchargeaccount() {
		ChargeAccount ca = new ChargeAccount();
//		ca.setPhonenumber("01693340991");
		ca.setPhonenumber("0961005566");
		return ca;
	}

	public static void main(String[] args) {
		String result_msg = "";
		String message = "";
		Cardservices cs = new Cardservices();
		CardProcess cardSaved = cs.getCardInfoFromSerial("10001182915148");

		// TODO Auto-generated method stub
		ChargeAccount accountreceiver = cs.selectchargeaccount();

		MyViettelAccount mvtaccNapthe = cs.selectAccountForNapThe();

		if (null == mvtaccNapthe) {
			result_msg = message + "out of myviettelAccountCheck";
		} else {
			System.out
					.println("using charge account: " + mvtaccNapthe.getUsername() + "__" + mvtaccNapthe.getPassword());
		}

		cardSaved.setChargedto(accountreceiver.getPhonenumber());
		cardSaved.setChargedby(mvtaccNapthe.getUsername());
		cs.updateNapTheDetailCharger(cardSaved);

		// process charge card
		NaptheSshClient naptheClient = new NaptheSshClient();
		int exitcode = naptheClient.nap(cardSaved, mvtaccNapthe, accountreceiver,"18caac9f");
		System.out.println("exitcode = " + exitcode);
	}

}
