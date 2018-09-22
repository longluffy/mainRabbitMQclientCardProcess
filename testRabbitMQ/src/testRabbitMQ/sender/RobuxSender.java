package testRabbitMQ.sender;

import com.rabbitmq.client.ConnectionFactory;

import cardprocess.hibernate.Cardservices;
import cardprocess.hibernate.MyViettelAccount;

import com.rabbitmq.client.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.rabbitmq.client.Channel;

public class RobuxSender {

	private final static String QUEUE_NAME = "robux_request";
	private final static String QUEUE_NAME_result = "robux_result";

	public static void main(String[] args) throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		String userName = "robuxsender";
		String password = "robuxsender";
		String virtualHost = "/";
		int portNumber = 5672;
		String hostName = "27.72.30.109";

		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		// message format : [serial]_[pin]

//		String pathxlsx = "D:\\4thenap10k19092018.xlsx";
//		File xlsxfile = new File(pathxlsx);
//		FileInputStream fsIP = null;
//		XSSFWorkbook wb = null;
//		XSSFSheet worksheet = null;
//		try {
//			fsIP = new FileInputStream(xlsxfile);
//			wb = new XSSFWorkbook(fsIP);
//			worksheet = wb.getSheetAt(0);
//
//		} catch (Exception e) {
//
//		}

//		for (int i = 2; i < 5; i++) {
//			Row row = worksheet.getRow(i);
//
//			Cell cellpin = row.getCell(1);
//			Cell cellseri = row.getCell(2);
//
//			System.out.println(cellpin.getStringCellValue());
//			System.out.println(cellseri.getStringCellValue());
//			String message = idx + "_" + cellseri.getStringCellValue() + "_" + cellpin.getStringCellValue();
//
//			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//			System.out.println(" [x] Sent '" + message + "'");
//
//		}

		String idx = "33333";
		String pin = "517017400064199";
		String serial = "10000995914628";

		String message1 = idx + "_" + serial + "_" + pin;
		channel.basicPublish("", QUEUE_NAME, null, message1.getBytes());
		System.out.println(" [x] Sent '" + message1 + "'");

//		String message2 ="325_10001357226302_518223892571861";
//		String message3 ="495_10001387143975_919897185276217";
//		String message4 ="206_10001362629648_810057776416267";
//		String message5 ="34_10001362629648_810057776416267" ;
//		String message6 ="314_810057776416267_10001362629648";
//		String message7 ="493_10001362629648_810057776416267";

//
//		channel.basicPublish("", QUEUE_NAME, null, message2.getBytes());
//		System.out.println(" [x] Sent '" + message2 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message3.getBytes());
//		System.out.println(" [x] Sent '" + message3 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message4.getBytes());
//		System.out.println(" [x] Sent '" + message4 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message5.getBytes());
//		System.out.println(" [x] Sent '" + message5 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message6.getBytes());
//		System.out.println(" [x] Sent '" + message6 + "'");
//
//		channel.basicPublish("", QUEUE_NAME, null, message7.getBytes());
//		
//		System.out.println(" [x] Sent '" + message7 + "'");

		channel.close();
		connection.close();
		System.exit(0);
	}

}
