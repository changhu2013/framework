package mobi.dadoudou.framework;

public interface FrameworkConstants {

	/**
	 * OSGI启动器在WEB上下文中的键名
	 */
	public static final String RIAMBSOFT_FRAMEWORK_LAUNCHER = "__riambsoft_framework_launcher";

	/**
	 * OSGI上下文在WEB上下文中的键名
	 */
	public static final String RIAMBSOFT_FRAMEWORK_BUNDLE_CONTEXT = "__riambsoft_framework_bundle_context";

	
	/**
	 * 导入到OSGI平台的JAVA包配置文件
	 */
	public static final String RIAMBSOFT_FRAMEWORK_PACKAGES_PROPERTIES = "packages.properties";
	
	/**
	 * 导入OSGI平台的JAVA包配置键名
	 */
	public static final String RIAMBSOFT_FRAMEWORK_PACKAGE_PROPERTIES_KEY = "scanning.package.includes";
	
	/**
	 * 自启动的Bundles配置文件
	 */
	public static final String RIAMBSOFT_FRAMEWORK_BUNDLES_PROPERTIES = "bundles.properties";
	
	/**
	 * 自启动的Bundles配置键名
	 */
	public static final String RIAMBSOFT_FRAMEWORK_BUNDLES_PROPERTIES_KEY = "scanning.bundle.autostart";
}
