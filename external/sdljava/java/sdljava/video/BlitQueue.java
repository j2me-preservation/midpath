package sdljava.video;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ByteOrder;

import sdljava.x.swig.SWIG_SDLVideo;

public class BlitQueue {

    SDLSurface src;
    IntBuffer srcX;
    IntBuffer srcY;
    IntBuffer srcWidth;
    IntBuffer srcHeight;
    
    IntBuffer dstX;
    IntBuffer dstY;
    IntBuffer dstWidth;
    IntBuffer dstHeight;
    
    int index = 0;
    
    /**
     * Constructs a new BlitQueue
     *
     * @param src   The src Surface
     * @param count The size of the Queue
     */
    public BlitQueue(SDLSurface src, int count) {
	this.src = src;
	System.out.println("count=" + count);

	ByteBuffer buf = null;

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.srcX = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.srcY = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.srcWidth  = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.srcHeight = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.dstX = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.dstY = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.dstWidth  = buf.asIntBuffer();

	buf = ByteBuffer.allocateDirect(count*4);
	buf.order(ByteOrder.nativeOrder());
	this.dstHeight = buf.asIntBuffer();
    }

    public void queueBlit(SDLRect srcRect, SDLSurface dstSurface, SDLRect dst) {
	if (index >= srcX.capacity()) {
	    throw new IllegalArgumentException("blit queue already full! index=" + index + ", size=" + srcX.capacity());
	}
	System.out.println("queueBlit: srcRect=" + srcRect + ", dstRect=" + dst);
	
	if (srcRect == null) {
	    srcX.put     ( index, 0 );
	    srcY.put     ( index, 0 );
	    srcWidth.put ( index, src.getWidth() );
	    srcHeight.put( index, src.getHeight() );
	}
	else {
	    srcX.put      ( index, srcRect.x );
	    srcY.put      ( index, srcRect.y );
	    srcWidth.put  ( index, srcRect.width );
	    srcHeight.put ( index, srcRect.height );
	}

	if (dst == null) {
	    dstX.put     ( index, 0 );
	    dstY.put     ( index, 0 );
	    dstWidth.put ( index, -1 );
	    dstHeight.put( index, -1 );
	}
	else {
	    dstX.put      ( index, dst.x );
	    dstY.put      ( index, dst.y );
	    dstWidth.put  ( index, dst.width );
	    dstHeight.put ( index, dst.height );
	}

	index += 1;
    }

    public void queueBlit(SDLSurface dstSurface, SDLRect dst) {
	queueBlit(null, dstSurface, dst);
    }

    public void queueBlit(SDLSurface dstSurface) {
	queueBlit(null, dstSurface, null);
    }

    public void flush(SDLSurface dst) {
	SWIG_SDLVideo.SWIG_executeBlitQueue(srcX, srcY, srcWidth, srcHeight, src.getSwigSurface(),
					    dstX, dstY, dstWidth, dstHeight, dst.getSwigSurface(),
					    srcX.capacity());
	clear();
    }

    public void clear() {
	srcX.clear();
	srcY.clear();
	dstX.clear();
	dstY.clear();
	
	index = 0;
    }
}