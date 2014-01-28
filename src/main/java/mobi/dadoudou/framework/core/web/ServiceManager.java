package mobi.dadoudou.framework.core.web;

import java.util.Iterator;
import java.util.Map;

public class ServiceManager {

	private ServiceController controller;

	private Map<String, Object> services;

	public ServiceManager(ServiceController controller,
			Map<String, Object> services) {
		super();
		this.controller = controller;
		this.services = services;
	}

	public void setController(ServiceController controller) {
		this.controller = controller;
	}

	public void setServices(Map<String, Object> services) {
		this.services = services;
	}

	public void init() {
		for (Iterator<String> iter = services.keySet().iterator(); iter
				.hasNext();) {
			String serviceName = (String) iter.next();
			Object service = services.get(serviceName);
			controller.addService(serviceName, service);
		}
	}

	public void destroy() {
		for (Iterator<String> iter = services.keySet().iterator(); iter
				.hasNext();) {
			String serviceName = (String) iter.next();
			Object service = services.get(serviceName);
			controller.removeService(serviceName, service);
		}
	}

}
