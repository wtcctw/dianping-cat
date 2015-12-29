package com.dianping.cat.metric;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

public interface DataSourceService<T> extends Initializable {

	public T getConnection(String category);

	public void refresh();

}
