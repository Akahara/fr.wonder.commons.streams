package fr.wonder.commons.streams.serialization.json;

import java.util.HashMap;
import java.util.Map;

public class JSONObject implements JSONElement {

	private final Map<String, Object> fields = new HashMap<>();
	
	/** {@code values} must <b>not</b> contain any non-primitive non-string value */
	public JSONObject(Map<String, ? extends Object> values) {
		this.fields.putAll(values);
	}
	
	public JSONObject() {
		
	}

	@Override
	public String toString() {
		return fields.toString();
	}
	
	public int fieldCount() {
		return fields.size();
	}
	
	public Map<String, Object> getFields() {
		return fields;
	}
	
	public boolean isSet(String field) {
		return fields.containsKey(field);
	}
	
	public void set(String field, Object o) {
		fields.put(field, o);
	}
	
	public Class<?> getFieldType(String field) {
		Object o = fields.get(field);
		return o == null ? null : o.getClass();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String field, Class<T> type) {
		Object o = fields.get(field);
		if(o == null)
			return null;
		if(type.isInstance(o))
			return (T) o;
		throw new IllegalArgumentException("Object " + field + " is not an instance of " + type.getSimpleName());
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String field) throws ClassCastException {
		return (T) fields.get(field);
	}
	
	public Number getNumber(String field) throws ClassCastException {
		return (Number) fields.get(field);
	}
	
	public double getDouble(String field) { return getNumber(field).doubleValue(); }
	public short getShort(String field) { return getNumber(field).shortValue(); }
	public float getFloat(String field) { return getNumber(field).floatValue(); }
	public long getLong(String field) { return getNumber(field).longValue(); }
	public byte getByte(String field) { return getNumber(field).byteValue(); }
	public int getInt(String field) { return getNumber(field).intValue(); }
	public JSONObject getObject(String field) { return get(field); }
	public JSONArray getArray(String field) { return get(field); }
	public boolean getBoolean(String field) { return get(field); }
	public String getString(String field) { return get(field); }

}
