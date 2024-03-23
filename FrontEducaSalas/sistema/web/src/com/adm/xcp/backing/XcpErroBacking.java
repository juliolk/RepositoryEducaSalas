package com.adm.xcp.backing;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.adm.xcp.util.XCapeExceptionHandler;

@ManagedBean
@RequestScoped
public class XcpErroBacking extends XcpAbstractBacking {

	private String desErro;

	@PostConstruct
	public void init() {
		this.desErro = (String) getExternalContext().getSessionMap().get(XCapeExceptionHandler.ERROR_STACK);
		getExternalContext().getSessionMap().remove(XCapeExceptionHandler.ERROR_STACK);
	}

	public String getDesErro() {
		return this.desErro;
	}

	public void setDesErro(String desErro) {
		this.desErro = desErro;
	}

}
