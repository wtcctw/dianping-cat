package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

class CodecComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(EscapingBufferWriter.class));

		all.add(A(PlainTextMessageCodec.class));

		return all;
	}
}
