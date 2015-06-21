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
 * Main class of Space Track Checker command line application.
 * 
 * @author Hiroaki Tateshita
 * @version 0.5.5
 * 
 */
public class SpaceTrackWorker {

	/**
	 * the url of space track
	 */
	final private static String baseURL = "https://www.space-track.org";

	/**
	 * the path string at authorization.
	 */
	final private static String authPath = "/auth/login";

	/**
	 * line break to avoid dependency of OS, linux and windows.
	 */
	final private static String BR = System.getProperty("line.separator");

	/**
	 * current time to execute this application
	 */
	private final Calendar current;

	/**
	 * last time to execute this application
	 */
	private Calendar last;

	/**
	 * member object to transfer between string and calendar object. it is based
	 * on UTC.
	 */
	private final SimpleDateFormat sdf;

	/**
	 * the line number to show the results of searching decay information from
	 * space track. Usually it will be set by ini file. if there is no value in
	 * the ini file, "10" will be set.
	 */
	private int showLine;

	/**
	 * properties object from space_track.ini
	 */
	private Properties spaceTrackProperties;

	/**
	 * http connection to connect space track website
	 */
	private HttpsURLConnection httpsConnection;

	/**
	 * the latest log file name.
	 */
	private String lastlogFileName;

	/**
	 * ArrayList of DecayEpoch objects from the latest log file
	 */
	private ArrayList<DecayEpochObject> lastEpochArray;

	// private int limit;

