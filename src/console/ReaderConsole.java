package console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Reader.ReaderAPI;
import sdk.Utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.*;
import org.json.simple.JSONArray;

public class ReaderConsole {
	
	public static HashMap<String, Integer> tagReads = new HashMap<>();
	public long user_id;
	public long order_id;
	boolean isScannerConnected = false;
	public static long counter ;
	public static long tagScanner ;
	public static boolean isTime = false ;
	public static boolean isTimeOut = false;
	boolean isUserAvailable = false;
	long userId ;
	int addressOfReader = 0;
	int hScanner[] = new int[1];
	int res;
	int[] nBaudRate = new int[1];
	int Address = 0;
	int timer_interval = 1000;
	byte[][] EPCC1G2_IDBuffer = new byte[ReaderAPI.MAX_LABELS][ReaderAPI.ID_MAX_SIZE_96BIT];
	MyReader mr = new MyReader();

	public static void main(String[] args) {
		ReaderConsole rc = new ReaderConsole();
		rc.runApp();
	}

	private void runApp() {
		while (true) {
			if (isScannerConnected) {
				try {
					if (!mr.isAlive())
						mr.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					if (mr.isAlive()) {
						mr.stop();
					}
					for (int i = 1; i <= 30; i++) {
						res = ReaderAPI.ConnectScanner(hScanner, "COM" + i, nBaudRate);
						if (res == ReaderAPI._OK) {
							System.out.println("Connected on COM" + i);
							isScannerConnected = true;
							addressOfReader = i;
							
							try {
								res = ReaderAPI.SetAntenna(hScanner[0], 15, Address);
								ReaderAPI.SetOutputPower(hScanner[0], 20, Address);
							} catch (Exception e) {

							}
							if (res != ReaderAPI._OK) {
								res = ReaderAPI.DisconnectScanner(hScanner[0]);
								isScannerConnected = false; // setMessage
							}
							break;
							
						}
					}
				} catch (Exception e) {
					isScannerConnected = false;
				}
			}
		}
	}
	
