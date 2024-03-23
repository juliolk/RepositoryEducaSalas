package com.adm.xcp.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

@WebListener
public class ContextListener implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

}
