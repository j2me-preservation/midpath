package org.thenesis.midpath.io;

import java.io.IOException;

public class RandomAccessArray {

	private static final int DEFAULT_SIZE = 10000;
	private byte[] data;
	private int currentPos = 0;

	public RandomAccessArray() {
		this(DEFAULT_SIZE);
	}

	public RandomAccessArray(int initialSize) {
		data = new byte[initialSize];
	}

	public void seek(int pos) throws IOException {
		if (pos < 0 || pos >= data.length)
			throw new IOException("Can't go to position: " + pos);

		currentPos = pos;
	}

	public void setLength(int length) throws IOException {
		if (length > data.length) {
			throw new IOException("Can't set file length up to its actual size ");
		}
		byte[] newData = new byte[length];
		System.arraycopy(data, 0, newData, 0, length);
		data = newData;
	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		int remain = data.length - currentPos;
		if (remain <= 0)
			return -1;

		if (length > remain)
			length = remain;

		System.arraycopy(data, currentPos, buffer, offset, length);
		currentPos += length;

		return length;
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {

		if ((currentPos + length) > data.length) {
			grow();
		}

		System.arraycopy(bytes, offset, data, currentPos, length);
		currentPos += length;

	}

	public void grow() {
		byte[] newData = new byte[data.length * 2];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	public int getPosition() {
		return currentPos;
	}
	
	public int getLength() {
		return data.length;
	}

}
