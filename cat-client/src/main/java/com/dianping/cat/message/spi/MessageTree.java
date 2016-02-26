package com.dianping.cat.message.spi;

import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;

public interface MessageTree extends Cloneable {
	public boolean canDiscard();

	public MessageTree copy();

	public String getDomain();

	public List<Event> getEvents();

	public List<Heartbeat> getHeartbeats();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public List<Metric> getMetrics();

	public String getParentMessageId();

	public String getRootMessageId();

	public String getSessionToken();

	public String getThreadGroupName();

	public String getThreadId();

	public String getThreadName();

	public List<Transaction> getTransactions();

	public boolean isProcessLoss();

	public void setDomain(String domain);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setParentMessageId(String parentMessageId);

	public void setProcessLoss(boolean loss);

	public void setRootMessageId(String rootMessageId);

	public void setDiscard(boolean discard);

	public void setSessionToken(String sessionToken);

	public void setThreadGroupName(String name);

	public void setThreadId(String threadId);

	public void setThreadName(String id);

}
