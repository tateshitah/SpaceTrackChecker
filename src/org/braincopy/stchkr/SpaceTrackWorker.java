/*
Copyright (c) 2015 Hiroaki Tateshita

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package org.braincopy.stchkr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.1.0
 * 
 */
public class SpaceTrackWorker {

	final private static String baseURL = "https://www.space-track.org";
	final private static String authPath = "/auth/login";
	final private static String BR = System.getProperty("line.separator");
	private Calendar current;
	private Calendar last;
	private final SimpleDateFormat sdf;
	private int showLine;

	SpaceTrackWorker() {
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.showLine = 10;
	}

	/**
	 * 
	 */
	private Properties spaceTrackProperties;

	/**
	 * 
	 */
	private HttpsURLConnection httpsConnection;
	private String lastlogFileName;
	private ArrayList<DecayEpoch> lastEpochArray;

	public static void main(String[] args) {
		try {
			SpaceTrackWorker worker = new SpaceTrackWorker();
			worker.current = Calendar.getInstance();
			ArrayList<DecayEpoch> decayEpochList = worker.getDecayEpochList();

			worker.outputDecayData(decayEpochList);
			worker.saveDecayData(decayEpochList);

		} catch (IOException e) {
			System.err.println("please check ini file.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get Decay data from Space Track website
	 * 
	 * @return
	 * @throws Exception
	 */
	public ArrayList<DecayEpoch> getDecayEpochList() throws Exception {
		ArrayList<DecayEpoch> result = null;

		loadPropertiesFiles();

		/*
		 * Authentication including establish https connection.
		 */
		spaceTrackAuthentication();

		/*
		 * create query for space track
		 */
		String query = createQueryForSpaceTrack();

		URL url = new URL(baseURL + query);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(url.openStream());
			result = createDecayEpochArray(doc);
		} catch (ParserConfigurationException | SAXException e) {
			System.err.println(e);
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			System.err.println("id and password might be wrong.");
			throw e;
		}

		httpsConnection.disconnect();

		return result;
	}

	/**
	 * Authentication. in this method, https connection will be created.
	 * 
	 * @throws IOException
	 */
	private void spaceTrackAuthentication() throws IOException {
		String userName = spaceTrackProperties.getProperty("user");
		String password = spaceTrackProperties.getProperty("password");
		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);

		URL url = new URL(baseURL + authPath);

		httpsConnection = (HttpsURLConnection) url.openConnection();
		httpsConnection.setDoOutput(true);
		httpsConnection.setRequestMethod("POST");

		String input = "identity=" + userName + "&password=" + password;

		OutputStream os = httpsConnection.getOutputStream();
		os.write(input.getBytes());
		os.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(httpsConnection.getInputStream())));

