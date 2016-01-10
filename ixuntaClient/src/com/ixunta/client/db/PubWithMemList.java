package com.ixunta.client.db;

import java.io.Serializable;
import java.util.ArrayList;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "pubwithmemlist")
public class PubWithMemList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@ElementList(entry="pubwithmem", inline = true, required = false)
	private ArrayList<PubWithMem> pmList;

	public ArrayList<PubWithMem> getPList() {
		return pmList;
	}

	public void setPList(ArrayList<PubWithMem> pList) {
		this.pmList = pList;
	}
}
