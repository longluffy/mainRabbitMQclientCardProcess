package cardprocess.hibernate;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

public class Cardservices {

	public static SessionFactory sessionfactory;
	public Session session;

	public Cardservices() {
		Configuration configObj = new Configuration();
		configObj.configure("hibernate.cfg.xml");

		// Since Hibernate Version 4.x, ServiceRegistry Is Being Used
		ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder()
				.applySettings(configObj.getProperties()).build();

		// Creating Hibernate SessionFactory Instance
		sessionfactory = configObj.buildSessionFactory(serviceRegistryObj);
		session = sessionfactory.openSession();
	}

	public CardProcess updatecardinfo(CardProcess cardinfo) {

		session.beginTransaction();

		CardProcess cpupdate = getCardInfobyId(cardinfo.id);

		if (0 != cardinfo.getCardavailable()) {
			cpupdate.setCardavailable(cardinfo.getCardavailable());
		}
		if (null != cardinfo.getCardcheckresult()) {
			cpupdate.setCardcheckresult(cardinfo.getCardcheckresult());
		}

		if (null != cardinfo.getCardvalue() && 0L != cardinfo.getCardvalue().longValueExact()) {
			cpupdate.setCardvalue(cardinfo.getCardvalue());
		}

		if (null != cardinfo.getCardexpiredate()) {
			cpupdate.setCardexpiredate(cardinfo.getCardexpiredate());
		}

		if (0 != cardinfo.getCardprocesssuccess()) {
			cpupdate.setCardprocesssuccess(cardinfo.getCardprocesssuccess());
		}

		if (null != cardinfo.getCardprocessresult()) {
			cpupdate.setCardprocessresult(cardinfo.getCardprocessresult());
		}

		if (null != cardinfo.getChargedto()) {
			cpupdate.setChargedto(cardinfo.getChargedto());
		}

		if (null != cardinfo.getChargedby()) {
			cpupdate.setChargedby(cardinfo.getChargedby());
		}

		if (null != cardinfo.getChargedtime()) {
			cpupdate.setChargedtime(cardinfo.getChargedtime());
		}

		if (null != cardinfo.getSrc_msg()) {
			cpupdate.setSrc_msg(cardinfo.getSrc_msg());
		}

		if (null != cardinfo.getUser_aded()) {
			cpupdate.setUser_aded(cardinfo.getUser_aded());
		}

		session.update(cpupdate);
		session.getTransaction().commit();
		return cpupdate;
	}

	public void updatecardinfo(MultivaluedMap<String, String> formParams) throws ParseException {
		// {cardvalue=[500000 ?], carddate=[31/12/2024], cardserial=[10001012541160], //
		// cardstate=[Th? ch?a s? d?ng]}
		CardProcess cpinfo = new CardProcess();

		String cardstate = formParams.getFirst("cardstate");

		if (cardstate.equalsIgnoreCase("Thẻ chưa sử dụng")) {
			cpinfo.setCardavailable(1);

		} else {
			cpinfo.setCardavailable(0);
		}
		cpinfo.setSerial(formParams.getFirst("cardserial"));

		String cardvalueStr = formParams.getFirst("cardstate").split(" ")[0];
		BigInteger cardvalue = BigInteger.valueOf(Long.valueOf(cardvalueStr));
		cpinfo.setCardvalue(cardvalue);

		String datestr = formParams.getFirst("carddate");
		java.util.Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(datestr);

		cpinfo.setCardexpiredate(date1);

		updatecardinfo(cpinfo);

	}

	public void updateNapTheInfo(CardProcess cpinfo) {
		session.beginTransaction();

		CardProcess cpupdate = getCardInfoFromPin(cpinfo.getPin());
		cpupdate.setCardprocesssuccess(cpinfo.getCardprocesssuccess());
		cpupdate.setCardprocessresult(cpinfo.getCardprocessresult());
		cpupdate.setChargedtime(cpinfo.getChargedtime());

		session.saveOrUpdate(cpupdate);
		session.getTransaction().commit();
	}

