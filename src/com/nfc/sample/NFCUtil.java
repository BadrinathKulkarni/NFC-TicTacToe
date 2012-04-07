package com.nfc.sample;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class NFCUtil {

	private static final String TAG = "NFCUtil";

	public static NdefMessage[] getNdefMessages(Intent intent) {

		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Log.d(TAG, "ACTION_TECH_DISCOVERED intent.");
		} else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			Log.d(TAG, "Unknown intent. ACTION_TAG_DISCOVERED Discovered.");
		}
		return msgs;
	}

	public static boolean writeTag(NdefMessage message, Tag tag, Context ctx) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					toast("Tag is read-only.", ctx);
					return false;
				}
				if (ndef.getMaxSize() < size) {
					toast("Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.", ctx);
					return false;
				}

				ndef.writeNdefMessage(message);
				toast("Wrote message to pre-formatted tag.", ctx);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						toast("Formatted tag and wrote message", ctx);
						return true;
					} catch (IOException e) {
						toast("Failed to format tag.", ctx);
						return false;
					}
				} else {
					toast("Tag doesn't support NDEF.", ctx);
					return false;
				}
			}
		} catch (Exception e) {
			toast("Failed to write tag", ctx);
		}

		return false;
	}

	public static void toast(String text, Context ctx) {
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}

	public static NdefMessage getIntArrayAsNdef(short tnf, String type,
			String id, int[] payload, String delimiter) {
		byte[] idBytes = id.getBytes();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < payload.length - 1; i++) {
			sb.append(payload[i] + "-");
		}
		sb.append(payload[payload.length - 1] + "");

		Log.d(TAG, "Payload for ndefmsg = "+sb.toString());
		
		byte[] payloadBytes = sb.toString().getBytes();
		byte[] typeBytes = type.getBytes();
		NdefRecord textRecord = new NdefRecord(tnf, typeBytes, idBytes,
				payloadBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}

	public static NdefMessage getStringAsNdef(short tnf, String type,
			String id, String payload) {
		byte[] idBytes = id.getBytes();
		byte[] payloadBytes = payload.getBytes();
		byte[] typeBytes = type.getBytes();
		NdefRecord textRecord = new NdefRecord(tnf, typeBytes, idBytes,
				payloadBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}

}
