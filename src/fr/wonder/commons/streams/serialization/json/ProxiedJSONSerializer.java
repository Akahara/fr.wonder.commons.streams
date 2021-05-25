package fr.wonder.commons.streams.serialization.json;

import java.lang.reflect.InvocationTargetException;

import fr.wonder.commons.exceptions.SerializationException;

public class ProxiedJSONSerializer extends JSONSerializer {

	public static final ProxiedJSONSerializer INSTANCE = new ProxiedJSONSerializer();
	public static final String PROXY_FIELD = "proxy";
	
	private final ClassLoader classLoader;
	
	public ProxiedJSONSerializer(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public ProxiedJSONSerializer() {
		this(ClassLoader.getSystemClassLoader());
	}
	
	@Override
	protected Object newInstance(Class<?> fieldType, JSONObject json, boolean requireAllFields) 
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, SerializationException {
		
		if(!json.isSet(PROXY_FIELD)) {
			return super.newInstance(fieldType, json, requireAllFields);
		} else {
			try {
				Class<?> clazz = Class.forName(json.getString(PROXY_FIELD), true, classLoader);
				Object created = clazz.getDeclaredConstructor().newInstance();
				fillObjectFields((JSONObject) json, created, requireAllFields);
				return created;
			} catch (ClassNotFoundException e) {
				throw new SerializationException(e);
			}
		}
	}
	
	public Object createObject(JSONObject json, boolean requireAllFields) throws SerializationException {
		if(!json.isSet(PROXY_FIELD))
			throw new SerializationException("Missing proxy field");
		try {
			return newInstance(null, json, requireAllFields);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException | SerializationException e) {
			throw new SerializationException(e);
		}
	}
	
	@Override
	public JSONObject toJSONObject(Object o) throws SerializationException {
		JSONObject json = super.toJSONObject(o);
		if(o != null)
			json.set(PROXY_FIELD, o.getClass().getName());
		return json;
	}

}
