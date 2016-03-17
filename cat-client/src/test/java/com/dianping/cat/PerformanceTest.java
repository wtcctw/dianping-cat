package com.dianping.cat;

import java.text.DecimalFormat;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.Transaction;

public class PerformanceTest {

	public static int m_totalTime;

	// 每个线程执行的cat api次数
	public static int m_perTheadExecuteTime = 10000;

	private static void doBussniess(int avg) throws InterruptedException {
		Thread.sleep(avg);
	}

	public static void test(int index, int avg) throws InterruptedException {
		long total = 0;

		for (int i = 1; i < m_perTheadExecuteTime; i++) {
			long start = System.currentTimeMillis(); // 记录开始时间
			Transaction t0 = Cat.newTransaction("test", "test0");
			Transaction t1 = Cat.newTransaction("test", "test1");
			Transaction t2 = Cat.newTransaction("test", "test2");
			Transaction t3 = Cat.newTransaction("test", "test3");
			Transaction t4 = Cat.newTransaction("test", "test4");
			Transaction t5 = Cat.newTransaction("test", "test5");
			Transaction t6 = Cat.newTransaction("test", "test6");
			Transaction t7 = Cat.newTransaction("test", "test7");
			Transaction t8 = Cat.newTransaction("test", "test8");
			Transaction t9 = Cat.newTransaction("test", "test9");

			long bussinessStart = System.currentTimeMillis(); // 记录业务开始时间
			doBussniess(avg);
			long duration = System.currentTimeMillis() - bussinessStart; // 记录业务结束时间

			t0.setStatus(Transaction.SUCCESS);
			t1.setStatus(Transaction.SUCCESS);
			t2.setStatus(Transaction.SUCCESS);
			t3.setStatus(Transaction.SUCCESS);
			t4.setStatus(Transaction.SUCCESS);
			t5.setStatus(Transaction.SUCCESS);
			t6.setStatus(Transaction.SUCCESS);
			t7.setStatus(Transaction.SUCCESS);
			t8.setStatus(Transaction.SUCCESS);
			t9.setStatus(Transaction.SUCCESS);
			t9.complete();
			t8.complete();
			t7.complete();
			t6.complete();
			t5.complete();
			t4.complete();
			t3.complete();
			t2.complete();
			t1.complete();
			t0.complete();

			total = total + System.currentTimeMillis() - start - duration; // 记录一次10次 cat api消耗的时间
			if (i % 100 == 0) { // 每次100次打印一次cat的平均消耗

				DecimalFormat df = new DecimalFormat("0.00");
				System.err.println("thread index " + index + " total time : " + total + "(ms)  cat cost:"
				      + (df.format((double) total / i)) + "(ms)");
			}
		}

		m_perTheadExecuteTime = m_perTheadExecuteTime + (int) total;

	}

	@Before
	public void setUp() {
		Transaction t = Cat.newTransaction("PerformanceTest", "PerformanceTest");

		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	@Test
	public void testMuliThread() throws InterruptedException {
		int totalThreadCount = 200;

		for (int i = 0; i < totalThreadCount; i++) {
			Threads.forGroup("cat").start(new TestThread(i));
		}

		Thread.sleep(1000000);
	}

	public class TestThread implements Task {

		private int m_index;

		public TestThread(int index) {
			m_index = index;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void run() {
			try {
				// 业务代码的sleep时间
				test(m_index, 50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void shutdown() {

		}
	}
}
