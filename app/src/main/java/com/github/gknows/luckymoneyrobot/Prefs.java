package com.github.gknows.luckymoneyrobot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressLint("CommitPrefEdits")
public class Prefs {
	private final SharedPreferences prefs;
	private final Editor edit;
	private final TelephonyManager tm;
	static final String magicNum = "LuCkYm0nEyKeY";
	static final String DELAY_TIME = "DelayTime";

	protected Prefs(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		edit = prefs.edit();
		tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	protected void setDelayTimems(int ms) {
		edit.putInt(DELAY_TIME, ms);
		edit.commit();
	}

	protected int getDelayTimems() {
		return prefs.getInt(DELAY_TIME, 999);
	}

	protected boolean setSerialNum(String sn) {
		if(checkSerialNum(sn)) {
			edit.putString("SN", sn);
			edit.commit();
			return true;
		}
		return false;
	}

	protected String getSerialNum() {
		return prefs.getString("SN", "");
	}

	protected boolean isRegisterd() {
		String sn = getSerialNum();
		return checkSerialNum(sn);
	}

	protected String getDeviceNum() {
		String id = tm.getDeviceId();
		id = id + magicNum;
		return stringToMD5(id).substring(3, 22);
	}

	private boolean checkSerialNum(String sn) {
		if(sn == null) {
			return false;
		}
		String id = getDeviceNum() + magicNum;

		if(sn.equals(stringToMD5(id).substring(4, 17))) {
			return true;
		}
		return false;
	}

	public static String stringToMD5(String string) {
		byte[] hash;

		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}

		return hex.toString();
	}
}
