package com.adm.xcp.util;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class XCapeExceptionHandlerFactory extends ExceptionHandlerFactory {

	private final javax.faces.context.ExceptionHandlerFactory parent;

	public XCapeExceptionHandlerFactory(final javax.faces.context.ExceptionHandlerFactory parent) {
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		return new XCapeExceptionHandler(this.parent.getExceptionHandler());
	}

}
