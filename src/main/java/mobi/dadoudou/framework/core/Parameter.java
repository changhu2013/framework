package mobi.dadoudou.framework.core;

public class Parameter {

	private String name;

	private Class<?> clazz;

	public Parameter(Class<?> clazz) {
		this.setClazz(clazz);
	}

	public Parameter(String name, Class<?> clazz) {
		this.setName(name);
		this.setClazz(clazz);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

}
