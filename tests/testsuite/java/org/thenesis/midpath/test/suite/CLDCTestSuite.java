package org.thenesis.midpath.test.suite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class CLDCTestSuite extends AbstractTestSuite {

	public CLDCTestSuite() {
		super("TestSuite");
	}
	

	/*
	 * Class tests
	 */

	public void testForName() {

		Class c = null;

		try {
			c = Class.forName("org.thenesis.midpath.test.suite.TestClass");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			fail("Can't get a Class from a class name");
			debug(e);
		}

		checkPoint("Class.forName");
		check(c != null);
		//check(c.isAssignableFrom(TestClass.class));

	}

	public void testNewInstance() {

		Object instance = null;

		try {
			instance = TestClass.class.newInstance();
		} catch (Exception e) {
			fail("Can't instantiate a class");
			debug(e);
		} 

		checkPoint("Class.newInstance");
		check(instance != null);
		check(((TestClass) instance).fieldValue, 15);

	}

	public void testIsInstance() {

		Object instance = new TestClass();

		checkPoint("Class.isInstance");
		check(TestClass.class.isInstance(instance));
		check(!TestClass.class.isInstance(new Object()));

	}

	public void testIsAssignable() {

		Object instance = new TestClass();

		checkPoint("Class.isAssignable");
		check(TestClass.class.isAssignableFrom(TestClass.class));
		check(!String.class.isAssignableFrom(TestClass.class));

	}

	public void testIsInterface() {
		checkPoint("Class.isInterface");
		check(TestInterface.class.isInterface());
	}

	public void testIsArray() {

		int[] array = new int[] { 1, 2 };

		checkPoint("Class.isArray");
		check(((Object) array).getClass().isArray());
	}

	public void testGetName() {
		checkPoint("Class.getName");
		check(TestClass.class.getName(), "org.thenesis.midpath.test.suite.TestClass");
	}

	public void testGetResourceAsStream() {

		checkPoint("getResourceAsStream");
		
		// Test 1
		InputStream is = CLDCTestSuite.class.getResourceAsStream("/org/thenesis/midpath/test/suite/file.txt");

		int val = -1;
		try {
			val = is.read();
		} catch (IOException e) {
			fail("Can't get resource as a stream");
			
		}
		check((char) val, '2');

		// Test 2
		is = CLDCTestSuite.class.getResourceAsStream("file.txt");

		try {
			val = is.read();
		} catch (IOException e) {
			fail("Can't get resource as a stream");
			debug(e);
		}
		check((char) val, '2');

	}
	
	public void testIsAlive() {
		
		checkPoint("Thread.isAlive");
		
		Thread thread = new Thread() {
			public void run() {	
			}
		};
		
		thread.start();
		check(thread.isAlive());
		
		// Wait for the thread to die
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		
		check(!thread.isAlive());
		
		/* Test isAlive() on a non-started thread */
		
		thread = new Thread() {
			public void run() {	
			}
		};
		
		check(!thread.isAlive());
	}

	/*
	 * Thread tests
	 */

	public void testWait() {

		long waitTime = 500;

		TestClass thread = new TestClass(waitTime);

		long startTime = System.currentTimeMillis();
		thread.start();
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
		}
		long delta = System.currentTimeMillis() - startTime;

		checkPoint("Object.wait");
		check((delta > 0) && (delta < 1000));

	}
	
	public void testNotify() {
		
		checkPoint("notify");
		
		TestClass thread = new TestClass(2000);
		
		//debug(thread.toString());

		try {
			thread.start();
			check(thread.isAlive());
			Thread.sleep(500);
			thread.release();
			Thread.sleep(500);
			check(!thread.isAlive());
		} catch (InterruptedException e) {
		}

		check(thread.fieldValue, 16);

	}

	/*
	 * Runtime tests
	 */
	public void testFreeMemory() {
		checkPoint("Runtime.freeMemory");
		check(Runtime.getRuntime().freeMemory() > 0);
	}

	public void testTotalMemory() {
		checkPoint("Runtime.totalMemory");
		check(Runtime.getRuntime().totalMemory() > 0);
	}

	public void testGC() {
		checkPoint("Runtime.GC");
		Runtime.getRuntime().gc();
		//FIXME
		check(true);
	}

	/*
	 * String tests
	 */

	public void testStringHashCode() {

		String s = "helloworld";
		char val[] = s.toCharArray();
		int h = 0;
		int len = val.length;

		for (int i = 0; i < len; i++) {
			h = 31 * h + val[i];
		}

		checkPoint("String.hashCode");
		check(s.hashCode(), h);
	}

	public void testIndexOf() {

		String s = "helloworld";
		char v[] = s.toCharArray();
		int count = v.length;
		int max = count;

		int ch = 'w';
		int fromIndex = 0;
		int position = -1;

		if (fromIndex < 0) {
			fromIndex = 0;
		} else if (fromIndex >= count) {
			position = -1;
		}
		for (int i = fromIndex; i < max; i++) {
			if (v[i] == ch) {
				position = i;
			}
		}
		
		checkPoint("String.indexOf");
		check(s.indexOf('w'), position);
		check(s.indexOf('w', 0), position);

	}
	
	public void testLastIndexOf() {
		String s = "helloworld";
		char v[] = s.toCharArray();
		int count = v.length;
		
		int min = 0;
		
		int ch = 'w';
		int fromIndex = 8;
		int position = -1;

        for (int i =((fromIndex >= count) ? count - 1 : fromIndex) ; i >= min ; i--) {
            if (v[i] == ch) {
            	position = i;
            }
        }
        
        checkPoint("String.lastIndexOf");
		check(s.lastIndexOf('w', fromIndex), position);
        
	}
	
	/*
	 * System tests
	 */
	public void testCurrentTimeMillis() {
		
		long time = System.currentTimeMillis();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		long time2 = System.currentTimeMillis();
		long delta = time2 - time;
		
		checkPoint("System.currentTimeMillis");
		check((time > 0) && (time2 > 0) && (delta > 0) && (delta < 1000));
		
	}
	
	public void testArrayCopy() {
		
		int[] array = new int[] { 0, 1, 2, 3, 4, 5 };
		int srcOffset = 2;
		int destOffset = 5;
		int length = 3;
		int[] copy = new int[destOffset + length];

		System.arraycopy(array, srcOffset, copy, destOffset, length);
		
		boolean failed = false;
		for (int i = 0; i < length; i++) {
			failed = (copy[destOffset + i] == array[srcOffset + i]);
			if (failed) break;
		}
		
		checkPoint("System.arraycopy");
		check(failed);
		
	}
	
	public void testSocketConnection() {
		
		checkPoint("javax.microedition.io.Connection");
		
//		String hostname = "www.yahoo.com";
//		int port = 80;
		String hostname = "192.168.1.1";
		int port = 777;
		StreamConnection client;
		
		try {
			client = (StreamConnection) Connector.open("socket://" + hostname + ":" + port);
			
			OutputStream os = client.openOutputStream();
			PrintStream pos = new PrintStream(os);
			InputStream is = client.openInputStream();
			InputStreamReader reader = new InputStreamReader(is);
			
			String request = "GET http://" + hostname + "\r\n";

			//pos.print(request);
			//pos.flush();
			
			byte[] bArray = request.getBytes();
			for (int i = 0; i < bArray.length; i++) {
				os.write(bArray[i]);
			}
			
			// Block until some data are available
			while(is.available() <= 0) {
			}
			debug("available data: " + is.available());
			check(is.available() > 0);
			
			int length = 0;
			char[] buffer = new char[1000];
			StringBuffer response = new StringBuffer();
			
			// Test read(byte[] buf, int off, int len)
			while ((length = reader.read(buffer)) != -1) {
				response.append(buffer);
				System.out.print(buffer);
			}
			
//			// Test read()
//			int val = 0;
//			while ((val = reader.read()) != -1) {
//				response.append((char)val);
//				System.out.print((char)val);
//			}
			
			check(response.toString().indexOf("html") != -1);
			client.close();
		} catch (IOException e) {
			fail("Can't open socket");
			debug(e);
		} 
		
	}


	private void testObjectCreation() {
		checkPoint("Small objects creation");
		verbose("Before: free memory: " + Runtime.getRuntime().freeMemory());
		for (int i = 0; i < 100000; i++) {
			SmallObject object = new SmallObject();
			object.val = i;
		}
		verbose("After: free memory: " + Runtime.getRuntime().freeMemory());
		check(true);
		
		checkPoint("Big objects creation");
		verbose("Before: free memory: " + Runtime.getRuntime().freeMemory());
		for (int i = 0; i < 10000; i++) {
			BigObject object = new BigObject();
			object.val = i;
		}
		verbose("After: free memory: " + Runtime.getRuntime().freeMemory());
		check(true);
		
		/* Now keep the objects in the memory */ 
		
		checkPoint("Small objects creation 2");
		verbose("Before: free memory: " + Runtime.getRuntime().freeMemory());
		Vector list = new Vector(100000);
		for (int i = 0; i < 100000; i++) {
			SmallObject object = new SmallObject();
			object.val = i;
			list.addElement(object);
		}
		verbose("After: free memory: " + Runtime.getRuntime().freeMemory());
		list.removeAllElements();
		System.gc();
		verbose("After GC: free memory: " + Runtime.getRuntime().freeMemory());
		check(true);
		
		checkPoint("Big objects creation 2");
		verbose("Before: free memory: " + Runtime.getRuntime().freeMemory());
		list = new Vector(40);
		for (int i = 0; i < 40; i++) {
			BigObject object = new BigObject();
			object.val = i;
			list.addElement(object);
		}
		verbose("After: free memory: " + Runtime.getRuntime().freeMemory());
		list.removeAllElements();
		System.gc();
		verbose("After GC: free memory: " + Runtime.getRuntime().freeMemory());
		check(true);	
		
		
	}
	
	private void testArrayCreation() {
		checkPoint("Small objects creation");
		
		for (int i = 1; i < 100000; i += 100) {
			int[] array = new int[i];
			array[0] = i;
		}
		
		check(true);
		
		
	}
	
	private void testThreadCreation() {
		
		checkPoint("Thread creation");
		
		
		Vector list = new Vector(40);
		
		for (int i = 1; i < 100; i += 100) {
			TestClass thread = new TestClass(500);
			list.addElement(thread);
			thread.start();
		}
		
		list.removeAllElements();
		System.gc();
		
		check(true);
		
	}

	
	public void testAll() {
		
		// System
		testCurrentTimeMillis();
		testArrayCopy();
		
		// String
		testStringHashCode();
		testIndexOf();
		testLastIndexOf();
		
		// Class
		testForName();
		testNewInstance();
		testIsInstance();
		testIsAssignable();
		testIsInterface();
		testIsArray();
		testGetName();
		testGetResourceAsStream();

		// Thread
		testWait();
		testIsAlive();
		testNotify();

		// Runtime
		testFreeMemory();
		testTotalMemory();
		testGC();
		
		// Socket
		//testSocketConnection();
		
		// Stress tests
//		testObjectCreation();
//		testArrayCreation();
//		testThreadCreation();
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CLDCTestSuite suite = new CLDCTestSuite();
		suite.testAll();
	}

}

interface TestInterface {
	public static final int STATIC_VALUE = 8;
}

class SmallObject {
	
	int[] buf = new int[10];
	int val;
	
}

class BigObject {
	
	int[] buf = new int[100000];
	int val;
}



class TestClass extends Thread implements TestInterface {

	private long waitTime = 0;
	public int fieldValue = 15;

	public TestClass(long waitTime) {
		this.waitTime = waitTime;
	}

	public TestClass() {
	}

	public synchronized void release() {
		notify();
	}

	public void run() {
		
		synchronized (this) {
			try {
				//wait();
				wait(waitTime);
				fieldValue = 16;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
