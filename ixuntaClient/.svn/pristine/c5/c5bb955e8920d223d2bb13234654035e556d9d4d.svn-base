package com.ixunta.client.converters;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import android.util.Base64;


public class ByteArrayConverter implements Converter<ByteArrayWrapper>
{
    public ByteArrayWrapper read(InputNode node) throws Exception 
    {
        InputNode nextnode = node.getNext();
        return new ByteArrayWrapper(Base64.decode(nextnode.getValue(),Base64.DEFAULT));
    }

    public void write(OutputNode node, ByteArrayWrapper byteArray) throws Exception 
    {       
        OutputNode byteArrayNode = node.getChild("byteArray");
        byteArrayNode.setValue(Base64.encodeToString(byteArray.getByteArray(), Base64.DEFAULT));     
    }

}