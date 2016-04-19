package org.unidal.cat.message.storage;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TokenMappingTest extends ComponentTestCase {

	@Before
	public void before() {
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(baseDir);
	}

	@Test
	public void test() throws IOException {
		TokenMapping mapping = lookup(TokenMapping.class, "local");
		int hour = 405845;

		for (int times = 0; times < 3; times++) {
			mapping.open(hour, "127.0.0.1");

			for (int i = 0; i < 64 * 1024; i++) {
				String expected = "token-mapping-" + i;
				int index = mapping.map(expected);
				String actual = mapping.lookup(index);

				Assert.assertEquals(i + 1, index);
				Assert.assertEquals(expected, actual);
			}

			mapping.close();
		}
	}

	@Test
	public void testMany() throws IOException {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));

		TokenMapping mapping = lookup(TokenMapping.class, "local");
		int hour = 405845;

		for (int times = 0; times < 3; times++) {
			mapping.open(hour, "127.0.0.1");

			for (int i = 0; i < 64 * 1024 * 10; i++) {
				String expected = "token-mapping-" + i;
				int index = mapping.map(expected);
				String actual = mapping.lookup(index);

				Assert.assertEquals(i + 1, index);
				Assert.assertEquals(expected, actual);
			}

			mapping.close();
		}
	}

}
