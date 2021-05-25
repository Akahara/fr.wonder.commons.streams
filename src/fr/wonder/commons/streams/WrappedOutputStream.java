package fr.wonder.commons.streams;

import java.io.IOException;
import java.io.OutputStream;

public class WrappedOutputStream {
	
	private final OutputStream stream;
	
	public WrappedOutputStream(OutputStream stream) {
		this.stream = stream;
	}
	
	public void write(int i) throws IOException {
		stream.write(i);
	}
	
	public void write(byte[] bytes) throws IOException {
		stream.write(bytes);
	}
	
	public void write(byte[] bytes, int off, int len) throws IOException {
		stream.write(bytes, off, len);
	}
	
	public void writeChar(char c) throws IOException {
		StreamUtils.writeChar(stream, c);
	}
	
	public void writeByte(byte b) throws IOException {
		StreamUtils.writeByte(stream, b);
	}
	
	public void writeShort(short s) throws IOException {
		StreamUtils.writeShort(stream, s);
	}
	
	public void writeInt(int i) throws IOException {
		StreamUtils.writeInt(stream, i);
	}
	
	public void writeLong(long l) throws IOException {
		StreamUtils.writeLong(stream, l);
	}
	
	public void writeFloat(OutputStream stream, float f) throws IOException {
		StreamUtils.writeFloat(stream, f);
	}
	
	public void writeDouble(double d) throws IOException {
		StreamUtils.writeDouble(stream, d);
	}
	
	public void writeString(String s) throws IOException {
		StreamUtils.writeString(stream, s);
	}
	
	public void writePacket(byte[] bytes) throws IOException {
		StreamUtils.writePacket(stream, bytes);
	}
	
}
