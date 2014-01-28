package mobi.dadoudou.framework.core.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServiceControllerManager implements ApplicationContextAware {

	private Log logger = LogFactory.getLog(ServiceControllerManager.class);

	private static ServiceControllerManager instance;

	private ApplicationContext applicationContext;

	private Map<String, ServiceController> map;

	private ServiceControllerManager() {
		map = Collections
				.synchronizedMap(new HashMap<String, ServiceController>());
	}

	public static ServiceControllerManager getInstance() {
		if (instance == null) {
			instance = new ServiceControllerManager();
		}
		return instance;
	}

	public void register(ServiceController controller) {
		String name = controller.getControllerName();
		logger.debug("注册控制器:" + name + " " + controller);
		synchronized (map) {
			map.put(name, controller);
		}
		applicationContext.publishEvent(new ServiceControllerRegisterEvent(
				this, controller));
	}

	public ServiceController getServiceController(String name) {
		ServiceController controller = map.get(name);
		return controller;
	}

	/**
	 * 获取已注册的服务控制器名字的迭代器,可用于对服务控制器的迭代。
	 * 
	 * @return
	 */
	public Iterator<String> getServiceControllerNameIterator() {
		return map.keySet().iterator();
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
