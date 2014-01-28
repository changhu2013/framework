package mobi.dadoudou.framework.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VariablePool {

	private Map<String, Variable> variableMap;

	private VariableCreator variableCreator;

	public VariablePool() {
		this.variableMap = new HashMap<String, Variable>();
		this.variableCreator = new VariableCreator();
	}

	public Set<String> variableNameSet() {
		return this.variableMap.keySet();
	}

	public Variable add(String name, Object value) {
		return this.add(name, (value == null) ? null : value.getClass(), value);
	}

	public Variable add(String name, Class<?> clazz, Object value) {
		Variable variable = this.variableCreator.createVariable(name, clazz,
				value);
		if (name != null && !"".equals(name)) {
			this.variableMap.put(name, variable);
		}
		return variable;
	}

	public Variable getVariable(String name) {
		return this.variableMap.get(name);
	}

	/**
	 * 获取变量的值,并转化为目标类型
	 * 
	 * @param name
	 *            变量名称
	 * @param clazz
	 *            目标类型
	 * @return
	 */
	public Object getValue(String name, Class<?> clazz) {
		Variable variable = getVariable(name);

		// 如果试图获取一个基本数据类型的值
		if (clazz.isPrimitive()) {
			Object value = variable.getValue();
			return castWrapperPrimitive(value, clazz);
		} else {
			// 如果试图获取的类型是对应变量的父类
			if (variable.getClazz().isAssignableFrom(clazz)) {
				return variable.getValue();
			} else {
				return null;
			}
		}
	}

	private Object castWrapperPrimitive(Object bean, Class<?> clazz) {
		if (bean == null) {
			return null;
		}
		if (clazz.isAssignableFrom(Byte.class)) {
			return new Byte("" + bean);
		} else if (clazz.isAssignableFrom(Integer.class)) {
			return new Integer("" + bean);
		} else if (clazz.isAssignableFrom(Character.class)) {
			if (("" + bean).length() >= 1) {
				return new Character(("" + bean).charAt(0));
			} else {
				return null;
			}
		} else if (clazz.isAssignableFrom(Float.class)) {
			return new Float("" + bean);
		} else if (clazz.isAssignableFrom(Double.class)) {
			return new Double("" + bean);
		} else if (clazz.isAssignableFrom(Long.class)) {
			return new Long("" + bean);
		} else if (clazz.isAssignableFrom(Boolean.class)) {
			return new Boolean("" + bean);
		} else {
			return null;
		}
	}

	public void destroy() {

	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator<?> i = this.variableMap.entrySet().iterator(); i
				.hasNext();) {
			sb.append(i.next() + "\t");
		}
		return sb.toString();
	}
}
