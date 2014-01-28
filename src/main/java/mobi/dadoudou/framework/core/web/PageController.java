package mobi.dadoudou.framework.core.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PageController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		String uri = req.getRequestURI();

		uri = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf(".jsp"));

		ModelAndView mv = new ModelAndView();

		mv.setViewName(uri);

		return mv;
	}

}