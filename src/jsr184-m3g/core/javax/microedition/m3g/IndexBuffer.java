package javax.microedition.m3g;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public abstract class IndexBuffer extends Object3D {
	protected ShortBuffer buffer = null;

	public int getIndexCount()
	{
		return buffer.limit();
	}
	
	public abstract void getIndices(int[] indices);
	
	protected void allocate(int numElements)
	{
		buffer = ByteBuffer.allocateDirect(numElements * 2).asShortBuffer();
		//buffer = BufferUtil.newIntBuffer(numElements);
	}
	
	ShortBuffer getBuffer() {
		return buffer;
	}
}
