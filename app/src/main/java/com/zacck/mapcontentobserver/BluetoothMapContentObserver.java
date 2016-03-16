package com.zacck.mapcontentobserver;

/**
 * Created by Zacck on 3/16/2016.
 */

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony.MmsSms;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;

import com.zacck.mapcontentobserver.BluetoothMapUtils.TYPE;

public class BluetoothMapContentObserver {
	private static final String TAG = "BluetoothMapContentObserver";
	private static final boolean D = false;
	private static final boolean V = false;
	private Context mContext;
	private ContentResolver mResolver;
	private BluetoothMnsObexClient mMnsClient;
	private int mMasId;
	private TYPE mSmsType;

	public BluetoothMapContentObserver(final Context context) {
		mContext = context;
		mResolver = mContext.getContentResolver();
		mSmsType = getSmsType();
	}

	private TYPE getSmsType() {
		TYPE smsType = null;
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
			smsType = TYPE.SMS_GSM;
		} else if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
			smsType = TYPE.SMS_CDMA;
		}
		return smsType;
	}

	private final ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (V) Log.d(TAG, "onChange on thread: " + Thread.currentThread().getId()
					+ " Uri: " + uri.toString() + " selfchange: " + selfChange);
			handleMsgListChanges();
		}
	};


	private class Event {
		String eventType;
		long handle;
		String folder;
		String oldFolder;
		TYPE msgType;

		public Event(String eventType, long handle, String folder,
					 String oldFolder, TYPE msgType) {
			String PATH = "telecom/msg/";
			this.eventType = eventType;
			this.handle = handle;
			if (folder != null) {
				this.folder = PATH + folder;
			} else {
				this.folder = null;
			}
			if (oldFolder != null) {
				this.oldFolder = PATH + oldFolder;
			} else {
				this.oldFolder = null;
			}
			this.msgType = msgType;
		}

		public byte[] encode() throws UnsupportedEncodingException {
			StringWriter sw = new StringWriter();
			XmlSerializer xmlEvtReport = Xml.newSerializer();
			try {
				xmlEvtReport.setOutput(sw);
				xmlEvtReport.startDocument(null, null);
				xmlEvtReport.text("\n");
				xmlEvtReport.startTag("", "MAP-event-report");
				xmlEvtReport.attribute("", "version", "1.0");
				xmlEvtReport.startTag("", "event");
				xmlEvtReport.attribute("", "type", eventType);
				xmlEvtReport.attribute("", "handle", BluetoothMapUtils.getMapHandle(handle, msgType));
				if (folder != null) {
					xmlEvtReport.attribute("", "folder", folder);
				}
				if (oldFolder != null) {
					xmlEvtReport.attribute("", "old_folder", oldFolder);
				}
				xmlEvtReport.attribute("", "msg_type", msgType.name());
				xmlEvtReport.endTag("", "event");
				xmlEvtReport.endTag("", "MAP-event-report");
				xmlEvtReport.endDocument();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (V) System.out.println(sw.toString());
			return sw.toString().getBytes("UTF-8");
		}
	}

	private class Msg {
		long id;
		int type;

		public Msg(long id, int type) {
			this.id = id;
			this.type = type;
		}
	}

	private Map<Long, Msg> mMsgListSms =
			Collections.synchronizedMap(new HashMap<Long, Msg>());
	private Map<Long, Msg> mMsgListMms =
			Collections.synchronizedMap(new HashMap<Long, Msg>());

	public void registerObserver(BluetoothMnsObexClient mns, int masId) {
		if (V) Log.d(TAG, "registerObserver");
		/* Use MmsSms Uri since the Sms Uri is not notified on deletes */
		mMasId = masId;
		mMnsClient = mns;
		mResolver.registerContentObserver(MmsSms.CONTENT_URI, false, mObserver);
	}

	public void unregisterObserver() {
		if (V) Log.d(TAG, "unregisterObserver");
		mResolver.unregisterContentObserver(mObserver);
		mMnsClient = null;
	}

	private void sendEvent(Event evt) {
		Log.d(TAG, "sendEvent: " + evt.eventType + " " + evt.handle + " "
				+ evt.folder + " " + evt.oldFolder + " " + evt.msgType.name());
		if (mMnsClient == null) {
			Log.d(TAG, "sendEvent: No MNS client registered - don't send event");
			return;
		}
		try {
			mMnsClient.sendEvent(evt.encode(), mMasId);
		} catch (UnsupportedEncodingException ex) {
			/* do nothing */
		}
	}



	private void SendMessage() {
		long id = 123456678;
		Event evt = new Event("NewMessage", id, "inbox",
				null, mSmsType);
		sendEvent(evt);
	}


	private void handleMsgListChanges() {
		SendMessage();
	}

}
