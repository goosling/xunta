package com.ixunta.client.db;


import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "feedback")
public class Feedback implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7727596158646739893L;
	@Element
	private String taID;
	@Element
	private String email;
	@Element(required=false)
	private String content;
	public String getTaID() {
		return taID;
	}
	public void setTaID(String taID) {
		this.taID = taID;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	

	
	
}
