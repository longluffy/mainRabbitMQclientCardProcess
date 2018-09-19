package cardprocess.hibernate;

import com.google.gson.Gson;

public class testhibernate {

	static CardProcess cardProcess;


	public static void main(String[] args) {
		
		Cardservices cardService = new Cardservices();
		MyViettelAccount myvtAcc = cardService.selectAccountForNapThe();
		System.out.println(myvtAcc.getUsername() + "_"+ myvtAcc.getEnabled());
		
		MyViettelAccount acc = cardService.selectAccountForNapThe();
		System.out.println(acc.getUsername() + "_"+ acc.getEnabled());
		
		cardService.updateMyviettelAcc(myvtAcc);
		System.out.println(myvtAcc.getUsername() + "_"+ myvtAcc.getEnabled());
		
		cardService.updateMyviettelAcc(acc);
		System.out.println(acc.getUsername() + "_"+ acc.getEnabled());

	}
}
