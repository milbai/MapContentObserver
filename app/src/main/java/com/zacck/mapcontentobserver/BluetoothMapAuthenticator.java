package com.zacck.mapcontentobserver;

/**
 * Created by Zacck on 3/16/2016.
 */
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import javax.obex.Authenticator;
import javax.obex.PasswordAuthentication;
/**
 * BluetoothMapAuthenticator is a used by BluetoothObexServer for obex
 * authentication procedure.
 */
public class BluetoothMapAuthenticator implements Authenticator {
	private static final String TAG = "BluetoothMapAuthenticator";
	private boolean mChallenged;
	private boolean mAuthCancelled;
	private String mSessionKey;
	private Handler mCallback;
	public BluetoothMapAuthenticator(final Handler callback) {
		mCallback = callback;
		mChallenged = false;
		mAuthCancelled = false;
		mSessionKey = null;
	}
	public final synchronized void setChallenged(final boolean bool) {
		mChallenged = bool;
	}
	public final synchronized void setCancelled(final boolean bool) {
		mAuthCancelled = bool;
	}
	public final synchronized void setSessionKey(final String string) {
		mSessionKey = string;
	}
	private void waitUserConfirmation() {
		Message msg = Message.obtain(mCallback);
		msg.what = BluetoothMapService.MSG_OBEX_AUTH_CHALL;
		msg.sendToTarget();
		synchronized (this) {
			while (!mChallenged && !mAuthCancelled) {
				try {
					wait();
				} catch (InterruptedException e) {
					Log.e(TAG, "Interrupted while waiting on isChallenged");
				}
			}
		}
	}
	public PasswordAuthentication onAuthenticationChallenge(final String description,
															final boolean isUserIdRequired, final boolean isFullAccess) {
		waitUserConfirmation();
		if (mSessionKey.trim().length() != 0) {
			PasswordAuthentication pa = new PasswordAuthentication(null, mSessionKey.getBytes());
			return pa;
		}
		return null;
	}
	// TODO: Reserved for future use only, in case MSE challenge MCE
	public byte[] onAuthenticationResponse(final byte[] userName) {
		byte[] b = null;
		return b;
	}
}
