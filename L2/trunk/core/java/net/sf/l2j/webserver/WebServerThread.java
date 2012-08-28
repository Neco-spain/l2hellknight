package net.sf.l2j.webserver;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

public class WebServerThread
{
	public WebServerThread(int web_server_port, String doc_root) throws Exception
	{
		// Create the server
		HttpServer server = new HttpServer();

		// Create a port listener
		SocketListener listener = new SocketListener();
		listener.setPort(web_server_port);
		server.addListener(listener);

		// Create a context 
		HttpContext context = new HttpContext();
		context.setContextPath("/");
		server.addContext(context);

		context.setResourceBase(doc_root);
		context.addHandler(new ResourceHandler());

		// Create a servlet container
		ServletHandler servlets = new ServletHandler();
		context.addHandler(servlets);
		servlets.addServlet("JServlet", "/", "net.sf.l2j.webserver.JServlet");

		// Start the http server
		server.start();
	}
}
