package mobi.dadoudou.framework.core.web;

/**
 * @author changhu
 */
public class NoneParserException extends Exception {

	private static final long serialVersionUID = 1L;

	private String content;

	public NoneParserException(Class<?> cls) {
		super(cls.getName() + " is not a subclass of AnalysisiProvider");
	}

	public NoneParserException(String msg, String content) {
		super(msg + "\n" + content);
		this.content = content;
	}

	public NoneParserException(String msg) {
		super(msg);
	}

	public String getContent() {
		return this.content;
	}
}
