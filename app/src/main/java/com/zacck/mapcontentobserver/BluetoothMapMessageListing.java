package com.zacck.mapcontentobserver;

/**
 * Created by Zacck on 3/16/2016.
 */
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;
import android.util.Log;
import android.util.Xml;
public class BluetoothMapMessageListing {
	private boolean hasUnread = false;
	private static final String TAG = "BluetoothMapMessageListing";
	private List<BluetoothMapMessageListingElement> list;
	public BluetoothMapMessageListing(){
		list = new ArrayList<BluetoothMapMessageListingElement>();
	}
	public void add(BluetoothMapMessageListingElement element) {
		list.add(element);
        /* update info regarding whether the list contains unread messages */
		if (element.getRead().equalsIgnoreCase("no"))
		{
			hasUnread = true;
		}
	}
	/**
	 * Used to fetch the number of BluetoothMapMessageListingElement elements in the list.
	 * @return the number of elements in the list.
	 */
	public int getCount() {
		if(list != null)
		{
			return list.size();
		}
		return 0;
	}
	/**
	 * does the list contain any unread messages
	 * @return true if unread messages have been added to the list, else false
	 */
	public boolean hasUnread()
	{
		return hasUnread;
	}
	/**
	 * Encode the list of BluetoothMapMessageListingElement(s) into a UTF-8
	 * formatted XML-string in a trimmed byte array
	 *
	 * @return a reference to the encoded byte array.
	 * @throws UnsupportedEncodingException
	 *             if UTF-8 encoding is unsupported on the platform.
	 */
	public byte[] encode() throws UnsupportedEncodingException {
		StringWriter sw = new StringWriter();
		XmlSerializer xmlMsgElement = Xml.newSerializer();
		try {
			xmlMsgElement.setOutput(sw);
			xmlMsgElement.startDocument(null, null);
			xmlMsgElement.startTag("", "MAP-msg-listing");
			xmlMsgElement.attribute("", "version", "1.0");
			// Do the XML encoding of list
			for (BluetoothMapMessageListingElement element : list) {
				element.encode(xmlMsgElement); // Append the list element
			}
			xmlMsgElement.endTag("", "MAP-msg-listing");
			xmlMsgElement.endDocument();
		} catch (IllegalArgumentException e) {
			Log.w(TAG, e.toString());
		} catch (IllegalStateException e) {
			Log.w(TAG, e.toString());
		} catch (IOException e) {
			Log.w(TAG, e.toString());
		}
		return sw.toString().getBytes("UTF-8");
	}
	public void sort() {
		Collections.sort(list);
	}
	public void segment(int count, int offset) {
		count = Math.min(count, list.size());
		if (offset + count <= list.size()) {
			list = list.subList(offset, offset + count);
		} else {
			list = null;
		}
	}
}