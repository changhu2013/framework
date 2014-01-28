package mobi.dadoudou.framework.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import mobi.dadoudou.framework.FrameworkConstants;
import mobi.dadoudou.framework.FrameworkException;
import mobi.dadoudou.framework.FrameworkLauncher;
import mobi.dadoudou.framework.FrameworkServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;


class MyPermissionCollection extends PermissionCollection {

	private static final long serialVersionUID = 8522371180271208106L;

	public Permission allPermission = new AllPermission();

	public void add(Permission permission) {
	}

	public boolean implies(Permission permission) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public Enumeration<Permission> elements() {
		return new MyEnumeration(this);
	}
}

@SuppressWarnings("rawtypes")
class MyEnumeration implements Enumeration {

	private int cur;

	private MyPermissionCollection mpc;

	public MyEnumeration(MyPermissionCollection mpc) {
		super();
		this.mpc = mpc;
	}

	public boolean hasMoreElements() {
		return this.cur < 1;
	}

	public Object nextElement() {

		if (this.cur == 0) {
			this.cur = 1;
			return mpc.allPermission;
		}

		throw new NoSuchElementException();
	}

}

public class DefaultFrameworkLauncher implements FrameworkLauncher {

	private Log logger = LogFactory.getLog(DefaultFrameworkLauncher.class);

	static final PermissionCollection allPermissions = new MyPermissionCollection();

	protected ServletContext servletContext;

	private File platformDirectory;

	private ClassLoader frameworkContextClassLoader;

	private ChildFirstURLClassLoader frameworkClassLoader;

	private String commandLine;

	static {
		if (allPermissions.elements() == null) {
			throw new IllegalStateException();
		}
	}

	public void init(FrameworkServletContext frameworkServletContext)
			throws FrameworkException {
		this.servletContext = frameworkServletContext.getServletContext();
		logger.info("Riambsoft Framework 初始化完成");
	}

	@SuppressWarnings("rawtypes")
	public BundleContext start(String commandLine) throws FrameworkException {

		if (platformDirectory == null) {
			throw new FrameworkException("Riambsoft Framework 尚未部署,无法启动");
		}

		if (frameworkClassLoader != null) {
			throw new FrameworkException("Riambsoft Framework 已经启动,无需再启");
		}

		ClassLoader original = Thread.currentThread().getContextClassLoader();
		Map<Object, Object> map = buildInitialPropertyMap();

		buildFrameworkPropertyMap(map);

		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			logger.info(key + " " + map.get(key));
		}

		String[] args = buildCommandLineArguments(commandLine);
		for (String arg : args) {
			logger.info("命令行参数:" + arg);
		}

		// Note that the OSGi TCK is one example where this property
		// MUST be set to false because many TCK bundles set and read system
		// properties.
		System.setProperty("osgi.framework.useSystemProperties", "false");

