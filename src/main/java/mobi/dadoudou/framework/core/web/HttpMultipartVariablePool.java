package mobi.dadoudou.framework.core.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mobi.dadoudou.framework.core.Variable;
import mobi.dadoudou.framework.core.VariablePool;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class HttpMultipartVariablePool extends VariablePool {

	private Map<String, Variable> sessionVariableMap;

	private Map<String, Variable> parameterVariableMap;

	private Map<String, Variable> requestVariableMap;

	private String rsDataType;

	private File tempFileDir;

	/**
	 * 构造方法
	 * 
	 * @param request
	 *            HTTP请求对象
	 * @param response
	 *            HTTP响应对象
	 */
	public HttpMultipartVariablePool(HttpServletRequest request,
			HttpServletResponse response) {
		super();
		this.sessionVariableMap = new HashMap<String, Variable>();
		this.requestVariableMap = new HashMap<String, Variable>();
		this.parameterVariableMap = new HashMap<String, Variable>();
		this.bindParameters(request, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param rsDataType
	 */
	public HttpMultipartVariablePool(HttpServletRequest request,
			HttpServletResponse response, String rsDataType) {
		super();
		this.sessionVariableMap = new HashMap<String, Variable>();
		this.requestVariableMap = new HashMap<String, Variable>();
		this.parameterVariableMap = new HashMap<String, Variable>();
		this.rsDataType = rsDataType;
		this.bindParameters(request, response);
	}

	/**
	 * 获取变量值
	 * 
	 * @param name
	 *            变量名称
	 * @param clazz
	 *            目标类型
	 * @return value 值
	 */
	public Object getValue(String name, Class<?> clazz) {
		Object result = super.getValue(name, clazz);
		if (result == null) {
			try {
				Variable variable = getVariable(name);
				Object value = variable.getValue();
				if (this.rsDataType != null) {
					return SuperParser.marshal(this.rsDataType, value, clazz);
				} else {
					return SuperParser.marshal(value, clazz);
				}
			} catch (Exception e) {
				return null;
			}
		} else {
			return result;
		}
	}

	/**
	 * 添加Session中的可用变量
	 * 
	 * @param variableName
	 * @param value
	 */
	public void addSessionVariable(String variableName, Object value) {
		Variable variable = this.add(variableName, value);
		this.sessionVariableMap.put(variableName, variable);
	}

	/**
	 * 添加Request中的可用变量
	 * 
	 * @param variableName
	 * @param value
	 */
	public void addRequestVariable(String variableName, Object value) {
		this.requestVariableMap
				.put(variableName, this.add(variableName, value));
	}

	/**
	 * 添加请求参数
	 * 
	 * @param variableName
	 * @param value
	 */
	public void addParameterVariable(String variableName, Object value) {
		this.parameterVariableMap.put(variableName,
				this.add(variableName, value));
	}

	/**
	 * 将请求中的可用参数添加到变量池中
	 * 
	 * @param request
	 */
	private void bindParameters(HttpServletRequest request,
			HttpServletResponse response) {

		HttpSession session = request.getSession();
		this.add(HttpServletVariablePool.PARAMETER_HTTP_SESSION,
				HttpSession.class, session);
		this.add(HttpServletVariablePool.PARAMETER_HTTP_REQUEST,
				HttpServletRequest.class, request);
		this.add(HttpServletVariablePool.PARAMETER_HTTP_RESPONSE,
				HttpServletResponse.class, response);

		Enumeration<String> sessEnum = session.getAttributeNames();
		while (sessEnum.hasMoreElements()) {
			String variableName = (String) sessEnum.nextElement();
			this.addSessionVariable(variableName,
					session.getAttribute(variableName));
		}
		Enumeration<String> attrEnum = request.getAttributeNames();
		while (attrEnum.hasMoreElements()) {
			String variableName = (String) attrEnum.nextElement();
			this.addRequestVariable(variableName,
					request.getAttribute(variableName));
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// 设置上传时的临时文件目录,路径由upload.xml 中<local-upload-temp-docu-path> 指定,
		// 此节点的路径需要自己手动创建,否则会报 java.io.FileNotFoundException

		// File tempDir = new File(FtpConfig.getTempPath());
		// factory.setRepository(tempDir);

		// 表示多大的文件将启用临时文件 默认30M
		factory.setSizeThreshold(30 * 1024 * 1024);

		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<?> items = upload.parseRequest(request);
			Iterator<?> iter = items.iterator();

			List<File> files = new ArrayList<File>();
			tempFileDir = createTempDir();

			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					this.processFormField(item);
				} else {
					File temp = this.processUploadedFile(tempFileDir, item);
					if (temp != null) {
						files.add(temp);
					}
				}
			}

			// 将上传的文件列表放入变量池
			this.addParameterVariable("files", files);

		} catch (Exception e) {
			e.printStackTrace();

		}
	};

	public void processFormField(FileItem field)
			throws UnsupportedEncodingException {
		if (field.isFormField()) {
			String name = field.getFieldName();
			String value = field.getString();
			name = java.net.URLDecoder.decode(name, ContentType.getEncoding());
			value = java.net.URLDecoder
					.decode(value, ContentType.getEncoding());

			this.addParameterVariable(name, value);
		}
	}

	public File processUploadedFile(File dir, FileItem item) throws Exception {
		if (!item.isFormField()) {
			String name = item.getName();
			name = java.net.URLDecoder.decode(name, ContentType.getEncoding());

			File temp = new File(dir, name);
			item.write(temp);

			return temp;
		}
		return null;
	}

	private File createTempDir() {
		String temp = System.getProperty("java.io.tmpdir");
		File dir = new File(temp + File.separator + "upload" + File.separator
				+ System.currentTimeMillis());
		dir.mkdirs();
		return dir;
	}

	public void destroy() {
		super.destroy();
		if (tempFileDir != null) {
			tempFileDir.delete();
		}
		sessionVariableMap.clear();
		parameterVariableMap.clear();
		requestVariableMap.clear();

		sessionVariableMap = null;
		parameterVariableMap = null;
		requestVariableMap = null;
	}
}
