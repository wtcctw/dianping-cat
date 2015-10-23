package com.dianping.cat.hadoop.hdfs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class MessageBlockReader {
	private FSDataInputStream m_indexFile;

	private FSDataInputStream m_dataFile;

	public MessageBlockReader(FileSystem fs, Path basePath, String dataFile) throws IOException {
		m_indexFile = fs.open(new Path(basePath, dataFile + ".idx"));
		m_dataFile = fs.open(new Path(basePath, dataFile));
	}

	public MessageBlockReader(FileSystem fs, String dataFile) throws IOException {
		m_indexFile = fs.open(new Path(dataFile + ".idx"));
		m_dataFile = fs.open(new Path(dataFile));
	}

	public void close() throws IOException {
		synchronized (m_indexFile) {
			m_indexFile.close();
			m_dataFile.close();
		}
	}

	public byte[] readMessage(int index) throws IOException {
		int blockAddress;
		int blockOffset;
		byte[] buf;

		synchronized (m_indexFile) {
			m_indexFile.seek(index * 6L);
			blockAddress = m_indexFile.readInt();
			blockOffset = m_indexFile.readShort() & 0xFFFF;
		}

		synchronized (m_dataFile) {
			m_dataFile.seek(blockAddress);
			buf = new byte[m_dataFile.readInt()];
			m_dataFile.readFully(buf);
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		DataInputStream in = new DataInputStream(new GZIPInputStream(bais));

		try {
			in.skip(blockOffset);

			int len = in.readInt();
			byte[] data = new byte[len];

			in.readFully(data);
			return data;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore it
			}
		}
	}
}
