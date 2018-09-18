package cardprocess.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")



public class CardresponseRobux implements Serializable {

	String id;

	String serial;

	BigInteger cardvalue;

	String cardprocesssuccess;

	String detail;
	
	
	
	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public BigInteger getCardvalue() {
		return cardvalue;
	}

	public void setCardvalue(BigInteger cardvalue) {
		this.cardvalue = cardvalue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getCardprocesssuccess() {
		return cardprocesssuccess;
	}

	public void setCardprocesssuccess(String cardprocesssuccess) {
		this.cardprocesssuccess = cardprocesssuccess;
	}

}
