package mobi.dadoudou.framework;

import org.osgi.framework.BundleContext;

public interface FrameworkLauncher {

	public void init(FrameworkServletContext servletContext) throws FrameworkException;

	public void deploy() throws FrameworkException;

	public void undeploy() throws FrameworkException;

	public BundleContext start(String commondLine) throws FrameworkException;

	public void stop() throws FrameworkException;

	public void destroy() throws FrameworkException;
	
	public void restart() throws FrameworkException;
}
