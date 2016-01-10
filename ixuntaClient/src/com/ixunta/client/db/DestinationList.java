package com.ixunta.client.db;

import java.io.Serializable;
import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;


@Root(name = "destList")
public class DestinationList implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ElementList(inline = true, required = false, entry="destination")
	private List<Destination> dList;

	public List<Destination> getdList() {
		return dList;
	}

	public void setdList(List<Destination> dList) {
		this.dList = dList;
	}

}