	public void removeUser(long user_id,long order_id) throws IOException{
		
		URL url = new URL(
				"http://apifrolic.tuple-mia.com:8003/stores/api/remove_left_over_cart/?format=json");
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		String urlParameters = "user_id=" + user_id+"&order_id="+order_id;
		//System.out.println(urlParameters);
		http.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		String encoding = con.getContentEncoding();
		try {
			InputStream in = http.getInputStream();
			encoding = encoding == null ? "UTF-8" : encoding;
			String body = IOUtils.toString(in, encoding);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(body);
			JSONArray array = (JSONArray) obj;
			System.out.println(body);
			isUserAvailable = (boolean) array.get(0);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	
	public void ReadTags(){
		
		try {
			int mem = 0, ptr = 0, len = 0;
			int res = -1;
			int i,  ID_len = 0, ID_len_temp = 0;
			String str = "";
			byte[] temp = new byte[64 * 2];
			byte[] IDBuffer = new byte[30 * 256];
			int[] nCounter = new int[2];
			byte[] mask = new byte[512];
			res = ReaderAPI.EPC1G2_ReadLabelID(hScanner[0], mem, ptr, len, mask, IDBuffer, nCounter, Address);
			if (res == ReaderAPI._OK) {				
				if (nCounter[0] > 8) {
					i = nCounter[0];
				}
				for (i = 0; i < nCounter[0]; i++) {
					if (IDBuffer[ID_len] > 32) {
						nCounter[0] = 0;
						break;
					}
					ID_len_temp = IDBuffer[ID_len] * 2 + 1;// 1word=16bit
					System.arraycopy(IDBuffer, ID_len, EPCC1G2_IDBuffer[i], 0, ID_len_temp);
					ID_len += ID_len_temp;
				}
				if (nCounter[0] > 0) {
					// MessageBeep(-1);
				}
				
				for (i = 0; i < nCounter[0]; i++) {
					str = "";
					ID_len = EPCC1G2_IDBuffer[i][0] * 2;
					System.arraycopy(EPCC1G2_IDBuffer[i], 1, temp, 0, ID_len);
					str = Utility.bytes2HexString(temp, ID_len);
					System.out.println("In function");
					System.out.println(str);
					int freq = 0;
					if (tagReads.get(str) != null) {
						freq = tagReads.get(str);
					}else{
						tagReads.put(str, ++freq);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Reader Exception"+e);
			isScannerConnected = false;
		}
	}
	
	private class MyReader extends Thread {
		

		public MyReader() {
		}

		@Override
		public void run() {
			for (;;) {
				try {
					File file = new File(System.getProperty("user.dir") + "/StoreIDS.txt");
					BufferedReader br = new BufferedReader(new FileReader(file));
					String st_data;
					String[] response;
					while ((st_data = br.readLine()) != null) {
						try {
							URL url = new URL(
									"http://apifrolic.tuple-mia.com:8003/stores/api/get_user_to_given_trolley/?format=json");
							URLConnection con = url.openConnection();
							HttpURLConnection http = (HttpURLConnection) con;
							String urlParameters = "device_id=" + st_data;
							//System.out.println(urlParameters);
							http.setDoOutput(true);
							DataOutputStream wr = new DataOutputStream(con.getOutputStream());
							wr.writeBytes(urlParameters);
							wr.flush();
							wr.close();
							String encoding = con.getContentEncoding();
							try {
								InputStream in = http.getInputStream();
								encoding = encoding == null ? "UTF-8" : encoding;
								String body = IOUtils.toString(in, encoding);
								JSONParser parser = new JSONParser();
								Object obj = parser.parse(body);
								JSONArray array = (JSONArray) obj;
								System.out.println(body);
								isUserAvailable = (boolean) array.get(0);
								
								// initialize the user_id and order_id
								if(isUserAvailable){
									user_id = (long) array.get(1);
									order_id = (long) array.get(2);
								}
								if (isUserAvailable && !isTime){
									isTime = true;
									counter = System.currentTimeMillis();
								}
								
								if (System.currentTimeMillis() < counter + 10000 ){
									System.out.println("Is Awake");
								}else{
									if (isUserAvailable){
										System.out.println("System Needs to Die Now");
										removeUser(user_id,order_id);
									}else{
										System.out.println("Waiting for someone to come");
										isTime = false;
									}
								}
								
								System.out.println("User Got recongnized at "+counter);
								
								try {
									int mem = 0, ptr = 0, len = 0;
									int res = -1;
									int i, j, k, ID_len = 0, ID_len_temp = 0;
									String str = "", str_temp;
									String strTemp;
									byte[] temp = new byte[64 * 2];
									byte[] DB = new byte[128];
									byte[] IDBuffer = new byte[30 * 256];
									int[] nCounter = new int[2];
									byte[] mask = new byte[512];
									res = ReaderAPI.EPC1G2_ReadLabelID(hScanner[0], mem, ptr, len, mask, IDBuffer, nCounter, Address);
									if (res == ReaderAPI._OK) {
																				
										if (nCounter[0] > 8) {
											i = nCounter[0];
										}
										for (i = 0; i < nCounter[0]; i++) {
											if (IDBuffer[ID_len] > 32) {
												nCounter[0] = 0;
												break;
											}
											ID_len_temp = IDBuffer[ID_len] * 2 + 1;// 1word=16bit
											System.arraycopy(IDBuffer, ID_len, EPCC1G2_IDBuffer[i], 0, ID_len_temp);
											ID_len += ID_len_temp;
										}
										if (nCounter[0] > 0) {
											// MessageBeep(-1);
										}
										
										for (i = 0; i < nCounter[0]; i++) {
											str = "";
											ID_len = EPCC1G2_IDBuffer[i][0] * 2;
											System.arraycopy(EPCC1G2_IDBuffer[i], 1, temp, 0, ID_len);
											str = Utility.bytes2HexString(temp, ID_len);
											System.out.println(str);
											int freq = 0;
											if (tagReads.get(str) != null) {
												freq = tagReads.get(str);
											} else {
												if (isUserAvailable){
													tagReads.put(str, ++freq);
													counter = System.currentTimeMillis();
													isTime = false;
													//initiate the timer to read continuously
													long t = System.currentTimeMillis();
													long end = t + 2000;
													while(System.currentTimeMillis() < end ){	
														ReadTags();
													}
												}
												else{
													System.out.println("Buzzer Bazao");
													// initiate the timer
//													long t = System.currentTimeMillis();
//													long end = t + 2000;
//													
//													while(System.currentTimeMillis() < end ){	
//														ReaderAPI.SetRelay(hScanner[0], 1, Address);
//													}
//													ReaderAPI.SetRelay(hScanner[0], 0, Address);
													tagReads.clear();
												}
											}
										}
									}
									
									// here all the tags have been read
									System.out.println("Tag");
									System.out.println(tagReads.keySet());
									
									if (tagReads.keySet().size() > 0){
										if (isUserAvailable){
											ArrayList<String> tag = new ArrayList<String>();
											// post the tag to the back end  server
											for (String tagvalue : tagReads.keySet()){
												tag.add("'"+tagvalue+"'");
											}
											URL url1 = new URL(
													"http://apifrolic.tuple-mia.com:8003/basket/api/add_product_to_basket/?format=json");
											URLConnection con1 = url1.openConnection();
											HttpURLConnection http1 = (HttpURLConnection) con1;
											http1.setRequestProperty("Authorization", "C");
											String urlParameters1 = "fridge_id=" + st_data +"&tags="+tag;
											System.out.println(urlParameters1);
											http1.setDoOutput(true);
											DataOutputStream wr1 = new DataOutputStream(con1.getOutputStream());
											wr1.writeBytes(urlParameters1);
											wr1.flush();
											wr1.close();
											try {
												InputStream in1 = http1.getInputStream();
												encoding = encoding == null ? "UTF-8" : encoding;
												String body1 = IOUtils.toString(in1, encoding);
												System.out.println("Rfid Response");
												System.out.println(body1);
												System.out.println("Rfid Response");
												
											}catch(Exception e){
												System.out.println("Exception while posting Rfid data"+e);
											}
											tagReads.clear();
										}
									}
								} catch (Exception e) {
									System.out.println("Reader Exception"+e);
									isScannerConnected = false;
								}
								
								Thread.sleep(timer_interval);
							} catch (Exception e) {
								System.out.println("Exception While Fetching User Info " + e.toString());
							}
						} catch (IOException x) {
							System.out.println(x.getLocalizedMessage());
							System.out.println(x);
						}
					}
				} catch (IOException x) {
					System.out.println(x);
				}
			}
		}
	}
}
