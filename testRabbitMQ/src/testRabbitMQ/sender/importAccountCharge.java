package testRabbitMQ.sender;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Scanner;

import java.text.ParseException;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.ChargeAccount;
import cardprocess.hibernate.MyViettelAccount;

public class importAccountCharge {

	static Scanner scanner;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String pathxlsx = "D:\\chargeaccount1809.xlsx";
		File xlsxfile = new File(pathxlsx);
		FileInputStream fsIP = null;
		XSSFWorkbook wb = null;
		XSSFSheet worksheet = null;
		try {
			fsIP = new FileInputStream(xlsxfile);
			wb = new XSSFWorkbook(fsIP);
			worksheet = wb.getSheetAt(0);

		} catch (Exception e) {

		}

		Cardservices cardService = new Cardservices();

		Iterator<Row> rowIterator = worksheet.iterator();
		rowIterator.next();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			Cell cellPhone = row.getCell(1);
			Cell cellvalue = row.getCell(2);
			Cell celldaily = row.getCell(4);

			System.out.println(cellPhone.getStringCellValue());
			System.out.println(cellvalue.getNumericCellValue());
			System.out.println(celldaily.getStringCellValue());

			ChargeAccount account = new ChargeAccount();

			account.setPhonenumber(cellPhone.getStringCellValue().trim());
			account.setAmount(BigInteger.valueOf((long) cellvalue.getNumericCellValue()));
			account.setUseradded(celldaily.getStringCellValue());

			Calendar currenttime = Calendar.getInstance();
			Date sqldate = new Date((currenttime.getTime()).getTime());

			account.setDate(sqldate);
			cardService.addChargeAccount(account);
//			MyViettelAccount acc = new MyViettelAccount();
//			acc.setUsername(cellPhone.getStringCellValue().trim());
//			acc.setPassword(cellpass.getStringCellValue().trim());
//			acc.setEnabled(1);
//			
//			cardService.addMyviettelAccount(acc);
//			System.out.println(acc.getId());

		}
	}

}
