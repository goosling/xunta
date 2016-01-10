package com.ixunta.client.db;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

import com.ixunta.client.IwantUApp;

@Root(name = "pub")
public class Publication implements Serializable, Parcelable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7964671147969388666L;
	@Element
	private String id;
	
	/*
	 * ������ID
	 */
	@Element
	private String taID;
	
	/*
	 * ����ʱ��
	 */
	@Element
	private long datetime;
	
	/*
	 * ��ǰλ�þ���
	 */
	@Element
	private double longitude;
	
	/*
	 * ��ǰλ��γ��
	 */
	@Element
	private double latitude;
	
	/*
	 * ʮ�����Ƹ�ʽ��fffffff����7λ�����е�һλ��־��customer��merchant���Ƕ��ǣ���ΪĿǰֻ�漰ƴ��ҵ����ֵΪ1��
	 * �м���λ��־���⳵���ǲ�����������һ������Ŀǰֻ�漰ƴ��ҵ����ֵΪ01,������λ��־�������������λ���á�
	 * ��ǰĬ��ֵΪ1010000
	 */
	@Element
	private int serviceCode;
	
	/*
	 * ��������
	 */
	@Element(required=false)
	private String serviceDescription;
	
	/*
	 * �����Ŀռ���� ����λ��
	 */
	@Element
	private float searchSpaceDistance;
	
	/*
	 * ������ʱ����룬��λ��
	 */
	@Element
	private long searchTimeDistance;
	
	/*
	 * Ŀ�ĵؾ���
	 */
	@Element
	private double destLongitude;
	
	/*
	 * Ŀ�ĵ�γ��
	 */
	@Element
	private double destLatitude;
	
	/*
	 * Ŀ�ĵ�����
	 */
	@Element
	private String destName;
	
	@Element
	private boolean isAvailable;

	public Publication() {
		id = " ";
		taID = " ";
		datetime = 0;
		longitude = 0;
		latitude = 0;
		serviceCode = IwantUApp.CONS_SERVICECODE_DEFAULT;
		serviceDescription = "";
		searchSpaceDistance = Float.MAX_VALUE;
		searchTimeDistance = Integer.MAX_VALUE;
		isAvailable = true;
	}
	public Publication(Publication p){
		id = new String(p.getId());
		taID = new String(p.getTaID());
		datetime = p.getDatetime();
		longitude = p.getLongitude();
		latitude = p.getLatitude();
		serviceCode = p.getServiceCode();
		serviceDescription = new String(p.getServiceDescription());
		searchTimeDistance = p.getSearchTimeDistance();
		searchSpaceDistance = p.getSearchSpaceDistance();
		this.destLatitude = p.getDestLatitude();
		this.destLongitude = p.getDestLongitude();
		this.destName = p.getDestName();
		this.isAvailable = p.isAvailable();
	}

	public float getSearchSpaceDistance() {
		return searchSpaceDistance;
	}

	public void setSearchSpaceDistance(float searchSpaceDistance) {
		this.searchSpaceDistance = searchSpaceDistance;
	}


	public long getSearchTimeDistance() {
		return searchTimeDistance;
	}

	public void setSearchTimeDistance(long searchTimeDistance) {
		this.searchTimeDistance = searchTimeDistance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTaID() {
		return taID;
	}

	public void setTaID(String taID) {
		this.taID = taID;
	}

	public long getDatetime() {
		return datetime;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setDatetime(long datetime) {
		this.datetime = datetime;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public Publication(String theTaID) {
		taID = theTaID;
	}

	public int getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(int serviceCode) {
		this.serviceCode = serviceCode;
	}

	public double getDestLongitude() {
		return destLongitude;
	}

	public void setDestLongitude(double desLongitude) {
		this.destLongitude = desLongitude;
	}

	public double getDestLatitude() {
		return destLatitude;
	}

	public void setDestLatitude(double desLatitude) {
		this.destLatitude = desLatitude;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String desName) {
		this.destName = desName;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(id);
		dest.writeString(taID);
		dest.writeLong(datetime);
		dest.writeDouble(longitude);
		dest.writeDouble(latitude);
		dest.writeInt(serviceCode);
		dest.writeString(serviceDescription);
		dest.writeFloat(searchSpaceDistance);
		dest.writeLong(searchTimeDistance);
		dest.writeDouble(destLongitude);
		dest.writeDouble(destLatitude);
		dest.writeString(destName);
		
	}
	
	public static final Parcelable.Creator<Publication> CREATOR = new Parcelable.Creator<Publication>() {
		public Publication createFromParcel(Parcel in) {
			return new Publication(in);
		}

		public Publication[] newArray(int size) {
			return new Publication[size];
		}
	};
	private Publication(Parcel in) {
		id = in.readString();
		taID = in.readString();
		datetime = in.readLong();
		longitude = in.readDouble();
		latitude = in.readDouble();
		serviceCode = in.readInt();
		serviceDescription=in.readString();
		searchSpaceDistance=in.readFloat();
		searchTimeDistance= in.readLong();
		destLongitude = in.readDouble();
		destLatitude = in.readDouble();
		destName = in.readString();
	}
	
	public String toString() {
		String formattedStr = "id=%1$s, datetime is: %2$s";
		return String.format(formattedStr, id, new SimpleDateFormat(
				"yyyy/MM/dd hh:mm:ss:SSS").format(datetime));
	}
}
