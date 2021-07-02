package fr.wonder.commons.streams.serialization;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import fr.wonder.commons.exceptions.SerializationException;
import fr.wonder.commons.systems.reflection.ReflectUtils;

class SerializedClass<T> {
	
	final Class<? extends T> clazz;
	final Constructor<? extends T> constructor;
	final Field[] serializedFields;

	SerializedClass(Class<? extends T> clazz) throws SerializationException {
		this(clazz, true, true);
	}
	
	SerializedClass(Class<? extends T> clazz, boolean searchFields, boolean searchConstructor) 
			throws SerializationException {
		this.clazz = clazz;
		if(!searchFields) {
			this.serializedFields = null;
		} else {
			this.serializedFields = ReflectUtils.getSerializableFields(clazz).toArray(Field[]::new);
		}
		if(!searchConstructor) {
			this.constructor = null;
		} else {
			try {
				this.constructor = clazz.getConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SerializationException("Class " + clazz + " does not implement an empty constructor");
			}
		}
	}

	List<Class<?>> getUsedClasses() {
		List<Class<?>> classes = new ArrayList<>();
		for(Field field : serializedFields) {
			Class<?> clazz = field.getType();
			while(clazz.isArray())
				clazz = clazz.componentType();
			if(!clazz.isPrimitive() && clazz != String.class && !classes.contains(clazz))
				classes.add(clazz);
		}
		return classes;
	}

	T newInstance() throws SerializationException {
		try {
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new SerializationException("Unable to create a new instance of " + clazz, e);
		}
	}

}