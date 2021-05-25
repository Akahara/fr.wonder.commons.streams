package fr.wonder.commons.streams.serialization.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JSONArray implements JSONElement {
	
	private final List<Object> array = new ArrayList<>();
	
	public JSONArray(Object[] array) {
		this.array.addAll(Arrays.asList(array));
	}
	
	public JSONArray(Collection<? extends Object> array) {
		this.array.addAll(array);
	}
	
	public JSONArray() {
		
	}
	
	@Override
	public String toString() {
		return array.toString();
	}
	
	public List<Object> getElements() {
		return array;
	}
	
	public List<JSONObject> getJSONElements() throws ClassCastException {
		List<JSONObject> jsons = new ArrayList<>(array.size());
		for(Object o : array)
			jsons.add((JSONObject) o);
		return jsons;
	}

	public void add(Object o) {
		array.add(o);
	}
	
	public int size() {
		return array.size();
	}
	public Class<?> getElementType(int index) {
		Object o = array.get(index);
		return o == null ? null : o.getClass();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(int index, Class<T> type) {
		Object o = array.get(index);
		if(o == null)
			return null;
		if(type.isInstance(o))
			return (T) o;
		throw new IllegalArgumentException("Object " + index + " is not an instance of " + type.getSimpleName());
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(int index) throws ClassCastException {
		return (T) array.get(index);
	}
	
	public Number getNumber(int index) {
		return (Number) array.get(index);
	}
	
	public double getDouble(int index) { return getNumber(index).doubleValue(); }
	public short getShort(int index) { return getNumber(index).shortValue(); }
	public float getFloat(int index) { return getNumber(index).floatValue(); }
	public long getLong(int index) { return getNumber(index).longValue(); }
	public byte getByte(int index) { return getNumber(index).byteValue(); }
	public int getInt(int index) { return getNumber(index).intValue(); }
	public JSONObject getObject(int index) { return get(index); }
	public JSONArray getArray(int index) { return get(index); }
	public boolean getBoolean(int index) { return get(index); }
	public String getString(int index) { return get(index); }

}
