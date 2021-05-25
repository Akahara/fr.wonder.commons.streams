package fr.wonder.commons.streams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class StreamUtils {
	
	private static final ByteBuffer buffer2 = ByteBuffer.allocate(2);
	private static final ByteBuffer buffer4 = ByteBuffer.allocate(4);
	private static final ByteBuffer buffer8 = ByteBuffer.allocate(8);

	public static synchronized void writeChar(OutputStream stream, char c) throws IOException {
		buffer2.clear();
		buffer2.putChar(c);
		stream.write(buffer2.array());
	}
	
	public static void writeByte(OutputStream stream, byte b) throws IOException {
		stream.write(b);
	}
	
	public static synchronized void writeShort(OutputStream stream, short s) throws IOException {
		buffer2.clear();
		buffer2.putShort(s);
		stream.write(buffer2.array());
	}
	
	public static synchronized void writeInt(OutputStream stream, int i) throws IOException {
		buffer4.clear();
		buffer4.putInt(i);
		stream.write(buffer4.array());
	}
	
	public static synchronized void writeLong(OutputStream stream, long l) throws IOException {
		buffer8.clear();
		buffer8.putLong(l);
		stream.write(buffer8.array());
	}
	
	public static synchronized void writeFloat(OutputStream stream, float f) throws IOException {
		buffer4.clear();
		buffer4.putFloat(f);
		stream.write(buffer4.array());
	}
	
	public static synchronized void writeDouble(OutputStream stream, double d) throws IOException {
		buffer8.clear();
		buffer8.putDouble(d);
		stream.write(buffer8.array());
	}
	
	public static void writeString(OutputStream stream, String s) throws IOException {
		writeInt(stream, s.length());
		stream.write(s.getBytes());
	}
	
	public static void writePacket(OutputStream stream, byte[] bytes) throws IOException {
		writeInt(stream, bytes.length);
		stream.write(bytes);
	}

	public static int readInt(InputStream stream) throws IOException {
		return  (stream.read() << 24) +
				(stream.read() << 16) +
				(stream.read() << 8 ) +
				(stream.read() << 0 );
	}
	
	public static String readString(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.getInt()];
		buffer.get(bytes);
		return new String(bytes);
	}
	
	public static byte[] readPacket(InputStream stream) throws IOException {
		int packetSize = readInt(stream);
		byte[] packet = new byte[packetSize];
		int read = stream.readNBytes(packet, 0, packetSize);
		if(read != packetSize)
			throw new IOException("A packet was not fully read " + read + " / " + packetSize);
		return packet;
	}
	
	public static byte[] readPacket(InputStream stream, int maxPacketSize) throws IOException {
		int packetSize = readInt(stream);
		if(packetSize < 0 || packetSize > maxPacketSize)
			throw new IOException("Packet size exceeded! " + packetSize + " > " + maxPacketSize);
		byte[] packet = new byte[packetSize];
		int read = stream.readNBytes(packet, 0, packetSize);
		if(read != packetSize)
			throw new IOException("A packet was not fully read " + read + " / " + packetSize);
		return packet;
	}
	
	public static ByteBuffer consume(InputStream stream) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		stream.transferTo(buf);
		return ByteBuffer.wrap(buf.toByteArray());
	}

	public static byte[] readFile(File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			return stream.readAllBytes();
		}
	}
	
	public static String readFileToString(File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			return new String(stream.readAllBytes());
		}
	}

	public static void writeFile(File file, String text) throws IOException {
		if(!file.exists())
			file.createNewFile();
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(text.getBytes());
		}
	}

	public static int hashFile(File f) throws IOException {
		return Arrays.hashCode(readFile(f));
	}
	
}
