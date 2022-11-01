/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package rife.engine;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.*;
import rife.config.RifeConfig;
import rife.servlet.RifeFilter;

import java.util.EnumSet;

public class Server {
    private final org.eclipse.jetty.server.Server server_ = new org.eclipse.jetty.server.Server();
    private final ServletContextHandler handler_ = new ServletContextHandler();

    public Server port(int port) {
        RifeConfig.server().setPort(port);
        return this;
    }

    public Server staticResourceBase(String base) {
        RifeConfig.server().setStaticResourceBase(base);
        return this;
    }

    public Server start(Site site) {
        try (var connector = new ServerConnector(server_)) {
            connector.setPort(RifeConfig.server().getPort());
            server_.setConnectors(new Connector[]{connector});
        }

        server_.setHandler(handler_);

        var rife_filter = new RifeFilter();
        rife_filter.site(site);
        var filter_holder = new FilterHolder(rife_filter);

        var ctx = new ServletContextHandler();
        ctx.setContextPath("/");

        var default_servlet = new DefaultServlet();
        var servlet_holder = new ServletHolder(default_servlet);
        if (RifeConfig.server().getStaticResourceBase() != null) {
            handler_.setResourceBase(RifeConfig.server().getStaticResourceBase());
        }

        handler_.addFilter(filter_holder, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler_.addServlet(servlet_holder, "/*");

        try {
            server_.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public void stop() {
        try {
            server_.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}