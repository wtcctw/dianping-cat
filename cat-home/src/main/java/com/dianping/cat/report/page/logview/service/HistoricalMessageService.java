package com.dianping.cat.report.page.logview.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalMessageService extends BaseHistoricalModelService<String> {

	@Inject(HtmlMessageCodec.ID)
	private MessageCodec m_html;

	@Inject(WaterfallMessageCodec.ID)
	private MessageCodec m_waterfall;

	@Inject(PlainTextMessageCodec.ID)
	private MessageCodec m_plainText;

	@Inject(HdfsBucket.ID)
	private BucketManager m_bucketManager;

	public HistoricalMessageService() {
		super("logview");
	}

	@Override
	protected String buildModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		MessageId id = MessageId.parse(messageId);
		MessageTree tree = null;

		try {
			Bucket bucket = m_bucketManager.getBucket(id.getDomain(),
			      NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), id.getHour(), true);

			if (bucket != null) {
				ByteBuf byteBuf = bucket.get(id);

				if (byteBuf != null) {
					tree = m_plainText.decode(byteBuf);
				}
			}
		} finally {
			m_plainText.reset();
		}

		if (tree != null) {
			return toString(request, tree);
		} else {
			return null;
		}
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		boolean eligibale = request.getPeriod().isHistorical();

		return eligibale;
	}

	protected String toString(ModelRequest request, MessageTree tree) {
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

		if (tree.getMessage() instanceof Transaction && request.getProperty("waterfall", "false").equals("true")) {
			m_waterfall.encode(tree, buf);
		} else {
			m_html.encode(tree, buf);
		}

		try {
			buf.readInt(); // get rid of length
			return buf.toString(Charset.forName("utf-8"));
		} catch (Exception e) {
			// ignore it
		}

		return null;
	}
}
