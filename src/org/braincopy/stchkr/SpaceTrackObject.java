package org.braincopy.stchkr;

public class SpaceTrackObject {
	protected String norad_cat_id;
	protected String msg_epoch;
	protected String decay_epoch;

	/**
	 * @return the norad_cat_id
	 */
	public String getNorad_cat_id() {
		return norad_cat_id;
	}

	public void setNorad_cat_id(String norad_cat_id_) {
		this.norad_cat_id = norad_cat_id_;

	}

	public void setDecay_epoch(String decay_epoch_) {
		this.decay_epoch = decay_epoch_;
	}

	/**
	 * @return the decay_epoch
	 */
	public String getDecay_epoch() {
		return decay_epoch;
	}

	/**
	 * @return the msg_epoch
	 */
	public String getMsg_epoch() {
		return msg_epoch;
	}

	public void setMsg_epoch(String msg_epoch_) {
		this.msg_epoch = msg_epoch_;
	}

	public boolean equals(DecayEpoch epoch) {
		boolean result = false;
		if (this.getNorad_cat_id().equals(epoch.getNorad_cat_id())
				&& this.getMsg_epoch().equals(epoch.getMsg_epoch())) {
			result = true;
		}
		return result;
	}
}
