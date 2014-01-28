package mobi.dadoudou.framework;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.springframework.web.context.ServletContextAware;

public class FrameworkServletContext implements ServletContextAware {

	private ServletContext servletContext;

	private FrameworkLauncher frameworkLauncher;

	private BundleContext buncleContext;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public FrameworkLauncher getFrameworkLauncher() {
		return frameworkLauncher;
	}

	public void setFrameworkLauncher(FrameworkLauncher frameworkLauncher) {

		this.frameworkLauncher = frameworkLauncher;

		servletContext.setAttribute(
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_LAUNCHER,
				frameworkLauncher);
	}

	public BundleContext getBuncleContext() {
		return buncleContext;
	}

	public void setBuncleContext(BundleContext buncleContext) {

		this.buncleContext = buncleContext;

		servletContext.setAttribute(
				FrameworkConstants.RIAMBSOFT_FRAMEWORK_BUNDLE_CONTEXT,
				buncleContext);
	}

	
}
