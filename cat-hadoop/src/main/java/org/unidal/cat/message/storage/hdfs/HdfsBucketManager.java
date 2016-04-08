package org.unidal.cat.message.storage.hdfs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;

@Named(type = BucketManager.class, value = HdfsBucket.ID)
public class HdfsBucketManager extends ContainerHolder implements BucketManager, Initializable, LogEnabled {

	@Inject("local")
	private FileBuilder m_bulider;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	private Map<String, HdfsBucket> m_buckets = new ConcurrentHashMap<String, HdfsBucket>();

	private Logger m_logger;

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
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void closeBuckets(int hour) {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
		if (!m_serverConfigManager.isHdfsOn()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(domain).append("-").append(ip).append("-").append(hour);

		String key = sb.toString();
		HdfsBucket bucket = m_buckets.get(key);

		boolean shouldCreate = createIfNotExists && bucket == null || !createIfNotExists;

		if (shouldCreate) {
			synchronized (m_buckets) {
				bucket = m_buckets.get(domain);

				if (bucket == null) {
					bucket = (HdfsBucket) lookup(Bucket.class, HdfsBucket.ID);

					bucket.initialize(domain, ip, hour);
					m_buckets.put(key, bucket);
				}
			}
		}

		return bucket;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isHdfsOn()) {
			Threads.forGroup("cat").start(new IdleChecker());
		}
	}

	class IdleChecker implements Task {
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
