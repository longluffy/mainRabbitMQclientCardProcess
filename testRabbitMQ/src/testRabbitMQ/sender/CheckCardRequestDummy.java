package testRabbitMQ.sender;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.Cardservices;

public class CheckCardRequestDummy {

	public static void main(String[] args) {

		Cardservices cs = new Cardservices();
		CardProcess cp = null;
		cp = cs.getCardInfobyId(428);

		CheckcardServiceRequest checkcardServices = new CheckcardServiceRequest();
		try {
			checkcardServices.checkCard(cp);
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
