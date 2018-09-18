package cardprocess.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "cardprocess")

public class CardProcess implements Serializable {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	long id;

	@Column(name = "serial")
	String serial;

	@Column(name = "pin")
	String pin;

	@Column(name = "receivetime")
	Date receivetime;

	@Column(name = "cardavailable")
	int cardavailable;

	@Column(name = "cardcheckresult")
	String cardcheckresult;

	@Column(name = "cardvalue")
	BigInteger cardvalue;

	@Column(name = "cardexpiredate")
	Date cardexpiredate;

	@Column(name = "cardprocesssuccess")
	int cardprocesssuccess;
	
	@Column(name = "cardprocessresult")
	String cardprocessresult;
		
	@Column(name = "chargedto")
	String chargedto;

	@Column(name = "chargedtime")
	String chargedtime;
	
	@Column(name = "src_msg")
	String src_msg;
	
	@Column(name = "chargedby")
	String chargedby;
	
	@Column(name = "user_aded")
	String user_aded;
	
	
	
	

	public String getChargedby() {
		return chargedby;
	}

	public void setChargedby(String chargedby) {
		this.chargedby = chargedby;
	}

	public String getUser_aded() {
		return user_aded;
	}

	public void setUser_aded(String user_aded) {
		this.user_aded = user_aded;
	}

	public String getSrc_msg() {
		return src_msg;
	}

	public void setSrc_msg(String src_msg) {
		this.src_msg = src_msg;
	}

	public String getCardcheckresult() {
		return cardcheckresult;
	}

	public void setCardcheckresult(String cardcheckresult) {
		this.cardcheckresult = cardcheckresult;
	}

	public BigInteger getCardvalue() {
		return cardvalue;
	}

	public void setCardvalue(BigInteger cardvalue) {
		this.cardvalue = cardvalue;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Date getReceivetime() {
		return receivetime;
	}

	public void setReceivetime(Date receivetime) {
		this.receivetime = receivetime;
	}

	public int getCardavailable() {
		return cardavailable;
	}

	public void setCardavailable(int cardavailable) {
		this.cardavailable = cardavailable;
	}

	public Date getCardexpiredate() {
		return cardexpiredate;
	}

	public void setCardexpiredate(Date cardexpiredate) {
		this.cardexpiredate = cardexpiredate;
	}

	public int getCardprocesssuccess() {
		return cardprocesssuccess;
	}

	public void setCardprocesssuccess(int cardprocesssuccess) {
		this.cardprocesssuccess = cardprocesssuccess;
	}

	public String getCardprocessresult() {
		return cardprocessresult;
	}

	public void setCardprocessresult(String cardprocessresult) {
		this.cardprocessresult = cardprocessresult;
	}

	public String getChargedto() {
		return chargedto;
	}

	public void setChargedto(String chargedto) {
		this.chargedto = chargedto;
	}

	public String getChargedtime() {
		return chargedtime;
	}

	public void setChargedtime(String chargedtime) {
		this.chargedtime = chargedtime;
	}

}
