package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.DefaultBlockDumper;
import org.unidal.cat.message.storage.local.DefaultBlockWriter;
import org.unidal.cat.message.storage.local.DefaultMessageDumper;
import org.unidal.cat.message.storage.local.DefaultMessageProcessor;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.hadoop.hdfs.bucket.HarfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucket;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.message.storage.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineHdfsComponents());
		all.addAll(defineLocalComponents());
		return all;
	}

	public List<Component> defineHdfsComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(FileSystemManager.class) //
		      .req(ServerConfigManager.class));

		all.add(C(HdfsUploader.class) //
		      .req(FileSystemManager.class, ServerConfigManager.class));

		all.add(C(MessageBucket.class, HdfsMessageBucket.ID, HdfsMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(FileSystemManager.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));

		all.add(C(MessageBucket.class, HarfsMessageBucket.ID, HarfsMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(FileSystemManager.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));

		all.add(C(MessageBucketManager.class, HdfsMessageBucketManager.ID, HdfsMessageBucketManager.class) //
		      .req(FileSystemManager.class, ServerConfigManager.class) //
		      .req(PathBuilder.class));

		return all;
	}

	public List<Component> defineLocalComponents() {
		List<Component> all = new ArrayList<Component>();
		String local = "local";

		all.add(C(MessageDumperManager.class));
		all.add(C(MessageDumper.class, DefaultMessageDumper.class).req(BlockDumperManager.class)
		      .req(BucketManager.class, local).is(PER_LOOKUP));
		all.add(C(MessageProcessor.class, DefaultMessageProcessor.class).req(BlockDumperManager.class).is(PER_LOOKUP));
		all.add(C(BlockDumper.class, DefaultBlockDumper.class).is(PER_LOOKUP));
		all.add(C(BlockDumperManager.class));
		all.add(C(BlockWriter.class, DefaultBlockWriter.class).req(BucketManager.class, local).is(PER_LOOKUP));
		all.add(C(BucketManager.class, local, LocalBucketManager.class).req(FileBuilder.class, local));
		all.add(C(Bucket.class, local, LocalBucket.class).req(FileBuilder.class, local).is(PER_LOOKUP));
		all.add(C(FileBuilder.class, local, LocalFileBuilder.class).req(StorageConfiguration.class));
		all.add(C(StorageConfiguration.class, DefaultStorageConfiguration.class));

		return all;
	}

}
