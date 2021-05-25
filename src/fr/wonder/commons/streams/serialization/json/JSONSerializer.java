package fr.wonder.commons.streams.serialization.json;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.wonder.commons.exceptions.SerializationException;
import fr.wonder.commons.streams.serialization.SerializedText;
import fr.wonder.commons.streams.serialization.Serializer;
import fr.wonder.commons.utils.PrimitiveUtils;
import fr.wonder.commons.utils.ReflectUtils;
import fr.wonder.commons.utils.StringUtils;

public class JSONSerializer implements Serializer<JSONObject, String> {

	// TODO comment all the functions from JSONSerializer and ProxiedJSONSerializer
	
	public static final JSONSerializer INSTANCE = new JSONSerializer();
	
	private static final String indentChar = "  ";
	
	public JSONSerializer() { }

	@Override
	public boolean isSerializable(Class<?> clazz) {
		return clazz == JSONObject.class;
	}

	@Override
	public String serializeSelf() {
		throw new IllegalAccessError("JSON serializers are not unique");
	}

	@Override
	public String serialize(JSONObject object) throws SerializationException {
		return serializeJSON(object);
	}
	
	public static String serializeJSON(JSONObject object) throws SerializationException {
		return writeValue(new StringBuilder(), 0, object).toString();
	}
	
	@Override
	public String serialize(Collection<? extends JSONObject> objects) throws SerializationException {
		return serializeJSON(objects);
	}
	
	public static String serializeJSON(Collection<? extends JSONObject> objects) throws SerializationException {
		return serializeJSON(new JSONObject(Map.of("array", new JSONArray(objects))));
	}

	@Override
	public JSONObject unserialize(String serialized) throws SerializationException {
		return unserializeJSON(serialized);
	}
	
	public static JSONObject unserializeJSON(String serialized) throws SerializationException {
		if(serialized.isBlank())
			return new JSONObject();
		return parseObject(new SerializedText(serialized));
	}
	
	public static JSONObject unwrapJSON(JSONObject json) {
		JSONObject newJSON = new JSONObject();
		for(Entry<String, Object> field : json.getFields().entrySet()) {
			Object o = field.getValue();
			if(o instanceof JSONObject) {
				JSONObject unwrapped = unwrapJSON((JSONObject) o);
				for(Entry<String, Object> field2 : unwrapped.getFields().entrySet()) {
					newJSON.set(field.getKey() + "." + field2.getKey(), field2.getValue());
				}
			} else if(o instanceof JSONArray) {
				for(Object oo : ((JSONArray) o).getElements()) {
					if(oo instanceof JSONObject)
						unwrapJSON((JSONObject) oo);
				}
				newJSON.set(field.getKey(), o);
			} else {
				newJSON.set(field.getKey(), o);
			}
		}
		return newJSON;
	}

	private static StringBuilder writeValue(StringBuilder sb, int indent, Object o) throws SerializationException {
		if (o == null) {
			sb.append("null");
			return sb;
		}
		Class<?> clazz = o.getClass();
		if(clazz == JSONObject.class)
			writeObject(sb, indent+1, (JSONObject) o);
		else if(clazz == JSONArray.class)
			writeArray(sb, indent+1, (JSONArray) o);
		else if(clazz == String.class)
			sb.append('"' + StringUtils.escape(o.toString()) + '"');
		else if(PrimitiveUtils.isPrimitiveType(clazz))
			sb.append(o.toString());
		else
			throw new SerializationException("Invalid object type " + o.getClass());
		return sb;
	}
	
