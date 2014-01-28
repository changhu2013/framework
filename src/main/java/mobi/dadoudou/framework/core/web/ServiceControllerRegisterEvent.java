package mobi.dadoudou.framework.core.web;

import org.springframework.context.ApplicationEvent;

public class ServiceControllerRegisterEvent extends ApplicationEvent {

	private static final long serialVersionUID = 7052380386276473252L;

	private ServiceController controller;

	public ServiceControllerRegisterEvent(Object source, ServiceController controller) {
		super(source);
		this.controller = controller;
	}

	public ServiceController getServiceController() {
		return controller;
	}
}
