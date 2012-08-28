package net.sf.l2j.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.l2j.Config;
import net.sf.l2j.util.Files;

import org.apache.commons.logging.Log;
import org.mortbay.html.Composite;
import org.mortbay.log.LogFactory;
import org.mortbay.util.Loader;
import org.mortbay.util.LogSupport;

public class JServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(JServlet.class);

	/* ------------------------------------------------------------ */
	String pageType;

	/* ------------------------------------------------------------ */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}

	/* ------------------------------------------------------------ */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}

	/* ------------------------------------------------------------ */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setAttribute("JServlet", this);
		request.setCharacterEncoding("UTF-8");
		getServletContext().setAttribute("JServlet", this);

		String info = request.getPathInfo();
		if(info != null && info.endsWith("Exception"))
			try
			{
				throw (Throwable) Loader.loadClass(this.getClass(), info.substring(1)).newInstance();
			}
			catch(Throwable th)
			{
				throw new ServletException(th);
			}

		String redirect = request.getParameter("redirect");
		if(redirect != null && redirect.length() > 0)
		{
			response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
			response.sendRedirect(redirect);
			response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
			return;
		}

		String error = request.getParameter("error");
		if(error != null && error.length() > 0)
		{
			response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
			response.sendError(Integer.parseInt(error));
			response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
			return;
		}

		String length = request.getParameter("length");
		if(length != null && length.length() > 0)
			response.setContentLength(Integer.parseInt(length));

		String buffer = request.getParameter("buffer");
		if(buffer != null && buffer.length() > 0)
			response.setBufferSize(Integer.parseInt(buffer));

		if(request.getServletPath().contains(".fsh"))
			response.setContentType("text/html; charset=utf-8");
		else if(request.getServletPath().contains(".css"))
			response.setContentType("text/css");
		else if(request.getServletPath().contains(".txt"))
			response.setContentType("text/plain; charset=utf-8");

		if(info != null && info.indexOf("Locale/") >= 0)
			try
			{
				String locale_name = info.substring(info.indexOf("Locale/") + 7);
				Field f = java.util.Locale.class.getField(locale_name);
				response.setLocale((Locale) f.get(null));
			}
			catch(Exception e)
			{
				LogSupport.ignore(log, e);
				response.setLocale(Locale.getDefault());
			}

		PrintWriter pout = response.getWriter();
		Composite page = null;
		String TemplateFile = Config.WEB_SERVER_ROOT + request.getServletPath().replace(".fsh", ".fst").replace(".txt", ".fst");

		if(Config.DEBUG)
			System.out.println("Try load " + TemplateFile);

		if(TemplateFile.endsWith("/"))
			TemplateFile += "index.fst";

		String content = Files.read(TemplateFile);

		if(content == null)
			return;

		try
		{
			page = new Composite();
			page.add(PageParser.parse(content));
		}
		catch(Exception e)
		{
			log.warn(LogSupport.EXCEPTION, e);
			return;
		}

		page.write(pout);
		pout.close();
		request.getInputStream().close();

	}

	/* ------------------------------------------------------------ */
	@Override
	public String getServletInfo()
	{
		return "JBForthScript Servlet";
	}

	/* ------------------------------------------------------------ */
	@Override
	public synchronized void destroy()
	{
		log.debug("Destroyed");
	}

	/* ------------------------------------------------------------ */
	@SuppressWarnings("unused")
	private static String toString(Object o)
	{
		if(o == null)
			return null;

		if(o.getClass().isArray())
		{
			StringBuffer sb = new StringBuffer();
			Object[] array = (Object[]) o;
			for(int i = 0; i < array.length; i++)
			{
				if(i > 0)
					sb.append("\n");
				sb.append(array.getClass().getComponentType().getName());
				sb.append("[");
				sb.append(i);
				sb.append("]=");
				sb.append(toString(array[i]));
			}
			return sb.toString();
		}
		return o.toString();
	}
}