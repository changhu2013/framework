package mobi.dadoudou.framework.core;

public class Variable {

	public String name;

	private Class<?> clazz;

	private Object value;

	public Variable(String name, Class<?> clazz) {
		this.setName(name);
		this.setClazz(clazz);
	}

	public Variable(String name, Class<?> clazz, Object value) {
		this.setName(name);
		this.setClazz(clazz);
		this.setValue(value);
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

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

	public String getValueText() {
		if (this.getValue() == null) {
			return null;
		}
		return this.getValue().toString();
	}

	public String getStringValue() {
		return this.getValueText();
	}

	public char getCharacterValue() {
		if (this.getValueText() == null) {
			return 0;
		}
		return new Character(this.getValueText().charAt(0)).charValue();
	}

	public byte getByteValue() {
		if (this.getValueText() == null) {
			return 0;
		}
		return new Byte(this.getValueText()).byteValue();
	}

	public boolean getBooleanValue() {
		if (this.getValue() == null) {
			return false;
		}
		return new Boolean(this.getValueText()).booleanValue();
	}

	public short getShortValue() {
		if (this.getValue() == null) {
			return 0;
		}
		return new Short(this.getValueText()).shortValue();
	}

	public int getIntegerValue() {
		if (this.getValue() == null) {
			return 0;
		}
		return new Integer(this.getValueText()).intValue();
	}

	public long getLongValue() {
		if (this.getValue() == null) {
			return 0;
		}
		return new Long(this.getValueText()).longValue();
	}

	public float getFloatValue() {
		if (this.getValue() == null) {
			return 0;
		}
		return new Float(this.getValueText()).floatValue();
	}

	public double getDoubleValue() {
		if (this.getValue() == null) {
			return 0;
		}
		return new Double(this.getValueText()).doubleValue();
	}

	public String toString() {
		return "name:" + this.getName() + " clazz:" + this.getClazz()
				+ " value:" + this.getValueText();
	}
}
