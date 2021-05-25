package fr.wonder.commons.streams.serialization;

import java.util.Arrays;
import java.util.Collection;

import fr.wonder.commons.exceptions.SerializationException;

public interface Serializer<I, R> {
	
	public boolean isSerializable(Class<?> clazz);
	
	public R serializeSelf() throws SerializationException;
	
	public R serialize(I object) throws IllegalArgumentException, SerializationException;
	public default R serialize(I[] objects) throws IllegalArgumentException, SerializationException {
		return serialize(Arrays.asList(objects));
	}
	public R serialize(Collection<? extends I> objects) throws IllegalArgumentException, SerializationException;
	
	public I unserialize(R serialized) throws IllegalArgumentException, SerializationException;
	
}
