package com.ixunta.client.converters;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class ByteArrayWrapper
{
    @Element
    protected byte[] byteArray;

    public ByteArrayWrapper()
    {
        super();
    }
    
    public ByteArrayWrapper(byte[] byteArray){
    	this.byteArray = byteArray;
    }

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

}