package org.unidal.cat.message;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.message.storage.IndexManagerTest;
import org.unidal.cat.message.storage.IndexTest;
import org.unidal.cat.message.storage.TokenMapTest;

@RunWith(Suite.class)
@SuiteClasses({

MessageIdTest.class,

BenchmarkTest.class,

IndexManagerTest.class,

IndexTest.class,

TokenMapTest.class,

IndexTest.class,

})
public class AllMessageTests {

}
