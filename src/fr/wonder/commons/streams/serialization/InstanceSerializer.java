package fr.wonder.commons.streams.serialization;

import static fr.wonder.commons.streams.StreamUtils.writeByte;
import static fr.wonder.commons.streams.StreamUtils.writeChar;
import static fr.wonder.commons.streams.StreamUtils.writeDouble;
import static fr.wonder.commons.streams.StreamUtils.writeFloat;
import static fr.wonder.commons.streams.StreamUtils.writeInt;
import static fr.wonder.commons.streams.StreamUtils.writeLong;
import static fr.wonder.commons.streams.StreamUtils.writeShort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.wonder.commons.streams.StreamUtils;

public class InstanceSerializer<T> extends AbstractSerializer<T, byte[]> {
	
	public InstanceSerializer(Class<T> superType, Class<? extends T>[] classes) {
		super(superType, classes);
	}
	
	public InstanceSerializer(Class<T> superType, List<Class<? extends T>> classes) {
		super(superType, classes);
	}
	
	@Override
	public byte[] serializeSelf() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			StreamUtils.writeInt(stream, serials.size());
			for(SerializedClass<?> serial : serials) {
				StreamUtils.writeString(stream, serial.clazz.getName());
			}
		} catch (IOException x) {
			// silently ignore exceptions, they can only come from StreamUtils#writeString
			// and this method cannot fail with a ByteArrayOutputStream.
			// multiple similar errors are ignored
		}
		return stream.toByteArray();
	}
	
	public static <T> InstanceSerializer<T> unserializeSerializer(Class<T> superType, byte[] bytes) throws IOException {
		return unserializeSerializer(superType, bytes, InstanceSerializer.class.getClassLoader());
	}
	
	public static <T> InstanceSerializer<T> unserializeSerializer(Class<T> superType, byte[] bytes, ClassLoader classLoader) throws IllegalArgumentException {
		if(bytes == null || bytes.length == 0)
			throw new NullPointerException("Unable de unserialize from empty or null byte array!");
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int classCount = buffer.getInt();
		List<Class<? extends T>> classes = new ArrayList<>(classCount);
		for(int i = 0; i < classCount; i++) {
			try {
				String classPath = StreamUtils.readString(buffer);
				Class<?> clazz = classLoader.loadClass(classPath);
				classes.add((Class<? extends T>) clazz.asSubclass(superType));
			} catch (ClassNotFoundException | ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return new InstanceSerializer<T>(superType, classes);
	}
	
	@Override
	protected byte[] writeObjects(Object[] objects) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			writeArray(superType, objects, stream);
		} catch (IOException x) { }
		return stream.toByteArray();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<T> unserialize(byte[] bytes) throws BufferUnderflowException {
		List<T> objects = new ArrayList<>();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		for(Object o : (Object[])getArray(superType, buffer)) {
			objects.add((T) o);
		}
		return objects;
	}

	private void serializeInstance(Object obj, ByteArrayOutputStream stream) throws IOException, IllegalArgumentException {
		try {
			if(obj == null) {
				StreamUtils.writeShort(stream, nullIdentifier);
				return;
			}
			int clazzId = getClassId(obj.getClass());
			StreamUtils.writeShort(stream, (short)clazzId);
			SerializedClass<? extends T> serial = getSerial(clazzId);
			for(Field f : serial.serializedFields) {
				Class<?> clazz = f.getType();
				if(clazz.isArray())
					writeArray(clazz.getComponentType(), f.get(obj), stream);
				else if(clazz == int.class)
					writeInt(stream, f.getInt(obj));
				else if(clazz == byte.class)
					writeByte(stream, f.getByte(obj));
				else if(clazz == float.class)
					writeFloat(stream, f.getFloat(obj));
				else if(clazz == long.class)
					writeLong(stream, f.getLong(obj));
				else if(clazz == double.class)
					writeDouble(stream, f.getDouble(obj));
				else if(clazz == short.class)
					writeShort(stream, f.getShort(obj));
				else if(clazz == char.class)
					writeChar(stream, f.getChar(obj));
				else if(clazz == String.class)
					writeString(stream, (String)f.get(obj));
				else {
					Object val = f.get(obj);
					serializeInstance(val, stream);
				}
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to serialize an object! " + obj.toString(), e);
		}
	}
	
	private <K> void writeArray(Class<K> arrayType, Object array, ByteArrayOutputStream stream) throws IOException, IllegalArgumentException {
		// TODO add support for extended native types (Integer, Float...)
		if(array == null) {
			writeInt(stream, nullIdentifier);
		} else if(arrayType.isArray()) {
			@SuppressWarnings("unchecked")
			K[] ar = (K[]) array;
			writeInt(stream, ar.length);
			for(K k : ar)
				writeArray(arrayType.getComponentType(), k, stream);
		} else if(arrayType == byte.class) {
			byte[] ar = (byte[])array;
			writeInt(stream, ar.length);
			stream.write(ar);
		} else if(arrayType == int.class) {
			int[] ar = (int[])array;
			writeInt(stream, ar.length);
			for(int x : ar)
				writeInt(stream, x);
		} else if(arrayType == float.class) {
			float[] ar = (float[])array;
			writeInt(stream, ar.length);
			for(float x : ar)
				writeFloat(stream, x);
		} else if(arrayType == long.class) {
			long[] ar = (long[])array;
			for(long x : ar)
				writeLong(stream, x);
		} else if(arrayType == double.class) {
			double[] ar = (double[])array;
			for(double x : ar)
				writeDouble(stream, x);
		} else if(arrayType == short.class) {
			short[] ar = (short[])array;
			for(short x : ar)
				writeShort(stream, x);
		} else if(arrayType == char.class) {
			char[] ar = (char[])array;
			for(char x : ar)
				writeChar(stream, x);
		} else if(arrayType == String.class) {
			String[] ar = (String[])array;
			writeInt(stream, ar.length);
			for(String x : ar) {
				writeString(stream, x);
			}
		} else {
			Object[] ar = (Object[]) array;
			writeInt(stream, ar.length);
			for(Object x : ar)
				serializeInstance(x, stream);
		}
	}
	
	private void writeString(OutputStream stream, String s) throws IOException {
		if(s == null) {
			writeInt(stream, nullIdentifier);
		} else {
			writeInt(stream, s.length());
			stream.write(s.getBytes());
		}
	}

	private T unserialize(ByteBuffer buffer) {
		int classId = buffer.getShort();
		if(classId == nullIdentifier)
			return null;
		SerializedClass<? extends T> serial = getSerial(classId);
		Object[] args = new Object[serial.serializedFields.length];
		for(int i = 0; i < args.length; i++) {
			Class<?> clazz = serial.serializedFields[i].getType();
			if(clazz.isArray())
				args[i] = getArray(clazz.getComponentType(), buffer);
			else if(clazz == int.class)
				args[i] = buffer.getInt();
			else if(clazz == byte.class)
				args[i] = buffer.get();
			else if(clazz == float.class)
				args[i] = buffer.getFloat();
			else if(clazz == long.class)
				args[i] = buffer.getLong();
			else if(clazz == double.class)
				args[i] = buffer.getDouble();
			else if(clazz == short.class)
				args[i] = buffer.getShort();
			else if(clazz == char.class)
				args[i] = buffer.getChar();
			else if(clazz == String.class)
				args[i] = readString(buffer);
			else
				args[i] = unserialize(buffer);
		}
		return serial.callConstructor(args);
	}

	@SuppressWarnings("unchecked")
	private <K> Object getArray(Class<K> arrayType, ByteBuffer buffer) {
		int arraySize = buffer.getInt();
		if(arraySize == nullIdentifier) {
			return null;
		} else if(arrayType.isArray()) {
			K[] array = (K[]) Array.newInstance(arrayType, arraySize);
			for(int i = 0; i < arraySize; i++)
				array[i] = (K) getArray(arrayType.getComponentType(), buffer);
			return array;
		} else if(arrayType == int.class) {
			int[] array = new int[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getInt();
			return array;
		} else if(arrayType == byte.class) {
			byte[] array = new byte[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.get();
			return array;
		} else if(arrayType == float.class) {
			float[] array = new float[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getFloat();
			return array;
		} else if(arrayType == long.class) {
			long[] array = new long[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getLong();
			return array;
		} else if(arrayType == double.class) {
			double[] array = new double[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getDouble();
			return array;
		} else if(arrayType == short.class) {
			short[] array = new short[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getShort();
			return array;
		} else if(arrayType == char.class) {
			char[] array = new char[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = buffer.getChar();
			return array;
		} else if(arrayType == String.class) {
			String[] array = new String[arraySize];
			for(int i = 0; i < arraySize; i++)
				array[i] = readString(buffer);
			return array;
		} else {
			Object[] array = (Object[]) Array.newInstance(arrayType, arraySize);
			for(int i = 0; i < arraySize; i++)
				array[i] = unserialize(buffer);
			return array;
		}
	}
	
	private String readString(ByteBuffer buffer) {
		int stringLen = buffer.getInt();
		if(stringLen == nullIdentifier)
			return null;
		byte[] buf = new byte[stringLen];
		buffer.get(buf);
		return new String(buf);
	}
	
}
