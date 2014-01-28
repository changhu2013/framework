package mobi.dadoudou.framework.core;

public interface ServiceEngineInterface {

	public Variable invoke(Object service, String methodName, VariablePool pool);

}
