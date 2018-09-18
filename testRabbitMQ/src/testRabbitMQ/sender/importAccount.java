package testRabbitMQ.sender;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.MyViettelAccount;

public class importAccount {

	static Scanner scanner;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String pathxlsx = "D:\\myvtacc.xlsx";
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
			Cell cellpass = row.getCell(2);

			System.out.println(cellPhone.getStringCellValue());
			System.out.println(cellpass.getStringCellValue());
			
			MyViettelAccount acc = new MyViettelAccount();
			acc.setUsername(cellPhone.getStringCellValue().trim());
			acc.setPassword(cellpass.getStringCellValue().trim());
			acc.setEnabled(1);
			
			cardService.addMyviettelAccount(acc);
			System.out.println(acc.getId());
			
		}
	}

}
