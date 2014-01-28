package mobi.dadoudou.framework.core;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -8800665127168375160L;

	/**
	 * 系统执行错误和非业务类异常
	 */
	public static final String ERROR = "0001";

	/**
	 * 执行业务方法时发生异常错误
	 */
	public static final String RSMETHOD_INVOKE_ERROR = "0002";

	/**
	 * 未找到要执行的业务方法
	 */
	public static final String RSMETHOD_NOTFOUND_ERROR = "0003";

	/**
	 * 未找到RS业务服务
	 */
	public static final String RSSERVICE_NOTFOUND_ERROR = "0004";

	/**
	 * 未找到权限验证服务
	 */
	public static final String ACCESSVALIDATOR_NOTFOUND_ERROR = "0010";

	/**
	 * 未找到用户有权执行的业务方法
	 */
	public static final String HASRIGHT_RSMETHOD_NOTFOUND_ERROR = "0011";

	private String status;

	public ServiceException(String status) {
		super();
		this.status = status;
	}

	public ServiceException(String status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public ServiceException(String status, String message) {
		super(message);
		this.status = status;
	}

	public ServiceException(String status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	/**
	 * 获取异常错误码
	 * 
	 * @return
	 */
	public String getStatus() {
		return status;
	}
}
