package org.commonsemantics.grails.security.oauth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Copy of https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/common/util/SerializationUtils.java
 * Added to allow the class loader to dynamically find the GrailsUser class when it
 * is deserialised from the database. */
public final class SerializationUtils {
	
	private SerializationUtils( ) { }

	public static byte[] serialize(final Object state) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(state);
			oos.flush();
			return bos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			if (oos != null) {
				try {
					oos.close();
				}
				catch (IOException e) {
					// eat it
				}
			}
		}
	}

	public static <T> T deserialize(final byte[] byteArray) {
		ObjectInputStream oip = null;
		try {
			oip = new ObjectInputStream(new ByteArrayInputStream(byteArray));
			@SuppressWarnings("unchecked")
			T result = (T) oip.readObject();
			return result;
		}
		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			if (oip != null) {
				try {
					oip.close();
				}
				catch (IOException e) {
					// eat it
				}
			}
		}
	}

};