	SpaceTrackWorker() {
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		// get current time.
		this.current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		try {
			SpaceTrackWorker worker = new SpaceTrackWorker();

			ArrayList<DecayEpochObject> decayEpochList = worker
					.getDecayEpochList();

			worker.outputDecayData(decayEpochList);

			worker.saveDecayData(decayEpochList);

		} catch (IOException e) {
			System.err.println("please check ini file.");
		} catch (SpaceTrackNoResultException e) {
			System.err.println(e.getMessage());
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
	public ArrayList<DecayEpochObject> getDecayEpochList() throws Exception {
		ArrayList<DecayEpochObject> result = null;

		loadPropertiesFiles();

		/*
		 * Authentication including establish https connection.
		 */
		spaceTrackAuthentication();

		/*
		 * create query for decay information of space track
		 */
		String query = createQueryForDecaySpaceTrack();

		URL url = new URL(baseURL + query);

		// for processing xml file.
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

	public ArrayList<DecayEpochObject> getTIPList() throws Exception {
		ArrayList<DecayEpochObject> result = null;

		loadPropertiesFiles();

		/*
		 * Authentication including establish https connection.
		 */
		spaceTrackAuthentication();

		/*
		 * create query for decay information of space track
		 */
		String query = createQueryForTIPSpaceTrack();

		URL url = new URL(baseURL + query);

		// for processing xml file.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(url.openStream());
			result = createTIPArray(doc);
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
	 * create query for space track web service.
	 * 
	 * @return query by using information from ini file.
	 */
	private String createQueryForDecaySpaceTrack() {
		String query;
		String country = "";
		String limit = "";
		String rcs_size = "";
		String noradCatId = "";
		if (spaceTrackProperties != null) {
			country = spaceTrackProperties.getProperty("country");
			limit = spaceTrackProperties.getProperty("limit");
			noradCatId = spaceTrackProperties.getProperty("NORAD_CAT_ID");
			rcs_size = spaceTrackProperties.getProperty("RCS_SIZE");
		}
		query = "/basicspacedata/query/class/decay/";
		if (noradCatId != null && !noradCatId.equals("")) {
			query += "NORAD_CAT_ID/";
			query += noradCatId + "/";
		}
		if (country != null && !country.equals("")) {
			query += "COUNTRY/";
			query += country + "/";
		}
		if (rcs_size != null && !rcs_size.equals("")) {
			query += "RCS_SIZE/";
			query += rcs_size + "/";
		}
		query += "MSG_TYPE/Prediction/orderby/MSG_EPOCH%20desc/";
		if (limit != null && !limit.equals("")) {
			query += "limit/";
			query += limit + "/";
		}
		query += "format/xml/metadata/false";
		return query;
		//
		// https://www.space-track.org/basicspacedata/query/class/decay/MSG_TYPE/prediction/orderby/MSG_EPOCH%20desc/limit/10/format/xml/metadata/false
	}

	private String createQueryForTIPSpaceTrack() {
		String query;
		String limit = "";
		String noradCatId = "";
		if (spaceTrackProperties != null) {
			limit = spaceTrackProperties.getProperty("limit");
			noradCatId = spaceTrackProperties.getProperty("NORAD_CAT_ID");
		}
		query = "/basicspacedata/query/class/tip/orderby/MSG_EPOCH%20desc/";
		if (noradCatId != null && !noradCatId.equals("")) {
			query += "NORAD_CAT_ID/";
			query += noradCatId + "/";
		}
		if (limit != null && !limit.equals("")) {
			query += "limit/";
			query += limit + "/";
		}
		query += "format/xml/metadata/false";
		return query;

		// https://www.space-track.org/basicspacedata/query/class/tip/orderby/MSG_EPOCH%20desc/limit/100/format/xml/metadata/false
	}

	/**
	 * Authentication for space track web service. In this method, https
	 * connection will be created.
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
	 * create delay epoch array list from xml response from web service of space
	 * track.
	 * 
	 * @param doc
	 *            xml object acquired from space track web service
	 * @return delay epoch array list
	 * @throws SpaceTrackNoResultException
	 */
	private ArrayList<DecayEpochObject> createDecayEpochArray(Document doc)
			throws SpaceTrackNoResultException {
		NodeList decayEpochNodeList = doc.getElementsByTagName("item");
		NodeList membersOfItem;
		ArrayList<DecayEpochObject> result = new ArrayList<DecayEpochObject>();
		if (decayEpochNodeList.getLength() > 0) {
			boolean isNoradCatIDSet = false;
			if (!spaceTrackProperties.getProperty("NORAD_CAT_ID").equals("")) {
				isNoradCatIDSet = true;
			}
			for (int i = 0; i < decayEpochNodeList.getLength(); i++) {
				// System.out.println(i + ": "
				// + decayEpochNodeList.item(i).getNodeName());
				DecayEpochObject decayEpoch = new DecayEpochObject();
				membersOfItem = decayEpochNodeList.item(i).getChildNodes();
				Node tempNode;

				for (int j = 0; j < membersOfItem.getLength(); j++) {
					if (membersOfItem.item(j).getNodeName()
							.equals("NORAD_CAT_ID")) {
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
						decayEpoch.setCountry(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("MSG_EPOCH")) {
						decayEpoch.setMsg_epoch(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("DECAY_EPOCH")) {
						decayEpoch.setDecay_epoch(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("SOURCE")) {
						decayEpoch.setSource(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("MSG_TYPE")) {
						decayEpoch.setMsg_type(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					}

				}
				if (isNoradCatIDSet
						|| !checkIncludeSameNoradCatID(result, decayEpoch)) {
					result.add(decayEpoch);
				}
			}
		} else {
			throw new SpaceTrackNoResultException(
					"The search result was 0. please check the ini file.");
		}

		return result;
	}

	/**
	 * 
	 * @param doc
	 * @return should be TIP object
	 * @throws SpaceTrackNoResultException
	 */
	private ArrayList<DecayEpochObject> createTIPArray(Document doc)
			throws SpaceTrackNoResultException {
		NodeList decayEpochNodeList = doc.getElementsByTagName("item");
		NodeList membersOfItem;
		ArrayList<DecayEpochObject> result = new ArrayList<DecayEpochObject>();
		if (decayEpochNodeList.getLength() > 0) {
			for (int i = 0; i < decayEpochNodeList.getLength(); i++) {
				// System.out.println(i + ": "
				// + decayEpochNodeList.item(i).getNodeName());
				DecayEpochObject tip = new DecayEpochObject();
				membersOfItem = decayEpochNodeList.item(i).getChildNodes();
				Node tempNode;

				for (int j = 0; j < membersOfItem.getLength(); j++) {
					if (membersOfItem.item(j).getNodeName()
							.equals("NORAD_CAT_ID")) {
						tip.setNorad_cat_id(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("LAT")) {
						tip.setLat(membersOfItem.item(j).getFirstChild()
								.getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("LON")) {
						tempNode = membersOfItem.item(j).getFirstChild();
						if (tempNode != null) {
							tip.setLon(tempNode.getNodeValue());
						}
					} else if (membersOfItem.item(j).getNodeName()
							.equals("MSG_EPOCH")) {
						tip.setMsg_epoch(membersOfItem.item(j).getFirstChild()
								.getNodeValue());
					} else if (membersOfItem.item(j).getNodeName()
							.equals("DECAY_EPOCH")) {
						tip.setDecay_epoch(membersOfItem.item(j)
								.getFirstChild().getNodeValue());
					}

				}
				if (!checkIncludeSameNoradCatID(result, tip)) {
					result.add(tip);
				}
			}
		} else {
			throw new SpaceTrackNoResultException(
					"The search result was 0. please check the ini file.");
		}
		return result;
	}

	/**
	 * check if decayEpochList include the decayEpoch or not.
	 * 
	 * @param decayEpochList
	 *            this list should be sorted by msg epoch
	 * @param decayEpoch
	 * @return decayEpochList include the decayEpoch or not. if include, return
	 *         true, if not return false.
	 */
	private boolean checkIncludeSameNoradCatID(
			ArrayList<DecayEpochObject> decayEpochList,
			DecayEpochObject decayEpoch) {
		boolean result = false;
		for (int i = 0; i < decayEpochList.size(); i++) {
			if (decayEpochList.get(i).getNorad_cat_id()
					.equals(decayEpoch.getNorad_cat_id())) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * show the results on the standard output.
	 * 
	 * @param decayEpochList
	 */
	private void outputDecayData(ArrayList<DecayEpochObject> decayEpochList) {
		String result = "";
		if (this.showLine > decayEpochList.size()) {
			this.showLine = decayEpochList.size();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		sdf.setTimeZone(this.current.getTimeZone());
		result += sdf.format(this.current.getTime()) + "("
				+ sdf.getTimeZone().getID() + ") ";
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
				result += (decayEpochList.get(i)).getCSVLine() + BR;
			}
			System.out.print(result);
		}
	}

	/**
	 * check whether the DecayEpoch object is included in the last results of
	 * DecayEpoch.
	 * 
	 * @param spaceTrackObject
	 * @return
	 */
	private boolean checkExist(DecayEpochObject spaceTrackObject) {
		boolean result = false;
		if (this.lastEpochArray != null) {
			for (int i = 0; i < this.lastEpochArray.size(); i++) {
				if (this.lastEpochArray.get(i).equals(spaceTrackObject)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * load properties file, space_track.ini
	 * 
	 * @throws IOException
	 *             file i/o might have problem.
	 * @throws ParseException
	 *             log file might be broken.
	 */
	private void loadPropertiesFiles() throws IOException, ParseException {
		FileReader lastlogFileReader = null;
		BufferedReader reader = null;
		if (spaceTrackProperties == null) {
			spaceTrackProperties = new Properties();
			try {
				spaceTrackProperties.load(new FileInputStream(
						"conf/space_track.ini"));
				try {
					this.lastlogFileName = spaceTrackProperties
							.getProperty("lastlog");
					File lastlogFile = new File(lastlogFileName);
					if (lastlogFile.exists()) {
						lastlogFileReader = new FileReader(lastlogFileName);
						reader = new BufferedReader(lastlogFileReader);
						this.last = Calendar.getInstance();
						this.last.setTime(sdf.parse(reader.readLine()));
						String tempStr;
						String[] tempStrArray;
						this.lastEpochArray = new ArrayList<DecayEpochObject>();
						while ((tempStr = reader.readLine()) != null) {
							tempStrArray = tempStr.split(",");
							DecayEpochObject epoch = new DecayEpochObject();
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
					} else {
						this.last = null;
					}
					String showLineStr = spaceTrackProperties
							.getProperty("showLine");
					if (showLineStr.equals("")) {
						this.showLine = 10;// default
					} else {
						this.showLine = Integer.parseInt(showLineStr);
					}
				} catch (ParseException e) {
					throw new ParseException(
							"log file might have problem. delete the latest log file in the log folder."
									+ e.getMessage(), e.getErrorOffset());
				}
			} catch (IOException e) {
				throw new IOException("during loading ini file: "
						+ e.getMessage());
			} finally {
				if (reader != null) {
					reader.close();
				}
				if (lastlogFileReader != null) {
					lastlogFileReader.close();
				}
			}
		}
	}

	/**
	 * save log file by using search result.
	 * 
	 * @param decayEpochList
	 * @throws IOException
	 *             file i/o problem to write log file.
	 */
	private void saveDecayData(ArrayList<DecayEpochObject> decayEpochList)
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
			DecayEpochObject epoch;
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
			throw e;
		}
	}

	public Calendar getCurrent() {
		return this.current;
	}
}
