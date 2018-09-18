package cardprocess.hibernate;

import java.math.BigInteger;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "chargeaccount")
public class ChargeAccount {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;
	String phonenumber;
	Date date;
	String useradded;
	BigInteger amount;
	BigInteger ChargedAmount;
	BigInteger leftAmount;
	int completed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCompleted(int completed) {
		this.completed = completed;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUseradded() {
		return useradded;
	}

	public void setUseradded(String useradded) {
		this.useradded = useradded;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	public BigInteger getChargedAmount() {
		return ChargedAmount;
	}

	public void setChargedAmount(BigInteger chargedAmount) {
		ChargedAmount = chargedAmount;
	}

	public BigInteger getLeftAmount() {
		return leftAmount;
	}

	public void setLeftAmount(BigInteger leftAmount) {
		this.leftAmount = leftAmount;
	}

}
