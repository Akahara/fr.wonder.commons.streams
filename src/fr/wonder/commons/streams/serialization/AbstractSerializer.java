package fr.wonder.commons.streams.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * -- Slightly out-dated documentation<br><br>
 * 
 * Default abstract implementation of {@link Serializer}, this class provides
 * an inner one; {@link SerializedClass} which is used for each serializable class
 * to collect information on serializable fields and which constructor to use.<br>
 * <br>
 * All three {@link #serialize(List) serialize} methods redirect to the abstract
 * {@link #writeObjects(Object[]) writeObjects} method, it is the job of the
 * child class to define this method as well as {@link #unserialize(Object)
 * unserialize}.<br>
 * <br>
 * See {@link InstanceSerializer} for an example of an implementation.
 *
 * @param <R> the return type of {@link #serialize(List) serialize} methods
 * @param <I> the type of a parent class of all serializable objects
 */
public abstract class AbstractSerializer<I, R> implements Serializer<I, R> {
	
	public static final short nullIdentifier = (short)(1<<(Short.SIZE-1));
	
	protected final List<SerializedClass<? extends I>> serials = new ArrayList<>();
	protected final Class<I> superType;
	
	public AbstractSerializer(Class<I> superType, Class<? extends I>[] classes) {
		this(superType, Arrays.asList(classes));
	}
	
	public AbstractSerializer(Class<I> superType, List<Class<? extends I>> classes) {
		this.superType = superType;
		
		List<Class<? extends I>> actualClasses = new ArrayList<>(classes);
		for(int i = 0; i < actualClasses.size(); i++) {
			Class<? extends I> clazz = actualClasses.get(i);
			// avoid redundancy and interfaces
			if(isSerializable(clazz))
				continue;
			SerializedClass<? extends I> serializedClass = new SerializedClass<>(clazz);
			for(Class<?> usedClass : serializedClass.getUsedClasses()) {
				if(!superType.isAssignableFrom(usedClass))
					throw new IllegalArgumentException("Class " + clazz + " contains a value of type "
							+ usedClass + " which does not extends " + superType);
				if(!actualClasses.contains(usedClass))
//					actualClasses.add((Class<? extends I>) usedClass);
					throw new IllegalArgumentException("Class " + clazz + " contains a value of type "
							+ usedClass + " which was not passed to the serializer");
			}
			serials.add(serializedClass);
		}
	}
	
	@Override
	public boolean isSerializable(Class<?> clazz) {
		if(clazz.isInterface() && superType.isAssignableFrom(clazz))
			return true;
		for(SerializedClass<?> serial : serials)
			if(serial.clazz == clazz)
				return true;
		return false;
	}
	
	protected int getClassId(Class<?> clazz) throws IllegalArgumentException {
		for(int i = 0; i < serials.size(); i++)
			if(serials.get(i).clazz == clazz)
				return i+1;
		if(clazz == superType)
			return 0;
		throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " is not serializable");
	}
	
	protected SerializedClass<? extends I> getSerial(int classId) throws IllegalArgumentException {
		if(classId == nullIdentifier)
			return null;
		if(classId == 0)
			return null;
		if(classId-1 > serials.size())
			throw new IllegalArgumentException("Unable to unserialize an instance with class id " + classId + "! unknown class");
		return serials.get(classId-1);
	}
	
	protected SerializedClass<? extends I> getSerial(Class<?> clazz) throws IllegalArgumentException {
		return getSerial(getClassId(clazz));
	}
	
	protected SerializedClass<? extends I> getSerial(Object o) throws IllegalArgumentException {
		return o == null ? null : getSerial(o.getClass());
	}
	
	@Override
	public R serialize(I object) throws IllegalArgumentException {
		return writeObjects(new Object[] { object });
	}
	
	@Override
	public R serialize(I[] objects) throws IllegalArgumentException {
		return writeObjects(objects);
	}
	
	@Override
	public R serialize(List<? extends I> objects) throws IllegalArgumentException {
		return writeObjects(objects.toArray());
	}
	
	protected abstract R writeObjects(Object[] objects) throws IllegalArgumentException;
	
}
