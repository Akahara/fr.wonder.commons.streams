package fr.wonder.commons.streams;

import java.io.IOException;
import java.io.InputStream;

public class WrappedInputStream {

	private final InputStream stream;
	
	public WrappedInputStream(InputStream stream) {
		this.stream = stream;
	}
	
	public int readInt() throws IOException {
		return StreamUtils.readInt(stream);
	}
	
	public int read() throws IOException {
		return stream.read();
	}
	
	public byte[] readNBytes(int len) throws IOException {
		return stream.readNBytes(len);
	}
	
	public int read(byte[] bytes) throws IOException {
		return stream.read(bytes);
	}
	
	public byte[] readAllBytes() throws IOException {
		return stream.readAllBytes();
	}
	
	public int read(byte[] bytes, int off, int len) throws IOException {
		return stream.read(bytes, off, len);
	}
	
	public int readNBytes(byte[] bytes, int off, int len) throws IOException {
		return stream.readNBytes(bytes, off, len);
	}
	
	public byte[] readPacket() throws IOException {
		return StreamUtils.readPacket(stream);
	}
	
	public byte[] readPacket(int maxPacketSize) throws IOException {
		return StreamUtils.readPacket(stream, maxPacketSize);
	}
		
}
