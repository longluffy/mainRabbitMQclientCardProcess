package testRabbitMQ.receiver;

import com.jcraft.jsch.*;

import cardprocess.hibernate.CardProcess;
import cardprocess.hibernate.ChargeAccount;
import cardprocess.hibernate.MyViettelAccount;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * @author World
 */
public class NaptheSshClient {

	static String user = "longluffy";
	static String password = "12345678";
	static String host = "192.168.1.2";
	static int port = 22;

	public int nap(CardProcess cp, MyViettelAccount mvtacc, ChargeAccount receiveaccount , String rundevice) {

		String str_scriptFileName = "/home/longluffy/calabash/features/naptheauto.feature";

		String phone = mvtacc.getUsername();
		String myviettelpassword = mvtacc.getPassword();
		String cardpin = String.valueOf(cp.getPin());

		String phonetocharge = receiveaccount.getPhonenumber();
		String loginandcheckcardscript = String.format("Feature: Nap the feature\r\n" + "\r\n" + "  \r\n"
				+ " Scenario: Napthe\r\n" + "\r\n" + " 	When I press view with id \"n_btn_login\"\r\n" + "\r\n"
				+ " 	Then I clear input field with id \"edtPhone\"\r\n" + "\r\n"
				+ "    Given I enter \"%s\" into username field\r\n" + "    \r\n"
				+ "    Then I enter \"%s\" into password field\r\n" + "\r\n"
				+ "    Then I press view with id \"btnLogin\"\r\n" + "\r\n" + "\r\n"
				+ "	When I press vtview with id \"home_new_nap_tien\"\r\n"
				+ "    Then I press view with id \"home_new_nap_tien\"\r\n" + "\r\n" + "\r\n"
				+ "	Then I press view with id \"n_napthecao\"\r\n" + "\r\n" + "    Then take picture\r\n" + "\r\n"
				+ "    Given I enter \"%s\" into mathecao field\r\n" + "\r\n" + "    Then hide the softkeyboard\r\n"
				+ "\r\n" + "    Then I clear input field with id \"n_phone\"\r\n" + "     \r\n"
				+ "    Given I enter \"%s\" into phone field\r\n" + "\r\n" + "    Then hide the softkeyboard\r\n"
				+ "	\r\n" + "	Given I enter captcha value into captcha napthe text box\r\n" + "\r\n"
				+ "	Then hide the softkeyboard\r\n" + "\r\n" + "	Then I press view with id \"n_btn_confirm\"\r\n"
				+ "	\r\n" + "	Then I wait for 8 seconds\r\n" + "\r\n"
				+ "	Then I send napthe information to the card server\r\n"  + "\r\n"
				+ "	Then I should see \"Nạp tiền\"", phone, myviettelpassword, cardpin, phonetocharge);

		String command = " export ANDROID_HOME=/home/longluffy/Android/Sdk    && export PATH=$PATH:$ANDROID_HOME/tools  && export PATH=$PATH:$ANDROID_HOME/platform-tools  && cd calabash &&"
				//+ " ADB_DEVICE_ARG=9927283f "
				+ " ADB_DEVICE_ARG="+ rundevice 
				+ " bundle exec calabash-android run myviettel.apk  features/naptheauto.feature";

		try {

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();

			WriteFileToLinux(session, loginandcheckcardscript, str_scriptFileName);
			System.out.println("Connected");

			int exitcode = sendcommandtosv(command, session);
			session.disconnect();
			System.out.println("DONE");
			return exitcode;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}

	}

	static void WriteFileToLinux(Session obj_Session, String str_Content, String str_FileName) {
		try {
			Channel obj_Channel = obj_Session.openChannel("sftp");
			obj_Channel.connect();
			ChannelSftp obj_SFTPChannel = (ChannelSftp) obj_Channel;
			// obj_SFTPChannel.cd(str_FileDirectory);
			InputStream obj_InputStream = new ByteArrayInputStream(str_Content.getBytes());
			obj_SFTPChannel.put(obj_InputStream, str_FileName);
			obj_SFTPChannel.exit();
			obj_InputStream.close();
			obj_Channel.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param command1
	 * @param session
	 * @throws JSchException
	 * @throws IOException
	 */
	private static int sendcommandtosv(String command1, Session session) throws JSchException, IOException {
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command1);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);

		InputStream in = channel.getInputStream();
		channel.connect();
		int exitstatus;
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				System.out.print(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				exitstatus = channel.getExitStatus();
				System.out.println("exit-status: " + exitstatus);
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();
		return exitstatus;
	}

}