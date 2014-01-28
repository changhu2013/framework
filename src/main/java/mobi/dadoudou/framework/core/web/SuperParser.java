package mobi.dadoudou.framework.core.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 工具类，负责根据系统配置的Parser进行数据解码和编码。<br/>
 * 当类被加载的时候加载配置文件。 配置文件：proxy-config.xml
 * 
 */
public class SuperParser {

	private static Map<String, Parser> registry;

	private static Log logger = LogFactory.getLog(SuperParser.class);

	static {
		try {
			SuperParser.registry = new HashMap<String, Parser>();
			SuperParser.loadConfig(Thread.currentThread()
					.getContextClassLoader().getResource("parser-config.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载配置文件并注册Parser
	 * 
	 * @param url
	 *            文件UIL
	 * @throws ParserException
	 */
	private static void loadConfig(URL url) throws ParserException {

		logger.debug("load config url:" + url);
		try {

			InputStream stream = url.openStream();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document config = builder.parse(stream);
			NodeList parsers = config.getElementsByTagName("parser");

			for (int i = 0; i < parsers.getLength(); i++) {
				Node provider = parsers.item(i);
				NamedNodeMap parser = provider.getAttributes();
				String prefix = parser.getNamedItem("prefix").getNodeValue()
						.toUpperCase();
				SuperParser.register(prefix, parser);
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ParserException("Cannot open stream from given url:"
					+ url.toExternalForm());
		} catch (ParserConfigurationException e) {
			throw new ParserException("Cannot parser config from url"
					+ url.getPath());
		} catch (SAXException e) {
			throw new ParserException("Cannot parser config from url"
					+ url.getPath());
		}
	}

	/**
	 * 注册Parser
	 * 
	 * @param prefix
	 *            Parser前缀
	 * @param parser
	 * @throws ParserException
	 */
	public static void register(String prefix, NamedNodeMap parser)
			throws ParserException {

		String className = parser.getNamedItem("class").getNodeValue();

		Class<?> subClass = null;
		try {

			subClass = Class.forName(className);
			if (Parser.class.isAssignableFrom(subClass)) {
				Parser p = (Parser) subClass.newInstance();
				p.setPrefix(prefix);
				SuperParser.registry.put(prefix, p);
			} else {
				throw new ParserException("Class:" + subClass.getName()
						+ " is not subclass of " + Parser.class.getName());
			}
		} catch (ClassNotFoundException e1) {
			throw new ParserException("get an instance of Class: "
					+ e1.getMessage());
		} catch (InstantiationException e2) {
			throw new ParserException("get an instance of Class:"
					+ subClass.getName() + " exception!");
		} catch (IllegalAccessException e3) {
			throw new ParserException("get an instance of Class:"
					+ subClass.getName() + " exception!");
		}
	}

	/**
	 * 对传入参数进行解码。该方法会使用默认的字符编码。
	 * 
	 * @param bean
	 *            值
	 * @param clazz
	 *            解码后的类型
	 * @return {@link Object}
	 * @throws ParserException
	 *             当没有可对传入对象进行解码的解析器时，抛出异常
	 */
	public static Object marshal(Object bean, Class<?> clazz)
			throws ParserException {
		List<Parser> list = SuperParser.getParsers();
		Object obj = null;
		for (Iterator<Parser> iter = list.iterator(); iter.hasNext();) {
			Parser temp = iter.next();
			try {
				obj = temp.marshal(bean, clazz);
				if (obj != null) {
					return obj;
				}
			} catch (ParserException e) {
				logger.debug("parser " + temp.getPrefix() + " cant unmartshal "
						+ bean);
			}
		}
		throw new ParserException("没有可对" + bean + "进行解码的解析器");
	}

	/**
	 * 使用指定的解析器对传入参数进行解码。
	 * 
	 * @param prefix
	 *            指定解析器前缀
	 * @param bean
	 *            值
	 * @param clazz
	 *            解码后的类型
	 * @return {@link Object}
	 * @throws NoneParserException
	 *             当没有找到指定的解析器时抛出该异常
	 * @throws ParserException
	 */
	public static Object marshal(String prefix, Object bean, Class<?> clazz)
			throws NoneParserException, ParserException {
		logger.debug("malshal bean: " + bean + " prefix:" + prefix);
		if (bean == null) {
			return null;
		}
		Parser p = SuperParser.getParser(prefix);
		return p.marshal(bean, clazz);
	}

	/**
	 * 获取系统配置的解析器列表，并将指定的解析器放入到列表第一个位置。
	 * 
	 * @param prefix
	 *            需要放入第一个位置的解析器
	 * @return
	 */
	public static List<Parser> getParsers() {
		List<Parser> list = new ArrayList<Parser>();
		for (Iterator<String> itor = SuperParser.registry.keySet().iterator(); itor
				.hasNext();) {
			Parser temp = SuperParser.registry.get(itor.next());
			list.add(temp);
		}
		return list;
	}

	/**
	 * 对传入对象进行编码，该方法会一次使用定义的解析器对传入对象进行编码，直到成功为止， 如果没有可以对传入对象进行编码的解析器，则抛出异常。
	 * 
	 * @param bean
	 * @return
	 * @throws ParserException
	 */
	public static String unmarshal(Object bean) throws ParserException {
		logger.debug("unmalshal bean: " + bean);
		if (bean == null) {
			return null;
		}
		List<Parser> list = SuperParser.getParsers();
		String obj = null;
		for (Iterator<Parser> iter = list.iterator(); iter.hasNext();) {
			Parser temp = iter.next();
			try {
				obj = temp.unmarshal(bean);
				if (obj != null) {
					return obj;
				}
			} catch (ParserException e) {
				logger.debug("parser " + temp.getPrefix() + " cant unmartshal "
						+ bean);
			}
		}
		throw new ParserException("没有可对" + bean + "进行编码的解析器");
	}

	/**
	 * 对传入对象进行编码，该方法会一次使用定义的解析器对传入对象进行编码，直到成功为止， 如果没有可以对传入对象进行编码的解析器，则抛出异常。
	 * 
	 * @param bean
	 * @return
	 * @throws ParserException
	 */
	public static String unmarshal(Object bean, String name)
			throws ParserException {
		logger.debug("unmalshal bean: " + bean);
		if (bean == null) {
			return null;
		}
		List<Parser> list = SuperParser.getParsers();
		String obj = null;
		for (Iterator<Parser> iter = list.iterator(); iter.hasNext();) {
			Parser temp = (Parser) iter.next();
			try {
				obj = temp.unmarshal(bean, name);
				if (obj != null) {
					return obj;
				}
			} catch (ParserException e) {
				logger.debug("parser " + temp.getPrefix() + " cant unmartshal "
						+ bean);
			}
		}
		throw new ParserException("没有可对" + bean + "进行编码的解析器");
	}

	/**
	 * 使用指定的解析器进行编码
	 * 
	 * @param prefix
	 *            指定解析器的前缀
	 * @param bean
	 *            值
	 * @return {@link String} 编码字符串
	 * @throws ParserException
	 * @throws NoneParserException
	 */
	public static String unmarshal(String prefix, Object bean)
			throws ParserException, NoneParserException {
		logger.debug("unmarshal bean: " + bean + " prefix:" + prefix);
		if (bean == null) {
			return null;
		}
		Parser p = SuperParser.getParser(prefix);
		return p.unmarshal(bean);
	}

	/**
	 * 使用指定的解析器进行编码
	 * 
	 * @param prefix
	 *            指定解析器前缀
	 * @param name
	 *            值名称
	 * @param bean
	 *            值
	 * @return {@link String} 编码字符串
	 * @throws NoneParserException
	 * @throws ParserException
	 */
	public static String unmarshal(String prefix, Object bean, String name)
			throws NoneParserException, ParserException {
		logger.debug("unmarshal name: " + name + " bean: " + bean + " prefix: "
				+ prefix);
		if (name == null || "".equals(name.trim())) {
			return SuperParser.unmarshal(prefix, bean);
		} else {
			Parser p = SuperParser.getParser(prefix);
			return p.unmarshal(bean, name);
		}
	}

	/**
	 * 根据前缀获取语法解析器，如果没有配置该解析器抛出异常
	 * 
	 * @param prefix
	 *            语法解析器前缀
	 * @return {@link Parser}
	 * @throws NoneParserException
	 */
	public static Parser getParser(String prefix) throws NoneParserException {
		Parser parser = null;
		for (Iterator<String> i = SuperParser.registry.keySet().iterator(); i
				.hasNext();) {
			Parser temp = SuperParser.registry.get(i.next());
			if (prefix != null && prefix.toUpperCase().equals(temp.getPrefix())) {
				parser = temp;
				break;
			}
		}
		if (parser == null) {
			throw new NoneParserException("未配置解析器" + prefix);
		} else {
			return parser;
		}
	}
}