	private static void writeObject(StringBuilder sb, int indent, JSONObject o) throws SerializationException {
		if(o.fieldCount() == 0) {
			sb.append("{}");
			return;
		}
		
		sb.append('{');
		for(Entry<String, Object> field : o.getFields().entrySet()) {
			sb.append('\n');
			sb.append(indentChar.repeat(indent));
			sb.append('"');
			sb.append(field.getKey());
			sb.append("\": ");
			Object val = field.getValue();
			writeValue(sb, indent, val);
			sb.append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append('\n');
		sb.append(indentChar.repeat(indent-1));
		sb.append('}');
	}
	
	private static void writeArray(StringBuilder sb, int indent, JSONArray a) throws SerializationException {
		if(a.size() == 0) {
			sb.append("[]");
			return;
		}
		
		sb.append('[');
		for(Object o : a.getElements()) {
			sb.append('\n');
			sb.append(indentChar.repeat(indent));
			writeValue(sb, indent, o);
			sb.append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append('\n');
		sb.append(indentChar.repeat(indent-1));
		sb.append(']');
	}
	
	private static Object parseValue(SerializedText text) {
		if(text.nextIsNull())
			return text.skipNull();
		
		char c = text.currentChar();
		
		if(c == '{')
			return parseObject(text);
		else if(c == '[')
			return parseArray(text);
		else if(c == '"')
			return text.nextString();
		else if(c == '-' || Character.isDigit(c) || text.nextIsNaN())
			return text.nextDouble();
		else if(c == 't' || c == 'f')
			return text.nextBool();
		else
			throw new IllegalStateException("Unknown value type for char " + c);
	}
	
	private static JSONObject parseObject(SerializedText text) {
		text.skip('{');
		JSONObject json = new JSONObject();
		if(text.currentChar() == '}') {
			text.skip(1);
			return json;
		}
		while(true) {
			String fieldName = text.nextString();
			text.skip(':');
			json.set(fieldName, parseValue(text));
			if(text.currentChar() == ',')
				text.skip(1);
			else
				break;
		}
		text.skip('}');
		return json;
	}
	
	private static JSONArray parseArray(SerializedText text) {
		text.skip('[');
		JSONArray json = new JSONArray();
		if(text.currentChar() == ']') {
			text.skip(1);
			return json;
		}
		while(true) {
			json.add(parseValue(text));
			if(text.currentChar() == ',')
				text.skip(1);
			else
				break;
		}
		text.skip(']');
		return json;
	}
	
	public void fillObjectFields(JSONObject json, Object o, boolean requireAllFields) throws SerializationException {
		for (Field f : o.getClass().getFields()) {
			if (!ReflectUtils.isSerializableField(f.getModifiers()))
				continue;
			Class<?> ft = f.getType();
			Object val;
			if (json.isSet(f.getName()))
				val = json.get(f.getName());
			else if (requireAllFields)
				throw new SerializationException("Field " + f + " not found in json");
			else
				continue;
			try {
				if (ft.isPrimitive()) {
					if (!(val instanceof Number))
						throw new SerializationException(
								"Unexpected value type " + val.getClass() + " for field " + f);
					PrimitiveUtils.setPrimitive(o, f, (Number) val);
					
				} else if (ft == String.class) {
					if (val != null && !(val instanceof String))
						throw new SerializationException(
								"Unexpected value type " + val.getClass() + " for field " + f);
					f.set(o, val);
					
				} else if (ft.isArray()) {
					if (val == null) {
						f.set(o, null);
					} else {
						if (!(val instanceof JSONArray))
							throw new SerializationException(
									"Unexpected value type " + val.getClass() + " for field " + f);
						JSONArray array = (JSONArray) val;
						Object fieldValue = Array.newInstance(ft.componentType(), array.size());
						fillArray(fieldValue, array, ft.componentType(), requireAllFields);
						f.set(o, fieldValue);
					}
					
				} else {
					if (val == null) {
						f.set(o, null);
					} else {
						if (!(val instanceof JSONObject))
							throw new SerializationException(
									"Unexpected value type " + val.getClass() + " for field " + f);
						
						f.set(o, newInstance(f.getType(), (JSONObject) val, requireAllFields));
					}
				}
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException
					| NoSuchMethodException e) {
				throw new SerializationException(e);
			}
		}
	}
	
	protected Object newInstance(Class<?> fieldType, JSONObject json, boolean requireAllFields) 
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, SerializationException {
		Object created = fieldType.getDeclaredConstructor().newInstance();
		fillObjectFields((JSONObject) json, created, requireAllFields);
		return created;
	}

	private void fillArray(Object filled, JSONArray array, Class<?> componentType, boolean requireAllFields)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			SecurityException, SerializationException {
		if (componentType.isPrimitive()) {
			for (int i = 0; i < array.size(); i++)
				Array.set(filled, i, PrimitiveUtils.castToPrimitive(array.get(i), componentType));
		} else if (componentType == String.class) {
			for (int i = 0; i < array.size(); i++)
				Array.set(filled, i, (String) array.getString(i));
		} else if (componentType.isArray()) {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i) == null)
					Array.set(filled, i, null);
				else if (!(array.get(i) instanceof JSONArray))
					throw new IllegalArgumentException(
							"Unexpected value type " + array.get(i).getClass() + " for array type " + componentType);
				JSONArray na = (JSONArray) array.get(i);
				Object c = Array.newInstance(componentType.componentType(), array.size());
				Array.set(filled, i, c);
				fillArray(c, na, componentType.componentType(), requireAllFields);
			}
		} else {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i) == null)
					continue; // null by default
				if (!(array.get(i) instanceof JSONObject))
					throw new SerializationException(
							"Unexpected value type " + array.get(i).getClass() + " for array type " + componentType);
				Array.set(filled, i, newInstance(componentType, array.getObject(i), requireAllFields));
			}
		}
	}
	
	public JSONObject toJSONObject(Object o) throws SerializationException {
		JSONObject json = new JSONObject();
		if(o == null)
			return json;
		if(!(o instanceof Serializable))
			throw new SerializationException("Not an instance of Serializable");
		List<Field> fields = ReflectUtils.getSerializableFields(o.getClass());
		try {
			for(Field f : fields)
				json.set(f.getName(), toJSONValue(f.get(o)));
		} catch (IllegalAccessException e) {
			throw new SerializationException(e);
		}
		return json;
	}
	
	private Object toJSONValue(Object obj) throws SerializationException {
		if(obj == null)
			return null;
		Class<?> type = obj.getClass();
		if(PrimitiveUtils.isPrimitiveType(type) || type == String.class)
			return obj;
		else if(type.isArray())
			return toJSONArray(obj);
		else
			return toJSONObject(obj);
	}
	
	private JSONArray toJSONArray(Object array) throws SerializationException {
		JSONArray json = new JSONArray();
		int length = Array.getLength(array);
		for(int i = 0; i < length; i++)
			json.add(toJSONValue(Array.get(array, i)));
		return json;
	}

}
