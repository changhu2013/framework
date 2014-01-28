package mobi.dadoudou.framework.core.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * JSON解析器。继承自{@link Parser}
 * 
 * @author changhu
 */
public class JSONParser extends Parser {

	/**
	 * 将传入参数编码为JSON字符串
	 * 
	 * @param bean
	 *            值对象
	 * @return {@link String} 编码后的JSON字符串
	 */
	public String unmarshal(Object bean) throws ParserException {
		try {
			return JSONObject.fromObject(bean).toString();
		} catch (JSONException e) {
			try {
				return JSONArray.fromObject(bean).toString();
			} catch (JSONException ee) {
				return "'" + bean.toString() + "'";
			}
		}
	}

	/**
	 * 将传入对象编码为JSON字符串，并指定值对象名字
	 * 
	 * @param name
	 *            值对象名称
	 * @param bean
	 *            值对象
	 * @return {@link String} 编码后的JSON字符串
	 */
	public String unmarshal(Object bean, String name) throws ParserException {
		if (name == null || "".equals(name.trim())) {
			return this.unmarshal(bean);
		} else {
			return "{" + name + ":" + this.unmarshal(bean) + "}";
		}
	}

	/**
	 * 将Java bean转换为指定数据类型
	 * 
	 * @param bean
	 *            值对象
	 * 
	 * @param clazz
	 *            目标类
	 */
	public Object marshalToObj(Object bean, Class<?> clazz)
			throws ParserException {
		if (Map.class.isAssignableFrom(clazz)) {
			JSONObject jo = JSONObject.fromObject(bean);
			return jsonObjectToMap(jo);
		} else if (List.class.isAssignableFrom(clazz)) {
			JSONArray ja = JSONArray.fromObject(bean);
			return jsonArrayToList(ja);
		} else if (clazz.isArray()) {
			JSONArray ja = JSONArray.fromObject(bean);
			return jsonArrayToArray(ja);
		} else {
			try {
				JSONObject jo = JSONObject.fromObject(bean);
				return JSONObject.toBean(jo, clazz);
			} catch (Exception e) {
				JSONArray ja = JSONArray.fromObject(bean);
				if (Collection.class.isAssignableFrom(clazz)) {
					return JSONArray.toCollection(ja);
				} else if (clazz.isArray()) {
					return JSONArray.toArray(ja, Object.class);
				} else {
					return bean;
				}
			}
		}
	}

	private Map<String, Object> jsonObjectToMap(JSONObject jo)
			throws JSONException, ParserException {
		Map<String, Object> map = new HashMap<String, Object>();
		@SuppressWarnings("rawtypes")
		Iterator i = jo.keys();
		while (i.hasNext()) {
			String key = (String) i.next();
			Object obj = jo.get(key);
			map.put(key, this.JSONToObj(obj));
		}
		return map;
	}

	private List<Object> jsonArrayToList(JSONArray ja) throws JSONException,
			ParserException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0, len = ja.size(); i < len; i++) {
			Object obj = ja.get(i);
			list.add(this.JSONToObj(obj));
		}
		return list;
	}

	private Object[] jsonArrayToArray(JSONArray ja) throws JSONException,
			ParserException {
		int len = ja.size();
		Object[] list = new Object[len];
		for (int i = 0; i < len; i++) {
			Object obj = ja.get(i);
			list[i] = this.JSONToObj(obj);
		}
		return list;
	}

	private Object JSONToObj(Object obj) throws JSONException, ParserException {
		if (obj instanceof JSONArray) {
			return this.jsonArrayToList((JSONArray) obj);
		} else if (obj instanceof JSONObject) {
			return this.jsonObjectToMap((JSONObject) obj);
		} else {
			return obj.toString();
		}
	}
}
