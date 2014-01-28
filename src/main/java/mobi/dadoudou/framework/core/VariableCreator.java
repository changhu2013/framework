package mobi.dadoudou.framework.core;

public class VariableCreator {

	public Variable createVariable(String name, Class<?> clazz) {
		return new Variable(name, clazz);
	}

	public Variable createVariable(String name, Class<?> clazz, Object value) {
		return new Variable(name, clazz, value);
	}
}