	public void updateNapTheDetailCharger(CardProcess cpinfo) {
		session.beginTransaction();
		CardProcess cpupdate = getCardInfoFromPin(cpinfo.getPin());
		cpupdate.setChargedby(cpinfo.getChargedby());
		cpupdate.setChargedto(cpinfo.getChargedto());
		cpupdate.setChargedtime(cpinfo.getChargedtime());

		session.saveOrUpdate(cpupdate);
		session.getTransaction().commit();
		session.flush();
	}

	public CardProcess getCardInfoFromSerial(String serial) {
		// TODO Auto-generated method stub
		session.flush();

		Criteria cr = session.createCriteria(CardProcess.class);
		cr.add(Restrictions.eq("serial", serial));
		cr.addOrder(Order.desc("receivetime"));

		List<CardProcess> result = cr.list();

		if (result.size() != 0) {
			CardProcess cpupdate = result.get(0);
			return cpupdate;

		} else {
			return null;
		}
	}

	public CardProcess getCardInfoFromPin(String pin) {
		// TODO Auto-generated method stub
		// syncstate
		session.flush();
		Criteria cr = session.createCriteria(CardProcess.class);
		cr.add(Restrictions.eq("pin", pin));
		cr.addOrder(Order.desc("receivetime"));

		List<CardProcess> result = cr.list();

		CardProcess cpupdate = result.get(0);
		return cpupdate;
	}

	public CardProcess getCardInfobyId(long id) {
		// TODO Auto-generated method stub
		Criteria cr = session.createCriteria(CardProcess.class);
		cr.add(Restrictions.eq("id", id));

		List<CardProcess> result = cr.list();

		CardProcess cpupdate = null;
		if (result.size() != 0) {
			cpupdate = result.get(0);

		} else {
			return null;
		}
		return cpupdate;
	}

	public MyViettelAccount selectAccountForCheckcard() {

		Criteria cr = session.createCriteria(MyViettelAccount.class);
		cr.add(Restrictions.lt("checked", 3));
		cr.setMaxResults(1);
		List<MyViettelAccount> result = cr.list();

		MyViettelAccount acc = result.get(0);
		return acc;

	}

	public MyViettelAccount selectAccountForNapThe() {

		Criteria cr = session.createCriteria(MyViettelAccount.class);
		cr.add(Restrictions.lt("charged", 5));
		cr.add(Restrictions.eq("enabled", 1));

		cr.setMaxResults(1);
		List<MyViettelAccount> result = cr.list();

		MyViettelAccount acc = result.get(0);

		session.beginTransaction();
		acc.setEnabled(2);
		session.saveOrUpdate(acc);
		session.getTransaction().commit();
		session.flush();
		return acc;

	}
	
	
	public MyViettelAccount selectAccountForNapTheAndroid() {

		Criteria cr = session.createCriteria(MyViettelAccount.class);
		cr.add(Restrictions.lt("charged", 5));
		cr.add(Restrictions.eq("checked", 1));
		cr.add(Restrictions.eq("enabled", 1));

		cr.setMaxResults(1);
		List<MyViettelAccount> result = cr.list();

		MyViettelAccount acc = result.get(0);

		session.beginTransaction();
		acc.setEnabled(2);
		session.saveOrUpdate(acc);
		session.getTransaction().commit();
		session.flush();
		return acc;

	}

	public ChargeAccount selectReceiveAccount(BigInteger cardValue) {

		Criteria cr = session.createCriteria(ChargeAccount.class);
		cr.add(Restrictions.eq("completed", 0));
		cr.add(Restrictions.ge("leftAmount", cardValue));

		cr.setMaxResults(1);
		List<ChargeAccount> result = null;
		try {
			result = cr.list();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			e.printStackTrace();
		}
		ChargeAccount acc = result.get(0);
		
		session.beginTransaction();
		acc.setCompleted(2);
		session.saveOrUpdate(acc);
		session.getTransaction().commit();
		session.flush();
		return acc;

	}

	public void updateMyviettelAcc(MyViettelAccount acc) {

		session.beginTransaction();
		acc.setEnabled(1);
		session.saveOrUpdate(acc);
		session.getTransaction().commit();
		session.flush();

	}

	public void checkCountMvtaccount(MyViettelAccount mvtaccCheck) {
		session.beginTransaction();
		mvtaccCheck.setChecked(mvtaccCheck.getChecked() + 1);
		session.saveOrUpdate(mvtaccCheck);
		session.getTransaction().commit();
	}

