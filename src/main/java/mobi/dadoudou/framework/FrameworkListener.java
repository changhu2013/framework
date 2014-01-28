package mobi.dadoudou.framework;

import java.util.Timer;
import java.util.TimerTask;

import mobi.dadoudou.framework.core.web.ServiceControllerRegisterEvent;
import mobi.dadoudou.framework.impl.DefaultFrameworkLauncher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;


@SuppressWarnings("rawtypes")
public class FrameworkListener implements ApplicationListener {

	private Log logger = LogFactory.getLog(FrameworkListener.class);

	// 启动OSGI的任务,当所有的WEB模块都启动之后在启动该任务
	private Timer initTimer;

	private long initDelay = 3000;

	private FrameworkServletContext frameworkServletContext;

	private FrameworkLauncher frameworkLauncher;

	private String frameworkLauncherClass;

	private String frameworkCommandLine;

	public long getInitDelay() {
		return initDelay;
	}

	public void setInitDelay(long initDelay) {
		this.initDelay = initDelay;
	}

	public FrameworkServletContext getFrameworkServletContext() {
		return frameworkServletContext;
	}

	public void setFrameworkServletContext(
			FrameworkServletContext frameworkServletContext) {
		this.frameworkServletContext = frameworkServletContext;
	}

	public FrameworkLauncher getFrameworkLauncher() {
		return frameworkLauncher;
	}

	public String getFrameworkLauncherClass() {
		return frameworkLauncherClass;
	}

	public void setFrameworkLauncherClass(String frameworkLauncherClass) {
		this.frameworkLauncherClass = frameworkLauncherClass;
	}

	public String getFrameworkCommandLine() {
		return frameworkCommandLine;
	}

	public void setFrameworkCommandLine(String frameworkCommandLine) {
		this.frameworkCommandLine = frameworkCommandLine;
	}

	public void init() {
		
		FrameworkServletContext servletContext = getFrameworkServletContext();

		if (servletContext == null) {

			logger.error("Riambsoft Framework 启动失败,未能获取上下文");
			return;
		}

		if (getFrameworkLauncherClass() != null)

			try {

				Class<?> frameworkLauncherClass = getClass().getClassLoader()
						.loadClass(getFrameworkLauncherClass());

				frameworkLauncher = ((FrameworkLauncher) frameworkLauncherClass
						.newInstance());

			} catch (Exception e) {
				logger.error("Riambsoft Framework filed to start", e);
			}

		else {
			frameworkLauncher = new DefaultFrameworkLauncher();
		}

		servletContext.setFrameworkLauncher(frameworkLauncher);

		try {

			frameworkLauncher.init(servletContext);

			frameworkLauncher.deploy();

			BundleContext buncleContext = frameworkLauncher
					.start(getFrameworkCommandLine());

			servletContext.setBuncleContext(buncleContext);

		} catch (FrameworkException e) {

			logger.error("Riambsoft Framework 启动失败", e);
		}
	}

	public void destroy() {

		try {
			frameworkLauncher.stop();
			frameworkLauncher.destroy();
		} catch (FrameworkException e) {
			logger.error("Riambsoft Framework filed to stop", e);
		}
	}

	public void onApplicationEvent(ApplicationEvent event) {

		/**
		 * 监听ServiceController的注册事件 当发生该事件的时候,开启一个启动OSGI容器的任务
		 * 如果该任务尚未启动又发生该事件,则取消之前的任务 并重新设定启动OSGI的任务
		 */
		System.out.println("有事件发生：" + event);
		if (event instanceof ServiceControllerRegisterEvent) {

			if (initTimer != null) {

				initTimer.cancel();
				initTimer.purge();
				initTimer = null;
			}

			initTimer = new Timer();
			initTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					FrameworkListener.this.init();
				}

			}, getInitDelay());
		} else if (event instanceof ContextClosedEvent) {
			/**
			 * 当WEB上下文关闭的时候关闭OSGI容器
			 */
			destroy();
		}
	}

}
