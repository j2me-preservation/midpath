package sdljava.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  sdljava - a java binding to the SDL API
 *
 *  Copyright (C) 2004  Ivan Z. Ganza
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA
 *
 *  Ivan Z. Ganza (ivan_ganza@yahoo.com)
 *  Bart LEBOEUF  (bartleboeuf@yahoo.fr)
 */

/**
 * This pool allocate native direct ByteBuffer class objects. 
 * It always tries to find a free buffer in the pool before trying create a new one.
 * 
 * @author Bart LEBOEUF
 * @version $Id: ByteBufferPool.java,v 1.1 2005/03/13 09:26:39 doc_alton Exp $
 */
public final strictfp class ByteBufferPool {

	// There is no point in allocating buffers smaller than 4K,
	// as direct ByteBuffers are page-aligned to the underlying
	// system, which is 4096 byte pages under most OS's.
	// If we want to save memory, we can distribute smaller-than-4K
	// buffers by using the slice() method to break up a standard buffer
	// into smaller chunks, but that's more work.
	private static final int START_POWER = 12; // 4096

	private static final int END_POWER = 25; // 33554432

	public static final int BLOCK_SIZE = 16384;

	// without an extra bucket here we get lots of wastage with the file cache
	// as typically
	// 16K data reads result in a buffer slightly bigger than 16K due to
	// protocol header
	// This means we would bump up to 32K pool entries, hence wasting 16K per
	// 16K entry
	private static final int[] EXTRA_BUCKETS = { 128, BLOCK_SIZE + 128 };

	public static final int MAX_SIZE = BigInteger.valueOf(2).pow(END_POWER).intValue();
	
	private static final long COMPACTION_CHECK_PERIOD = 2 * 60 * 1000; //2Min

	private static final long MAX_FREE_BYTES = 10 * 1024 * 1024; //10 MB
	
	private static ByteBufferPool pool = new ByteBufferPool();

	private Map buffersMap = new LinkedHashMap(END_POWER - START_POWER + 1);

	private static final Object poolsLock = new Object();

	private HashMap in_use_counts = new HashMap();

	/**
	 * Constructor
	 */
	private ByteBufferPool() {
		//create the buffer pool for each buffer size
		ArrayList list = new ArrayList();
		for (int p = START_POWER; p <= END_POWER; p++) {
			list.add(new Integer(BigInteger.valueOf(2).pow(p).intValue()));
		}
		for (int i = 0; i < EXTRA_BUCKETS.length; i++) {
			list.add(new Integer(EXTRA_BUCKETS[i]));
		}
		Integer[] sizes = (Integer[])list.toArray(new Integer[0]);
		Arrays.sort(sizes);
		for (int i = 0; i < sizes.length; i++) {
			ArrayList bufferPool = new ArrayList();
			buffersMap.put(sizes[i], bufferPool);
		}
		// Initiate periodic task to check free memory usage
	    Scheduler.getInstance().addTask(COMPACTION_CHECK_PERIOD,new Task(
		    new TaskPerformer() {
		    public void perform() {
		      checkMemoryUsage();
		    }
		  }));

	}

	/**
	 * Allocate and return a new direct ByteBuffer.
	 */
	private ByteBuffer allocateNewBuffer(final int _size) {
		try {
			return ByteBuffer.allocateDirect(_size);
		} catch (OutOfMemoryError e) {
			clearBufferPools();
			runGarbageCollection();
			try {
				return ByteBuffer.allocateDirect(_size);
			} catch (OutOfMemoryError ex) {
				String msg = "Memory allocation failed: Out of direct memory space.\n"
						+ "To fix: Use the -XX:MaxDirectMemorySize=512m command line option,\n"
						+ "or upgrade your Java JRE to version 1.4.2_05 or 1.5 series or newer.";
				System.err.println(msg);
				throw (ex);
			}
		}
	}

	/**
	 * Retrieve a buffer from the buffer pool of size at least <b>length </b>,
	 * and no larger than <b>ByteBufferPool.MAX_SIZE </b>
	 */
	public static ByteBuffer getBuffer(int _length, ByteOrder byteOrder) throws IOException {
		if (_length < 1) {
			throw new IOException("requested length [" + _length + "] < 1");
		}

		if (_length > MAX_SIZE) {
			throw new IOException("requested length [" + _length
					+ "] > MAX_SIZE [" + MAX_SIZE + "]");
		}
		return pool.getBufferHelper(_length).order(byteOrder);
	}

	/**
	 * Retrieve an appropriate buffer from the free pool, or create a new one if
	 * the pool is empty.
	 */
	private ByteBuffer getBufferHelper(int _length) {
		Integer reqVal = new Integer(_length);
		//loop through the buffer pools to find a buffer big enough
		Iterator it = buffersMap.keySet().iterator();
		while (it.hasNext()) {
			Integer keyVal = (Integer) it.next();
			//check if the buffers in this pool are big enough
			if (reqVal.compareTo(keyVal) <= 0) {
				ArrayList bufferPool = (ArrayList) buffersMap.get(keyVal);
				ByteBuffer buff;
				synchronized (poolsLock) {
					//make sure we don't remove a buffer when running compaction
					//if there are no free buffers in the pool, create a new one.
					//otherwise use one from the pool
					if (bufferPool.isEmpty()) {
						buff = allocateNewBuffer(keyVal.intValue());
					} else {
						synchronized (bufferPool) {
							buff = (ByteBuffer) bufferPool.remove(bufferPool.size() - 1);
						}
					}
				}

				// clear doesn't actually zero the data, it just sets pos to 0
				buff.clear(); //scrub the buffer
				buff.limit(_length);
				return buff;
			}
		}
		//we should never get here
		throw (new RuntimeException("Unable to find an appropriate buffer pool"));
	}

	/**
	 * Return the given buffer to the appropriate pool.
	 */
	private static void free(ByteBuffer _buffer) {
		Integer buffSize = new Integer(_buffer.capacity());
		ArrayList bufferPool = (ArrayList) pool.buffersMap.get(buffSize);
		if (bufferPool != null) {
			//no need to sync around 'poolsLock', as adding during compaction is ok
			synchronized (bufferPool) {
				bufferPool.add(_buffer);
			}
		} else {
			System.err.println("Invalid buffer given; could not find proper buffer pool");
		}
	}

	/**
	 * Clears the free buffer pools so that currently unused buffers can be
	 * garbage collected.
	 */
	private void clearBufferPools() {
		Iterator it = buffersMap.values().iterator();
		while (it.hasNext()) {
			ArrayList bufferPool = (ArrayList) it.next();
			bufferPool.clear();
		}
	}

	/**
	 * Force system garbage collection.
	 */
	private void runGarbageCollection() {
		System.runFinalization();
		System.gc();
	}

	/**
	 * Checks memory usage of free buffers in buffer pools, and calls the
	 * compaction method if necessary.
	 */
	private void checkMemoryUsage() {
		long bytesUsed = 0;
		synchronized (poolsLock) {
			//count up total bytes used by free buffers
			Iterator it = buffersMap.keySet().iterator();
			while (it.hasNext()) {
				Integer keyVal = (Integer) it.next();
				ArrayList bufferPool = (ArrayList) buffersMap.get(keyVal);
				bytesUsed += keyVal.intValue() * bufferPool.size();
			}
			//compact buffer pools if they use too much memory
			if (bytesUsed > MAX_FREE_BYTES) {
				compactFreeBuffers(bytesUsed);
			}
		}
	}

	/**
	 * Fairly removes free buffers from the pools to limit memory usage.
	 */
	private void compactFreeBuffers(final long bytes_used) {
		final int numPools = buffersMap.size();
		long bytesToFree = 0;
		int maxPoolSize = 0;

		int[] buffSizes = new int[numPools];
		int[] poolSizes = new int[numPools];
		int[] numToFree = new int[numPools];

		//fill size arrays
		int pos = 0;
		Iterator it = buffersMap.keySet().iterator();
		while (it.hasNext()) {
			Integer keyVal = (Integer) it.next();
			ArrayList bufferPool = (ArrayList) buffersMap.get(keyVal);

			buffSizes[pos] = keyVal.intValue();
			poolSizes[pos] = bufferPool.size();
			numToFree[pos] = 0;

			//find initial max value
			if (poolSizes[pos] > maxPoolSize)
				maxPoolSize = poolSizes[pos];
			pos++;
		}

		//calculate the number of buffers to free from each pool
		while (bytesToFree < (bytes_used - MAX_FREE_BYTES)) {
			for (int i = 0; i < numPools; i++) {
				//if the pool size is as large as the current max size
				if (poolSizes[i] == maxPoolSize) {
					//update counts
					numToFree[i]++;
					poolSizes[i]--;
					bytesToFree += buffSizes[i];
				}
			}
			//reduce max size for next round
			maxPoolSize--;
		}

		//free buffers from the pools
		pos = 0;
		it = buffersMap.values().iterator();
		while (it.hasNext()) {
			//for each pool
			ArrayList bufferPool = (ArrayList) it.next();
			synchronized (bufferPool) {
				int size = bufferPool.size();
				//remove the buffers from the end
				for (int i = (size - 1); i >= (size - numToFree[pos]); i--) {
					bufferPool.remove(i);
				}
			}
			pos++;
		}
		runGarbageCollection();
	}

	/**
	 * Give ByteBuffer back to pool
	 * @param buffer the ByteBuffer to free
	 */
	public static void returnBuffer(ByteBuffer buffer) {
		free(buffer);
	}

	private long bytesFree() {
		long bytesUsed = 0;
		synchronized (poolsLock) {
			//count up total bytes used by free buffers
			Iterator it = buffersMap.keySet().iterator();
			while (it.hasNext()) {
				Integer keyVal = (Integer) it.next();
				ArrayList bufferPool = (ArrayList) buffersMap.get(keyVal);
				bytesUsed += keyVal.intValue() * bufferPool.size();
			}
		}
		return bytesUsed;
	}
}