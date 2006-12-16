package sdljava.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Some usefull methods for reading URLs or streams and returning the data as Direct Buffers
 *
 * @author Ivan Z. Ganza
 * @author Robert Schuster
 * @author Bart LEBOEUF
 * @version $Id: BufferUtil.java,v 1.9 2005/09/11 05:19:37 ivan_ganza Exp $
 */
public class BufferUtil {

    // byte   8 bits
    // short 16 bits
    // int   32 bits
    // long  64 bits
    // char  16 bits
    // float 32 bits
    // double 64 bits

    private static final int BUFFER_SIZE = 8192;

    /**
     * Tries to open the given URL, get its input stream, returns the data in a <I><B>direct</I></B> ByteBuffer
     *
     * @param url an <code>URL</code> value
     * @return a <code>ByteBuffer</code> value with the contents of the data present at URL
     * @exception IOException if an error occurs
     * @exception MalformedURLException if an error occurs
     */
    public static ByteBuffer readURL(URL url) throws IOException, MalformedURLException {
	URLConnection connection  = null;
	try {
	    connection = url.openConnection();
	    return readInputStream(new BufferedInputStream(connection.getInputStream()));
	} catch (IOException e) {
	    throw e;
	}
    }

    /**
     * Fully reads the given InputStream, returning its contents as a ByteBuffer
     *
     * @param in an <code>InputStream</code> value
     * @return a <code>ByteBuffer</code> value
     * @exception IOException if an error occurs
     */
    public static ByteBuffer readInputStream(InputStream in) throws IOException {
	/* Converts the InputStream into a channels which allows
	 * us to read into a ByteBuffer.
	 */
	ReadableByteChannel	ch = Channels.newChannel(in);
		
	// Creates a list that stores the intermediate buffers.
	List list = new LinkedList();
	
	// A variable storing the accumulated size of the data.
	int sum = 0, read = 0;
		
	/* Reads all the bytes from the channel and stores them
	 * in various buffer objects.
	 */
	do {
	    ByteBuffer b = createByteBuffer(BUFFER_SIZE);
	    read = ch.read(b);
	
	    if(read > 0) {
		b.flip(); // make ready for reading later
		list.add(b);
		sum += read;
	    }
	} while(read != -1);
	
	/* If there is only one buffer used we do not
	 * need to merge the data.
	 */
	if(list.size() == 1) {
	    return (ByteBuffer) list.get(0);
	}
	
	ByteBuffer bb = createByteBuffer(sum);
	
	/* Merges all buffers into the Buffer bb */
	Iterator ite = list.iterator();
	while(ite.hasNext()){
	    bb.put((ByteBuffer) ite.next());
	}
	
	list.clear();

	// Makes buffer ready for reading later
	bb.flip();
	
	return bb;
    }
    
    /*## Create direct native type buffers ##*/
    
    public static ByteBuffer createByteBuffer(int capacity) {
    	return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
    
    public static ByteBuffer createByteBuffer(byte[] values) {
    	return createByteBuffer(values.length).put(values);
    }

    public static FloatBuffer createFloatBuffer(int capacity) {
	return createByteBuffer(capacity*4).asFloatBuffer();
    }

    public static FloatBuffer createFloatBuffer(float[] values) {
	return createFloatBuffer(values.length).put(values);
    }
    
    public static IntBuffer createIntBuffer(int capacity) {
	return createByteBuffer(capacity*4).asIntBuffer();
    }

    public static IntBuffer createIntBuffer(int[] values) {
	return createIntBuffer(values.length).put(values);
    }

    public static DoubleBuffer createDoubleBuffer(int capacity) {
	return createByteBuffer(capacity*8).asDoubleBuffer();
    }

    public static DoubleBuffer createDoubleBuffer(double[] values) {
	return createDoubleBuffer(values.length).put(values);
    }
    
    public static LongBuffer createLongBuffer(int capacity) {
	return createByteBuffer(capacity*8).asLongBuffer();
    }

    public static LongBuffer createLongBuffer(long[] values) {
	return createLongBuffer(values.length).put(values);
    }
    
    public static ShortBuffer createShortBuffer(int capacity) {
	return createByteBuffer(capacity*2).asShortBuffer();
    }

    public static ShortBuffer createShortBuffer(short[] values) {
	return createShortBuffer(values.length).put(values);
    }
	
    public static CharBuffer createCharBuffer(int capacity) {
	return createByteBuffer(capacity*2).asCharBuffer();
    }
    
    public static CharBuffer createCharBuffer(char[] values) {
	return createCharBuffer(values.length).put(values);
    }
}