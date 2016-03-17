package org.unidal.cat.message.storage.local;

public class QueueFullException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public QueueFullException(String msg) {
		super(msg);
	}

}
