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

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.3.0
 * 
 */
public class DecayEpoch extends SpaceTrackObject {
	private String object_name;
	private String rcs_size;
	private String country;
	private String msg_type;
	private String source;

	/**
	 * @return the object_name
	 */
	public String getObject_name() {
		return object_name;
	}

	/**
	 * @return the rcs_size
	 */
	public String getRcs_size() {
		return rcs_size;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the msg_type
	 */
	public String getMsg_type() {
		return msg_type;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return this.norad_cat_id + ":" + this.object_name;
	}

	public void setObject_name(String object_name_) {
		this.object_name = object_name_;

	}

	public void setRcs_size(String rcs_size_) {
		this.rcs_size = rcs_size_;

	}

	public void setCountry(String country_) {
		this.country = country_;

	}

	public void setMsg_type(String msg_type_) {
		this.msg_type = msg_type_;
	}

	public void setSource(String source_) {
		this.source = source_;
	}

	public String getCSVLine() {
		String result = getNorad_cat_id() + "," + getObject_name() + ","
				+ getCountry() + "," + getMsg_epoch() + "," + getDecay_epoch()
				+ "," + getRcs_size() + "," + getSource() + "," + getMsg_type();
		return result;
	}

}
