package mobi.dadoudou.framework.core.web;

/**
 * @author changhu
 */
public class ParserException extends Exception {

	private static final long serialVersionUID = 1L;

	private String content;

	public ParserException(Class<?> cls) {
		super(cls.getName() + " is not a subclass of AnalysisiProvider");
	}

	public ParserException(String msg, String content) {
		super(msg + "\n" + content);
		this.content = content;
	}

	public ParserException(String msg) {
		super(msg);
	}

	public String getContent() {
		return this.content;
	}
}
