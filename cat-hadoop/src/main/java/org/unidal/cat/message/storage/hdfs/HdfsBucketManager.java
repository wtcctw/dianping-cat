package org.unidal.cat.message.storage.hdfs;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

@Named(type = BucketManager.class, value = HdfsBucket.ID)
public class HdfsBucketManager extends ContainerHolder implements BucketManager, Initializable, LogEnabled {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private PathBuilder m_pathBuilder;

	@Inject
	private FileSystemManager m_manager;

	@Inject(PlainTextMessageCodec.ID)
	private MessageCodec m_plainTextCodec;

	private Map<String, HdfsBucket> m_buckets = new ConcurrentHashMap<String, HdfsBucket>();

	private Logger m_logger;

	@Override
	public void closeBuckets(int hour) {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private List<String> filterFiles(FileSystem fs, String domain, final String base, final String path) {
		final List<String> paths = new ArrayList<String>();

		try {
			final Path basePath = new Path(base + path);
			final String key = domain;

			if (fs != null) {
				fs.listStatus(basePath, new PathFilter() {
					@Override
					public boolean accept(Path p) {
						String name = p.getName();

						if (name.contains(key) && name.endsWith(".dat")) {
							paths.add(path + name.substring(0, name.length() - 4));
						}
						return false;
					}
				});
			}
		} catch (IOException e) {
			Cat.logError(e);
		}
		return paths;
	}

	@Override
	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isHdfsOn()) {
			Threads.forGroup("cat").start(new IdleChecker());
		}
	}

	private List<String> loadFileFromHdfs(MessageId id, Date date) throws IOException {
		StringBuilder sb = new StringBuilder();
		String p = m_pathBuilder.getLogviewPath(date, "");
		FileSystem fs = m_manager.getFileSystem(ServerConfigManager.DUMP_DIR, sb);
		List<String> paths = filterFiles(fs, id.getDomain(), sb.toString(), p);

		return paths;
	}

	@Override
	public MessageTree loadMessage(String messageId) {
		if (m_serverConfigManager.isHdfsOn()) {
			Transaction t = Cat.newTransaction("BucketService", getClass().getSimpleName());
			t.setStatus(Message.SUCCESS);

			try {
				MessageId id = MessageId.parse(messageId);
				Date date = new Date(id.getTimestamp());
				List<String> paths = loadFileFromHdfs(id, date);

				t.addData(paths.toString());

				return readMessage(id, date, paths);
			} catch (RuntimeException e) {
				t.setStatus(e);
				Cat.logError(e);
				throw e;
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
		return null;
	}

	private MessageTree readMessage(MessageId id, Date date, List<String> paths) {
		for (String dataFile : paths) {
			try {
				HdfsBucket bucket = m_buckets.get(dataFile);

				if (bucket == null) {
					synchronized (m_buckets) {
						bucket = m_buckets.get(dataFile);

						if (bucket == null) {
							bucket = (HdfsBucket) lookup(Bucket.class, HdfsBucket.ID);

							bucket.initialize(dataFile);
							m_buckets.put(dataFile, bucket);
						}
					}
				}

				if (bucket != null) {
					ByteBuf data = bucket.get(id);

					if (data != null) {
						try {
							MessageTree tree = m_plainTextCodec.decode(data);

							if (tree.getMessageId().equals(id.toString())) {
								return tree;
							}
						} finally {
							m_plainTextCodec.reset();
						}
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return null;
	}

	private class IdleChecker implements Task {
		private void closeIdleBuckets() throws IOException {
			long now = System.currentTimeMillis();
			long hour = 3600 * 1000L;
			Set<String> closed = new HashSet<String>();

			for (Entry<String, HdfsBucket> entry : m_buckets.entrySet()) {
				HdfsBucket bucket = entry.getValue();

				if (now - bucket.getLastAccessTime() >= hour) {
					try {
						bucket.close();
						closed.add(entry.getKey());
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
			for (String close : closed) {
				HdfsBucket bucket = m_buckets.remove(close);

				m_logger.info("Close bucket " + bucket);
				release(bucket);
			}
		}

		@Override
		public String getName() {
			return "HdfsBucketManager-IdleChecker";
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(60 * 1000L); // 1 minute

					try {
						closeIdleBuckets();
					} catch (IOException e) {
						Cat.logError(e);
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