		try {
			URL[] urls = { new URL((String) map.get("osgi.framework")) };

			frameworkClassLoader = new ChildFirstURLClassLoader(urls,
					getClass().getClassLoader());

			Class<?> clazz = frameworkClassLoader.loadClass(
					"org.eclipse.core.runtime.adaptor.EclipseStarter", true);

			Method setInitialProperties = clazz.getMethod(
					"setInitialProperties", new Class[] { Map.class });

			setInitialProperties.invoke(null, new Object[] { map });

			registerRestartHandler(clazz);

			Method runMethod = clazz.getMethod("startup", new Class[] {
					String[].class, Runnable.class });

			BundleContext bundleContext = (BundleContext) runMethod.invoke(
					null, new Object[] { args, null });

			frameworkContextClassLoader = Thread.currentThread()
					.getContextClassLoader();

			logger.info("Riambsoft Framework 启动成功");

			// 记录命令行,重启的时候使用该参数
			this.commandLine = commandLine;

			return bundleContext;
		} catch (Exception e) {

			throw new FrameworkException(e);
		} finally {

			Thread.currentThread().setContextClassLoader(original);
		}
	}

	public void stop() throws FrameworkException {

		if (platformDirectory == null) {

			logger.warn("Riambsoft Framework 尚未部署,无法停止");
			return;
		}

		if (frameworkClassLoader == null) {

			logger.warn("Riambsoft Framework 已经停止");
			return;
		}

		ClassLoader original = Thread.currentThread().getContextClassLoader();

		try {
			Class<?> clazz = frameworkClassLoader
					.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
			Method method = clazz.getDeclaredMethod("shutdown", new Class[] {});
			Thread.currentThread().setContextClassLoader(
					frameworkContextClassLoader);
			method.invoke(clazz);

			clazz = getClass().getClassLoader().loadClass(
					"org.apache.commons.logging.LogFactory");

			method = clazz.getDeclaredMethod("release",
					new Class[] { ClassLoader.class });
			method.invoke(clazz, new Object[] { frameworkContextClassLoader });
		} catch (Exception e) {

			logger.warn("停止 Riambsoft Framework 时发生异常", e);
			return;
		} finally {

			frameworkClassLoader = null;
			frameworkContextClassLoader = null;
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	public void deploy() throws FrameworkException {

		if (platformDirectory != null) {
			logger.info("Riambsoft Framework 已经部署,无需再部署");
			return;
		}

		File temp = (File) servletContext
				.getAttribute("javax.servlet.context.tempdir");
		platformDirectory = new File(temp, "eclipse");

		if (!platformDirectory.exists()) {
			platformDirectory.mkdirs();
		}

		logger.debug("platformDirectory : "
				+ platformDirectory.getAbsolutePath());

		copyResource("/WEB-INF/eclipse/configuration/", new File(
				platformDirectory, "configuration"));

		copyResource("/WEB-INF/eclipse/features/", new File(
				platformDirectory, "features"));

		File plugins = new File(platformDirectory, "plugins");

		copyResource("/WEB-INF/eclipse/plugins/", plugins);

		// copyResource("/WEB-INF/eclipse/.eclipseproduct", new
		// File(platformDirectory, ".eclipseproduct"));
	}

	public void undeploy() throws FrameworkException {

		if (platformDirectory == null) {
			logger.info("Riambsoft Framework 尚未部署,无需卸载");
			return;
		}

		if (frameworkClassLoader != null) {
			throw new IllegalStateException("Riambsoft Framework 尚未停止,无法卸载");
		}

		deleteDirectory(new File(platformDirectory, "configuration"));
		
		deleteDirectory(new File(platformDirectory, "features"));
		
		deleteDirectory(new File(platformDirectory, "plugins"));
		
		// deleteDirectory(new File(platformDirectory, "workspace"));
		// new File(platformDirectory, ".eclipseproduct").delete();

		platformDirectory = null;
	}

	public void destroy() throws FrameworkException {
		stop();
		undeploy();
	}

	public void restart() throws FrameworkException {
		stop();
		start(this.commandLine);
	}

	protected void copyResource(String resourcePath, File target) {
		if (resourcePath.endsWith("/")) {
			target.mkdir();
			Set<String> paths = servletContext.getResourcePaths(resourcePath);
			if (paths == null)
				return;
			for (Iterator<String> it = paths.iterator(); it.hasNext();) {
				String path = it.next();
				File newFile = new File(target, path.substring(resourcePath
						.length()));
				copyResource(path, newFile);
			}
		} else {
			try {
				if (target.createNewFile()) {
					InputStream is = null;
					OutputStream os = null;
					try {
						is = servletContext.getResourceAsStream(resourcePath);
						if (is == null)
							return;
						os = new FileOutputStream(target);
						byte[] buffer = new byte[8192];
						int bytesRead = is.read(buffer);
						while (bytesRead != -1) {
							os.write(buffer, 0, bytesRead);
							bytesRead = is.read(buffer);
						}
					} finally {
						if (is != null) {
							is.close();
						}
						if (os != null)
							os.close();
					}
					if (is != null) {
						is.close();
					}
					if (os != null)
						os.close();
				}
			} catch (IOException e) {
				logger.warn("拷贝资源发生异常", e);
			}
		}
	}

	protected static boolean deleteDirectory(File directory) {
		if ((directory.exists()) && (directory.isDirectory())) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					deleteDirectory(files[i]);
				else {
					files[i].delete();
				}
			}
		}
		return directory.delete();
	}

	protected Map<Object, Object> buildInitialPropertyMap()
			throws FrameworkException {
		Map<Object, Object> map = new HashMap<Object, Object>();
		Properties launchProperties = loadProperties("/WEB-INF/eclipse/launch.ini");
		for (Iterator<Entry<Object, Object>> it = launchProperties.entrySet()
				.iterator(); it.hasNext();) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			if (key.endsWith("*")) {
				if (value.equals("@null")) {
					clearPrefixedSystemProperties(
							key.substring(0, key.length() - 1), map);
				}
			} else if (value.equals("@null")) {
				map.put(key, null);
			} else {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		try {
			if (map.get("osgi.install.area") == null) {
				map.put("osgi.install.area", platformDirectory.toURI().toURL()
						.toExternalForm());
			}

			if (map.get("osgi.syspath") == null) {
				String installArea = (String) map.get("osgi.install.area");
				if (installArea.startsWith("file:")) {
					installArea = installArea.substring("file:".length());
				}
				map.put("osgi.syspath", installArea);
			}

			if (map.get("osgi.configuration.area") == null) {
				File configurationDirectory = new File(platformDirectory,
						"configuration");
				if (!configurationDirectory.exists()) {
					configurationDirectory.mkdirs();
				}
				map.put("osgi.configuration.area", configurationDirectory
						.toURI().toURL().toExternalForm());
			}

			// if (map.get("osgi.instance.area") == null) {
			// File workspaceDirectory = new File(platformDirectory,
			// "workspace");
			// if (!workspaceDirectory.exists()) {
			// workspaceDirectory.mkdirs();
			// }
			// map.put("osgi.instance.area", workspaceDirectory.toURI()
			// .toURL().toExternalForm());
			// }

			if (map.get("osgi.framework") == null) {
				String installArea = (String) map.get("osgi.install.area");
				if (installArea.startsWith("file:")) {
					installArea = installArea.substring("file:".length());
				}
				String path = new File(installArea, "plugins").toString();
				path = searchFor("org.eclipse.osgi", path);
				if (path == null) {
					throw new FrameworkException("为能找到OSGI Framewwork");
				}
				map.put("osgi.framework", new File(path).toURI().toURL()
						.toExternalForm());
			}
		} catch (MalformedURLException e) {

			throw new FrameworkException("读取OSGI配置文件发生异常", e);
		}
		return map;
	}

	protected Properties loadProperties(String resource) {
		Properties result = new Properties();
		InputStream in = null;
		try {
			URL location = servletContext.getResource(resource);
			if (location != null) {
				in = location.openStream();
				result.load(in);
			}
		} catch (Exception e) {
			if (in != null)
				try {
					in.close();
				} catch (IOException e2) {
				}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e3) {
				}
		}
		return result;
	}

	protected void buildFrameworkPropertyMap(Map<Object, Object> map) {
		String includes = getProperty(
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_PACKAGES_PROPERTIES,
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_PACKAGE_PROPERTIES_KEY);
		if (includes != null) {
			String packages = (String) map
					.get("org.osgi.framework.system.packages");
			map.put("org.osgi.framework.system.packages",
					(packages != null && !"".equals(packages.trim())) ? packages
							+ ", " + includes
							: includes);
			logger.debug("导入到OSGI框架的JAVA包  ："
					+ map.get("org.osgi.framework.system.packages"));
		}

		String autostart = getProperty(
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_BUNDLES_PROPERTIES,
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_BUNDLES_PROPERTIES_KEY);
		if (autostart != null) {
			String bundles = (String) map.get("osgi.bundles");
			map.put("osgi.bundles",
					(bundles != null && !"".equals(bundles.trim())) ? bundles
							+ "," + autostart : autostart);
			logger.debug("自动启动的Bundles : " + map.get("osgi.bundles"));
		}
	}

	protected String getProperty(String file, String key) {
		URL location = Thread.currentThread().getContextClassLoader()
				.getResource(file);
		Properties result = new Properties();
		InputStream in = null;
		try {
			if (location != null) {
				in = location.openStream();
				result.load(in);
			}
		} catch (Exception e) {
			logger.warn("读取Riambosoft Framework 配置文件" + file + "异常", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
		return (String) result.get(key);
	}

	private static void clearPrefixedSystemProperties(String prefix,
			Map<Object, Object> targetPropertyMap) {
		for (Iterator<Object> it = System.getProperties().keySet().iterator(); it
				.hasNext();) {
			String propertyName = (String) it.next();
			if ((propertyName.startsWith(prefix))
					&& (!targetPropertyMap.containsKey(propertyName))) {
				targetPropertyMap.put(propertyName, null);
			}
		}
	}

	protected String searchFor(final String target, String start) {
		File[] candidates = new File(start).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.equals(target)) || (name.startsWith(target + "_"));
			}
		});

		if (candidates == null) {
			return null;
		}
		String[] arrays = new String[candidates.length];
		for (int i = 0; i < arrays.length; i++) {
			arrays[i] = candidates[i].getName();
		}
		int result = findMax(arrays);
		if (result == -1)
			return null;
		return candidates[result].getAbsolutePath().replace(File.separatorChar,
				'/')
				+ (candidates[result].isDirectory() ? "/" : "");
	}

	protected int findMax(String[] candidates) {
		int result = -1;
		Object maxVersion = null;
		for (int i = 0; i < candidates.length; i++) {
			String name = candidates[i];
			String version = "";
			int index = name.indexOf('_');
			if (index != -1)
				version = name.substring(index + 1);
			Object currentVersion = getVersionElements(version);
			if (maxVersion == null) {
				result = i;
				maxVersion = currentVersion;
			} else if (compareVersion((Object[]) maxVersion,
					(Object[]) currentVersion) < 0) {
				result = i;
				maxVersion = currentVersion;
			}
		}
		return result;
	}

	private int compareVersion(Object[] left, Object[] right) {
		int result = ((Integer) left[0]).compareTo((Integer) right[0]);
		if (result != 0) {
			return result;
		}
		result = ((Integer) left[1]).compareTo((Integer) right[1]);
		if (result != 0) {
			return result;
		}
		result = ((Integer) left[2]).compareTo((Integer) right[2]);
		if (result != 0) {
			return result;
		}
		return ((String) left[3]).compareTo((String) right[3]);
	}

	private Object[] getVersionElements(String version) {
		if (version.endsWith(".jar"))
			version = version.substring(0, version.length() - 4);
		Object[] result = { new Integer(0), new Integer(0), new Integer(0), "" };
		StringTokenizer t = new StringTokenizer(version, ".");
		int i = 0;
		while ((t.hasMoreTokens()) && (i < 4)) {
			String token = t.nextToken();
			if (i < 3) {
				try {
					result[(i++)] = new Integer(token);
				} catch (Exception localException) {
					break;
				}
			} else {
				result[(i++)] = token;
			}
		}
		return result;
	}

	protected String[] buildCommandLineArguments(String commandLine) {
		List<String> args = new ArrayList<String>();
		if (commandLine != null) {
			StringTokenizer tokenizer = new StringTokenizer(commandLine,
					" \t\n\r\f");
			while (tokenizer.hasMoreTokens()) {
				String arg = tokenizer.nextToken();
				if (arg.startsWith("\"")) {
					String remainingArg = tokenizer.nextToken("\"");
					arg = arg.substring(1) + remainingArg;
					tokenizer.nextToken(" \t\n\r\f");
				} else if (arg.startsWith("'")) {
					String remainingArg = tokenizer.nextToken("'");
					arg = arg.substring(1) + remainingArg;
					tokenizer.nextToken(" \t\n\r\f");
				}
				args.add(arg);
			}
		}
		return args.toArray(new String[0]);
	}

	private void registerRestartHandler(Class<?> starterClazz)
			throws NoSuchMethodException, ClassNotFoundException,
			IllegalAccessException, InvocationTargetException {
		Method registerFrameworkShutdownHandler = null;
		try {
			registerFrameworkShutdownHandler = starterClazz.getDeclaredMethod(
					"internalAddFrameworkShutdownHandler",
					new Class[] { Runnable.class });
		} catch (NoSuchMethodException localNoSuchMethodException) {

			logger.info(starterClazz.getName()
					+ " 不支持设置 shutdown handler, 故不可设置重启");
			return;
		}
		if (!registerFrameworkShutdownHandler.isAccessible()) {
			registerFrameworkShutdownHandler.setAccessible(true);
		}
		Runnable restartHandler = createRestartHandler();
		registerFrameworkShutdownHandler.invoke(null,
				new Object[] { restartHandler });
	}

	private Runnable createRestartHandler() throws ClassNotFoundException,
			NoSuchMethodException {
		Class<?> frameworkPropertiesClazz = frameworkClassLoader
				.loadClass("org.eclipse.osgi.framework.internal.core.FrameworkProperties");
		final Method getProperty = frameworkPropertiesClazz.getMethod(
				"getProperty", new Class[] { String.class });
		Runnable restartHandler = new Runnable() {
			public void run() {
				try {
					String forcedRestart = (String) getProperty.invoke(null,
							new Object[] { "osgi.forcedRestart" });
					if (Boolean.valueOf(forcedRestart).booleanValue()) {
						DefaultFrameworkLauncher.this.restart();
					}
				} catch (InvocationTargetException ite) {
					Throwable t = ite.getTargetException();
					if (t == null) {
						t = ite;
					}
					throw new RuntimeException(t.getMessage());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		};
		return restartHandler;
	}

	protected class ChildFirstURLClassLoader extends URLClassLoader {

		public ChildFirstURLClassLoader(URL[] urls) {
			super(urls);
		}

		public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent,
				URLStreamHandlerFactory factory) {
			super(urls, parent, factory);
		}

		public URL getResource(String name) {
			URL resource = findResource(name);
			if (resource == null) {
				ClassLoader parent = getParent();
				if (parent != null)
					resource = parent.getResource(name);
			}
			return resource;
		}

		protected synchronized Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			Class<?> clazz = findLoadedClass(name);
			if (clazz == null) {
				try {
					clazz = findClass(name);
				} catch (ClassNotFoundException localClassNotFoundException) {
					ClassLoader parent = getParent();
					if (parent != null)
						clazz = parent.loadClass(name);
					else {
						clazz = ClassLoader.getSystemClassLoader().loadClass(
								name);
					}
				}
			}
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}

		protected PermissionCollection getPermissions(CodeSource codesource) {
			return DefaultFrameworkLauncher.allPermissions;
		}
	}

}