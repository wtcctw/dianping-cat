package com.dianping.cat.alarm.spi.dx.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MtApiUtil {
	private static DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
	static {
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public static String getDateString(Date date) {
		return df.format(date);
	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public static String getSignature(String data, String key) {
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			return Base64.encode(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMAC : " + e.getMessage());
		}
	}
}