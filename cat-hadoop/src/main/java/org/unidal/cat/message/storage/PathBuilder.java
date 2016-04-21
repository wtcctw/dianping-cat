package org.unidal.cat.message.storage;

import java.util.Date;

public interface PathBuilder {
	public String getPath(String domain, Date startTime, String ip, FileType type);

	public static enum FileType {
		MAPPING("map"),

		TOKEN("token"),

		INDEX("idx"),

		DATA("dat");

		private String m_extension;

		private FileType(String extension) {
			m_extension = extension;
		}

		public String getExtension() {
			return m_extension;
		}
	}
}
