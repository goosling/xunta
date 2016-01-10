package com.ixunta.client.db;

import org.simpleframework.xml.Element;

/*
 * deprecated by production reason, duplicate with publication in many fields.
 * tom, 2013/3/29
 * 
 */
@Element(name = "searchCriteria")
public class SearchCriteria{
	@Element
	private float spaceDistance;
	@Element
	private long timeDistance;
	@Element
	private String taServiceType;
	@Element
	private String taServiceLvl1;
	@Element
	private String taServiceLvl2;
	
	public SearchCriteria(){
		spaceDistance=0;
		timeDistance=0;
		taServiceType="";
		taServiceLvl1="";
		taServiceLvl2="";
	}
	
	public float getSpaceDistance() {
		return spaceDistance;
	}
	public void setSpaceDistance(float spaceDistance) {
		this.spaceDistance = spaceDistance;
	}
	public long getTimeDistance() {
		return timeDistance;
	}
	public void setTimeDistance(long timeDistance) {
		this.timeDistance = timeDistance;
	}
	public String getTaServiceType() {
		return taServiceType;
	}
	public void setTaServiceType(String taServiceType) {
		this.taServiceType = taServiceType;
	}
	public String getTaServiceLvl1() {
		return taServiceLvl1;
	}
	public void setTaServiceLvl1(String taServiceLvl1) {
		this.taServiceLvl1 = taServiceLvl1;
	}
	public String getTaServiceLvl2() {
		return taServiceLvl2;
	}
	public void setTaServiceLvl2(String taServiceLvl2) {
		this.taServiceLvl2 = taServiceLvl2;
	}

}
