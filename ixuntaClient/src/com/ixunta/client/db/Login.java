package com.ixunta.client.db;


import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "login")
public class Login implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8245407402947118589L;
	@Element
	private String taID;
	@Element
	private String phoneNum;
	@Element(required = false)
	private String imsi;
	@Element
	private long datetime;
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public long getDatetime() {
		return datetime;
	}
	public void setDatetime(long datetime) {
		this.datetime = datetime;
	}
	public String getTaID() {
		return taID;
	}
	public void setTaID(String taID) {
		this.taID = taID;
	}
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	
	
}