		String output;
		// System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			System.out.println("authrization: " + output);
		}
	}

	/**
	 * 
	 * @param doc
	 * @return
	 */
	private ArrayList<DecayEpoch> createDecayEpochArray(Document doc) {
		NodeList decayEpochNodeList = doc.getElementsByTagName("item");
		NodeList membersOfItem;
		ArrayList<DecayEpoch> result = new ArrayList<DecayEpoch>();
		for (int i = 0; i < decayEpochNodeList.getLength(); i++) {
			// System.out.println(i + ": "
			// + decayEpochNodeList.item(i).getNodeName());
			DecayEpoch decayEpoch = new DecayEpoch();
			membersOfItem = decayEpochNodeList.item(i).getChildNodes();
			Node tempNode;

			for (int j = 0; j < membersOfItem.getLength(); j++) {
				if (membersOfItem.item(j).getNodeName().equals("NORAD_CAT_ID")) {
					decayEpoch.setNorad_cat_id(membersOfItem.item(j)
							.getFirstChild().getNodeValue());
				} else if (membersOfItem.item(j).getNodeName()
						.equals("OBJECT_NAME")) {
					decayEpoch.setObject_name(membersOfItem.item(j)
							.getFirstChild().getNodeValue());
				} else if (membersOfItem.item(j).getNodeName()
						.equals("RCS_SIZE")) {
					tempNode = membersOfItem.item(j).getFirstChild();
					if (tempNode != null) {
						decayEpoch.setRcs_size(tempNode.getNodeValue());
					}
				} else if (membersOfItem.item(j).getNodeName()
						.equals("COUNTRY")) {
					decayEpoch.setCountry(membersOfItem.item(j).getFirstChild()
							.getNodeValue());
				} else if (membersOfItem.item(j).getNodeName()
						.equals("MSG_EPOCH")) {
					decayEpoch.setMsg_epoch(membersOfItem.item(j)
							.getFirstChild().getNodeValue());
				} else if (membersOfItem.item(j).getNodeName()
						.equals("DECAY_EPOCH")) {
					decayEpoch.setDecay_epoch(membersOfItem.item(j)
							.getFirstChild().getNodeValue());
				} else if (membersOfItem.item(j).getNodeName().equals("SOURCE")) {
					decayEpoch.setSource(membersOfItem.item(j).getFirstChild()
							.getNodeValue());
				} else if (membersOfItem.item(j).getNodeName()
						.equals("MSG_TYPE")) {
					decayEpoch.setMsg_type(membersOfItem.item(j)
							.getFirstChild().getNodeValue());
				}

			}
			result.add(decayEpoch);
		}
		return result;
	}

	private void outputDecayData(ArrayList<DecayEpoch> decayEpochList)
			throws IOException {
		String result = "";
		if (decayEpochList != null) {
			if (this.last != null) {
				double diff = ((this.current.getTimeInMillis() - this.last
						.getTimeInMillis()) / 1000.0 / 3600.0);
				result += "It took " + diff + " hours from the previous output"
						+ BR;
			} else {
				result += "This is first output since last initialization."
						+ BR;
			}
			for (int i = 0; i < this.showLine; i++) {
				if (!checkExist(decayEpochList.get(i))) {
					result += "+";
				}
				result += decayEpochList.get(i).getCSVLine() + BR;
			}
			System.out.print(result);
		} else {
			System.out.println("here");
		}
	}

	private boolean checkExist(DecayEpoch decayEpoch) {
		boolean result = false;
		if (this.lastEpochArray != null) {
			for (int i = 0; i < this.lastEpochArray.size(); i++) {
				if (this.lastEpochArray.get(i).equals(decayEpoch)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @throws IOException
	 */
	private void loadPropertiesFiles() throws IOException {
		if (spaceTrackProperties == null) {
			spaceTrackProperties = new Properties();
			try {
				spaceTrackProperties.load(new FileInputStream(
						"conf/space_track.ini"));

				FileReader lastlogFileReader;
				try {
					this.lastlogFileName = spaceTrackProperties
							.getProperty("lastlog");
					File lastlogFile = new File(lastlogFileName);
					if (lastlogFile.exists()) {
						lastlogFileReader = new FileReader(lastlogFileName);
						BufferedReader reader = new BufferedReader(
								lastlogFileReader);
						this.last = Calendar.getInstance();
						this.last.setTime(sdf.parse(reader.readLine()));
						String tempStr;
						String[] tempStrArray;
						this.lastEpochArray = new ArrayList<DecayEpoch>();
						while ((tempStr = reader.readLine()) != null) {
							tempStrArray = tempStr.split(",");
							DecayEpoch epoch = new DecayEpoch();
							epoch.setNorad_cat_id(tempStrArray[0]);
							epoch.setObject_name(tempStrArray[1]);
							epoch.setCountry(tempStrArray[2]);
							epoch.setMsg_epoch(tempStrArray[3]);
							epoch.setDecay_epoch(tempStrArray[4]);
							epoch.setRcs_size(tempStrArray[5]);
							epoch.setSource(tempStrArray[6]);
							epoch.setMsg_type(tempStrArray[7]);
							this.lastEpochArray.add(epoch);

						}
						reader.close();
					} else {
						this.last = null;
					}
					this.showLine = Integer.parseInt(spaceTrackProperties
							.getProperty("showLine"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				// dbProperties
				// .load(new FileInputStream("WEB-INF/conf/db.ini"));
				// }
			} catch (IOException e) {
				throw new IOException("during loading ini file: "
						+ e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param decayEpochList
	 * @throws IOException
	 */
	private void saveDecayData(ArrayList<DecayEpoch> decayEpochList)
			throws IOException {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf_filename = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.UK);
		sdf_filename.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateStr = sdf_filename.format(calendar.getTime());
		// System.out.println("dateStr: " + dateStr);
		String fileName = "log/decay_" + dateStr + ".log";
		FileWriter writer;
		try {
			writer = new FileWriter(fileName);
			writer.write(sdf.format(calendar.getTime()) + BR);
			DecayEpoch epoch;
			for (int i = 0; i < decayEpochList.size(); i++) {
				epoch = decayEpochList.get(i);
				writer.write(epoch.getCSVLine() + BR);
			}
			if (spaceTrackProperties != null) {
				spaceTrackProperties.setProperty("lastlog", fileName);
				spaceTrackProperties.store(new FileOutputStream(
						"conf/space_track.ini"), null);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * create query for space track.
	 * 
	 * @return
	 */
	private String createQueryForSpaceTrack() {
		String query;

		query = "/basicspacedata/query/class/decay/COUNTRY/JPN/MSG_TYPE/Prediction/orderby/MSG_EPOCH%20desc/format/xml/metadata/false";
		return query;
	}

}
