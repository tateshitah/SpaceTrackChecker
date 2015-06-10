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

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * By using browser, this class will show the result of searching space-track.
 * So this class will try to create html file. In the html file, you can the
 * record of decay information which should include list of past and future (60
 * days) decay: results and predictions.
 * 
 * Train - Drive By
 * 
 * @author Hiroaki Tateshita
 *
 */
public class SpaceTrackViewer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		SpaceTrackViewer viewer = new SpaceTrackViewer();

		try {
			ArrayList<DecayEpoch> decayEpochList = worker.getDecayEpochList();
			ArrayList<TIP> tipList = worker.getTIPList();
			viewer.createHTML(decayEpochList);
			viewer.createHTML2(tipList);
		} catch (Exception e1) {
			System.err.println("" + e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		File htmlfile = new File("dist/decaylist.html");
		if (htmlfile.exists()) {

			Desktop desktop = Desktop.getDesktop();
			String uriString = "file://"
					+ htmlfile.getAbsolutePath().replace('\\', '/');
			try {
				URI uri = new URI(uriString);
				desktop.browse(uri);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createHTML2(ArrayList<TIP> tipList)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("dist/tipMap.html", "UTF-8");
		} catch (FileNotFoundException e) {
			throw e;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
		writer.print("<!DOCTYPE html><html><header>"
				+ "<link rel='stylesheet' href='css/style_1.css' type='text/css'>"
				+ "</header><body>");
		writer.close();

	}

	private void createHTML(ArrayList<DecayEpoch> decayEpochList)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("dist/decaylist.html", "UTF-8");
		} catch (FileNotFoundException e) {
			throw e;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
		writer.print("<!DOCTYPE html><html><header>"
				+ "<link rel='stylesheet' href='css/style_1.css' type='text/css'>"
				+ "</header><body>");
		writer.print("<table class="
				+ "'bordered'><thead><tr><th>Cat ID</th><th>Name</th>"
				+ "<th>Country</th><th>MSG_EPOCH</th><th>DECAY_EPOCH</th></tr></thead><tbody>");
		for (int i = 0; i < decayEpochList.size(); i++) {
			writer.print("<tr><td>" + decayEpochList.get(i).getNorad_cat_id()
					+ "</td><th>" + decayEpochList.get(i).getObject_name()
					+ "</th><td>" + decayEpochList.get(i).getCountry()
					+ "</td><td>" + decayEpochList.get(i).getMsg_epoch()
					+ "</td><td>" + decayEpochList.get(i).getDecay_epoch()
					+ "</td></tr>");
		}
		writer.print("</tbody></table></body></html>");
		writer.close();
	}
}
