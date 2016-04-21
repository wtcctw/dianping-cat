package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.LogviewProcessor;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultByteBufCache;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.hadoop.hdfs.bucket.HarfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucket;

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

		all.add(A(FileSystemManager.class));

		all.add(A(HdfsUploader.class));

		all.add(A(HdfsMessageBucket.class));

		all.add(A(HarfsMessageBucket.class));

		all.add(A(HdfsMessageBucketManager.class));

		

		return all;
	}

	public List<Component> defineLocalComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultMessageDumperManager.class));
		all.add(A(DefaultMessageFinderManager.class));
		all.add(A(DefaultMessageDumper.class));
		all.add(A(DefaultMessageProcessor.class));
		all.add(A(DefaultBlockDumperManager.class));
		all.add(A(DefaultBlockDumper.class));
		all.add(A(DefaultBlockWriter.class));

		all.add(A(LocalBucketManager.class));
		all.add(A(LocalBucket.class));
		all.add(A(HdfsBucket.class));
		all.add(A(HdfsBucketManager.class));
		
		all.add(A(LocalIndexManager.class));
		all.add(A(LocalIndex.class));
		all.add(A(HdfsIndex.class));

		all.add(A(LocalFileBuilder.class));
		all.add(A(LocalTokenMapping.class));
		all.add(A(LocalTokenMappingManager.class));

		all.add(A(DefaultStorageConfiguration.class));

		all.add(A(LogviewProcessor.class));
		
		all.add(A(DefaultByteBufCache.class));
		all.add(A(DefaultStorageConfiguration.class));

		
		return all;
	}

}
