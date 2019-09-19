package com.feiliks.sap_bridge;

import com.feiliks.sap_bridge.servlets.ApiBridgeServlet;
import com.feiliks.sap_bridge.servlets.GetTableServlet;
import com.feiliks.sap_bridge.utils.JCoUtil;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;


public class SapBridgeServer {

	private static int readPort(String[] args) {
		if (args.length > 0) {
			try {
				return Integer.parseInt(args[0]);
			} catch (NumberFormatException ignored) {
			}
		}
		return 8081;
	}

	public static void main(String[] args) throws Exception {

		if (args.length > 1)
			JCoUtil.init(args[1]);
		else
			JCoUtil.init();

		Server server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(readPort(args));
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(ApiBridgeServlet.class, "/api/*");
		context.addServlet(GetTableServlet.class, "/get-table");
		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { context, new DefaultHandler() });

		server.setHandler(handlers);

		server.start();
		server.join();
	}

}

