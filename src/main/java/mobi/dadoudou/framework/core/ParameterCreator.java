package mobi.dadoudou.framework.core;

import java.util.List;
import java.util.Map;

public class ParameterCreator {

	public Parameter createBooleanParameter(String parameterName) {
		return new Parameter(parameterName, Boolean.TYPE);
	}

	public Parameter createByteParameter(String parameterName) {
		return new Parameter(parameterName, Byte.TYPE);
	}

	public Parameter createCharacterParameter(String parameterName) {
		return new Parameter(parameterName, Character.class);
	}

	public Parameter createDoubleParameter(String parameterName) {
		return new Parameter(parameterName, Double.TYPE);
	}

	public Parameter createFloatParameter(String parameterName) {
		return new Parameter(parameterName, Float.TYPE);
	}

	public Parameter createIntegerParameter(String parameterName) {
		return new Parameter(parameterName, Integer.TYPE);
	}

	public Parameter createLongParameter(String parameterName) {
		return new Parameter(parameterName, Long.TYPE);
	}

	public Parameter createShortParameter(String parameterName) {
		return new Parameter(parameterName, Short.TYPE);
	}

	public Parameter createStringParameter(String parameterName) {
		return new Parameter(parameterName, String.class);
	}

	public Parameter createListParameter(String parameterName) {
		return new Parameter(parameterName, List.class);
	}

	public Parameter createMapParameter(String parameterName) {
		return new Parameter(parameterName, Map.class);
	}

	public Parameter createObjectParameter(String parameterName,
			Class<?> parameterClass) {
		return new Parameter(parameterName, parameterClass);
	}

	public Parameter createParameter(String parameterName,
			Class<?> parameterClass) {
		return new Parameter(parameterName, parameterClass);
	}
}
