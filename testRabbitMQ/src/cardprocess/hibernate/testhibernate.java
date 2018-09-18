package cardprocess.hibernate;

import com.google.gson.Gson;

public class testhibernate {

	static CardProcess cardProcess;


	public static void main(String[] args) {
		
		Cardservices cs = new Cardservices();
		
		MyViettelAccount acc = cs.selectAccountForCheckcard();
		System.out.println(acc.username + acc.checked);
		
		
		cs.checkCountMvtaccount(acc);
		//System.out.println(acc.username + acc.checked);

		
		CardProcess card = cs.getCardInfobyId(89);
		Gson gson = new Gson();
		String jsonInString = gson.toJson(card);
		
		System.out.println(jsonInString);
		
		

	}
}
