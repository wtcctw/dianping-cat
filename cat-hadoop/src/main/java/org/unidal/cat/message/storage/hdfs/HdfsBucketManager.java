package org.unidal.cat.message.storage.hdfs;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
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
	private FileSystemManager m_manager;

	@Inject(PlainTextMessageCodec.ID)
	private MessageCodec m_plainTextCodec;

	@Inject
	private MessageConsumerFinder m_consumerFinder;

	private Map<String, HdfsBucket> m_buckets = new LinkedHashMap<String, HdfsBucket>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, HdfsBucket> eldest) {
			return size() > 500;
		}
	};

	protected Logger m_logger;

	@Override
	public void closeBuckets(int hour) {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void initialize() throws InitializationException {
	}

	@Override
	public MessageTree loadMessage(String messageId) {
		if (m_serverConfigManager.isHdfsOn()) {
			Transaction t = Cat.newTransaction("BucketService", getClass().getSimpleName());
			t.setStatus(Message.SUCCESS);

			try {
				MessageId id = MessageId.parse(messageId);
				List<String> ips = m_consumerFinder.findConsumerIps(id);

				t.addData(ips.toString());

				return readMessage(id, ips);
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

	private MessageTree readMessage(MessageId id, List<String> ips) {
		for (String ip : ips) {
			String domain = id.getDomain();
			int hour = id.getHour();
			String key = domain + '-' + ip + '-' + hour;

			try {
				HdfsBucket bucket = m_buckets.get(key);

				if (bucket == null) {
					synchronized (m_buckets) {
						bucket = m_buckets.get(key);

						if (bucket == null) {
							bucket = (HdfsBucket) lookup(Bucket.class, HdfsBucket.ID);

							bucket.initialize(domain, ip, hour);
							m_buckets.put(key, bucket);
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

}