	public void naptheCountMvtaccount(MyViettelAccount mvtaccNapthe) {
		session.beginTransaction();
		mvtaccNapthe.setChecked(mvtaccNapthe.getCharged() + 1);
		session.saveOrUpdate(mvtaccNapthe);
		session.getTransaction().commit();
	}

	public void setmaximizeCheckcount(MyViettelAccount mvtaccCheck) {
		session.beginTransaction();
		mvtaccCheck.setChecked(99);
		session.saveOrUpdate(mvtaccCheck);
		session.getTransaction().commit();
	}

	public ChargeAccount selectchargeaccount() {

		Criteria cr = session.createCriteria(ChargeAccount.class);
		cr.add(Restrictions.eq("completed", 0L));
		cr.setMaxResults(1);
		List<ChargeAccount> result = cr.list();

		ChargeAccount acc = result.get(0);
		return acc;
	}

	public CardProcess addCardProcess(CardProcess cp) {
		// TODO Auto-generated method stub
		session.beginTransaction();
		session.save(cp);
		session.getTransaction().commit();
		return cp;
	}

	public void updateAmountReceiveAccount(ChargeAccount receiveAccount, CardProcess cardSaved) {
		session.beginTransaction();
		BigInteger newcharged = receiveAccount.getChargedAmount().add(cardSaved.getCardvalue());
		receiveAccount.setChargedAmount(newcharged);
		BigInteger newleft = receiveAccount.getLeftAmount().subtract(cardSaved.getCardvalue());
		receiveAccount.setLeftAmount(newleft);

		if (receiveAccount.getAmount() == receiveAccount.getChargedAmount()) {
			receiveAccount.setCompleted(1);
		}else {
			receiveAccount.setCompleted(0);
		}
		session.saveOrUpdate(receiveAccount);
		session.getTransaction().commit();
	}

	public MyViettelAccount addMyviettelAccount(MyViettelAccount acc) {
		// TODO Auto-generated method stub
		session.beginTransaction();
		session.save(acc);
		session.getTransaction().commit();
		session.flush();
		return acc;
	}

	public ChargeAccount addChargeAccount(ChargeAccount account) {
		session.beginTransaction();
		session.save(account);
		session.getTransaction().commit();
		session.flush();
		return account;
	}

	public MyViettelAccount getmyViettelAccById(long myvtAccId) {
		Criteria cr = session.createCriteria(MyViettelAccount.class);
		cr.add(Restrictions.eq("id", myvtAccId));
		cr.setMaxResults(1);
		List<MyViettelAccount> result = cr.list();

		MyViettelAccount acc = result.get(0);
		return acc;
	}

	public ChargeAccount getChargeAccountById(int receiveAccountId) {
		Criteria cr = session.createCriteria(ChargeAccount.class);
		cr.add(Restrictions.eq("id", receiveAccountId));
		cr.setMaxResults(1);
		List<ChargeAccount> result = cr.list();

		ChargeAccount acc = result.get(0);
		return acc;
	}

	public void updateChargeAcctoTratruoc(ChargeAccount receiveAccount) {
		// TODO Auto-generated method stub
		session.beginTransaction();
		receiveAccount.setCompleted(3);
		session.saveOrUpdate(receiveAccount);
		session.getTransaction().commit();
		session.flush();
	}

	public void updateReleaseReceiveAccount(ChargeAccount receiveAccount) {
		session.beginTransaction();
		receiveAccount.setCompleted(0);
		session.save(receiveAccount);
		session.getTransaction().commit();
		session.flush();		
	}

	public CardProcess checkCardExistsOnsystem(CardProcess card) {
		session.flush();

		Criteria cr = session.createCriteria(CardProcess.class);
		cr.add(Restrictions.eq("serial", card.getSerial()));
		cr.add(Restrictions.eq("pin", card.getPin()));
		cr.add(Restrictions.eq("cardprocesssuccess", 1));
		cr.addOrder(Order.desc("receivetime"));

		List<CardProcess> result = cr.list();

		if (result.size() != 0) {
			CardProcess cpupdate = result.get(0);
			return cpupdate;

		} else {
			return null;
		}
	}

}
