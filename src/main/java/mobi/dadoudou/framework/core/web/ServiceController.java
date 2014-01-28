package mobi.dadoudou.framework.core.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobi.dadoudou.framework.core.ServiceEngineInterface;
import mobi.dadoudou.framework.core.ServiceException;
import mobi.dadoudou.framework.core.Variable;
import mobi.dadoudou.framework.core.VariablePool;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ServiceController implements Controller {

	private Log logger = LogFactory.getLog(ServiceController.class);

	public static final String HTTP_SESSION_KEY_FOR_USER = "_httpSessionKeyForUser";

	private Map<String, List<Object>> map;

	private String name;

	private ServiceEngineInterface serviceEngine;

	public ServiceController(String name, ServiceControllerManager manager) {
		super();
		this.name = name;
		this.map = Collections
				.synchronizedMap(new HashMap<String, List<Object>>());
		manager.register(this);
	}

	public void setServiceEngine(ServiceEngineInterface serviceEngine) {
		this.serviceEngine = serviceEngine;
	}

	public String getControllerName() {
		return name;
	}

	public void addService(String name, Object service) {
		synchronized (map) {
			List<Object> services = map.get(name);
			if (services == null) {
				services = new ArrayList<Object>();
				map.put(name, services);
			}
			logger.info("注册服务:" + name + "  " + service);
			services.add(service);
		}
	}

	public void removeService(String name, Object service) {
		synchronized (map) {
			List<Object> services = map.get(name);
			if (services != null) {
				services.remove(service);
			}
			logger.info("注销服务:" + name + "  " + service);
		}
	}

	/**
	 * 从请求中获取所要调用的方法名称, 如果没有传入则默认调用service方法
	 * 
	 * @param request
	 * @return
	 */
	private String getServiceMethodName(HttpServletRequest request) {
		String rsMethod = request.getHeader("Rs-method");
		if (rsMethod == null || "".equals(rsMethod.trim())) {
			rsMethod = request.getParameter("Rs-method");
		}
		return (rsMethod == null || "".equals(rsMethod.trim())) ? "service"
				: rsMethod;
	}

	public String getRsAccept(HttpServletRequest request) {
		String rsAccept = request.getHeader("Rs-accept");
		if (rsAccept == null || "".equals(rsAccept.trim())) {
			rsAccept = request.getParameter("Rs-accept");
		}
		return rsAccept == null ? "json" : rsAccept.toLowerCase();
	}

	public String getRsDataType(HttpServletRequest request) {
		String rsDataType = request.getHeader("Rs-dataType");
		if (rsDataType == null || "".equals(rsDataType.trim())) {
			rsDataType = request.getParameter("Rs-dataType");
		}
		return rsDataType == null ? "json" : rsDataType;
	}

	private VariablePool getVariablePool(HttpServletRequest request,
			HttpServletResponse response, String rsDataType) {
		boolean multipartRequest = ServletFileUpload
				.isMultipartContent(request);
		VariablePool vp = null;
		if (multipartRequest) {
			vp = new HttpMultipartVariablePool(request, response, rsDataType);
		} else {
			vp = new HttpServletVariablePool(request, response, rsDataType);
		}
		return vp;
	}

	/**
	 * 处理业务请求,包含后台业务服务请求和访问控制请求
	 */
	public ModelAndView handleRequest(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		handleServiceRequest(req, resp);
		return null;
	}

	/**
	 * 处理后台业务服务请求
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void handleServiceRequest(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {

		long start = System.currentTimeMillis();

		String controllerName = getControllerName();
		String serviceName = getServiceName(req);
		String methodName = getServiceMethodName(req);
		String rsDataType = getRsDataType(req);

		// 查找注册的业务方法
		List<Object> services = null;
		synchronized (map) {
			services = map.get(serviceName);
		}
		if (services == null || services.size() < 1) {
			response(req, resp, Boolean.FALSE, null,
					ServiceException.RSSERVICE_NOTFOUND_ERROR, "未找到注册的业务服务",
					methodName, serviceName, controllerName,
					System.currentTimeMillis() - start);
			return;
		}

		// 创建变量池,注意用完之后必须将该变量池销毁
		VariablePool pool = getVariablePool(req, resp, rsDataType);
		Variable result = null;
		try {
			for (Object service : services) {
				result = serviceEngine.invoke(service, methodName, pool);
			}
			if (result != null) {
				response(req, resp, Boolean.TRUE, result, null, null,
						methodName, serviceName, controllerName,
						System.currentTimeMillis() - start);
			} else {
				response(req, resp, Boolean.FALSE, null,
						ServiceException.HASRIGHT_RSMETHOD_NOTFOUND_ERROR,
						" 未找到您有权执行的业务方法", methodName, serviceName,
						controllerName, System.currentTimeMillis() - start);
			}
		} catch (Exception e) {
			String message = e.getMessage();
			String status = e.getClass().getName();
			if (e instanceof ServiceException) {
				status = ((ServiceException) e).getStatus();
			}
			response(req, resp, Boolean.FALSE, null, status, message,
					methodName, serviceName, controllerName,
					System.currentTimeMillis() - start);
		} finally {
			// 销毁变量池
			pool.destroy();
		}
	}

	/**
	 * 获取业务方法
	 * 
	 * @param req
	 * @return
	 */
	private String getServiceName(HttpServletRequest req) {
		String uri = req.getRequestURI();
		uri = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf(".rsc"));
		return uri;
	}

	/**
	 * 将业务方法调用结果以标准的数据格式发送到前台
	 */
	private void response(HttpServletRequest req, HttpServletResponse resp,
			Boolean succ, Variable result, String errorCode, String errorMsg,
			String rsMethod, String rsService, String rsController, long time) {

		String accept = getRsAccept(req);
		StringBuffer buf = new StringBuffer();

		// 现只支持两种数据格式XML和JSON，默认为JSON数据格式
		if ("xml".equals(accept)) {
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			buf.append("<Data>");
			buf.append("<Success>" + (succ ? "true" : "false") + "</Success>");
			if (succ) {
				try {
					String temp = SuperParser.unmarshal(accept,
							result.getValue(), result.getName());
					buf.append("<Result>" + temp + "</Result>");
				} catch (Exception e) {
					buf.append("<Error_Code>" + ServiceException.ERROR
							+ "</Error_Code>");
					buf.append("<Error_Message>" + e.getMessage()
							+ "</Error_Message>");
				}
			} else {
				buf.append("<Error_Code>" + errorCode + "</Error_Code>");
				buf.append("<Error_Message>" + errorMsg + "</Error_Message>");
			}

			buf.append("<Time unit=\"ms\">" + time + "</Time>");
			buf.append("<RS_Method>" + rsMethod + "</RS_Method>");
			buf.append("<RS_Service>" + rsService + "</RS_Service>");
			buf.append("<RS_Controller>" + rsController + "</RS_Controller>");
			buf.append("</Data>");
		} else {
			// 默认以JSON数据格式响应

			buf.append("{");
			buf.append("Success:" + (succ ? "true" : "false") + ",");
			if (succ) {
				// 业务方法调用成功
				try {
					String temp = SuperParser.unmarshal(accept,
							result.getValue(), result.getName());
					buf.append("Result:" + temp + ",");
				} catch (Exception e) {
					buf.append("Error_Code:'" + ServiceException.ERROR + "',");
					buf.append("Error_Message:'" + e.getMessage() + "',");
				}
			} else {
				// 业务方法调用失败
				buf.append("Error_Code:'" + errorCode + "',");
				buf.append("Error_Message:'" + errorMsg + "',");
			}
			buf.append("Time:" + time + ",");
			buf.append("RS_Method:'" + rsMethod + "',");
			buf.append("RS_Service:'" + rsService + "',");
			buf.append("RS_Controller:'" + rsController + "'");
			buf.append("}");
		}

		try {
			PrintWriter writer = resp.getWriter();

			resp.setContentType(ContentType.get(accept) + "; charset="
					+ ContentType.getEncoding());
			writer.append(buf);
			logger.info("响应内容:" + buf);
			writer.close();
		} catch (IOException e) {
			try {
				resp.sendError(500, e.getMessage());
			} catch (IOException e1) {
			}
		}
	}
}
