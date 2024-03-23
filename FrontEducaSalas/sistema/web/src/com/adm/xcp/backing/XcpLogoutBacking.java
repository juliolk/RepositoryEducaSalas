package com.adm.xcp.backing;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpSession;

import com.adm.xcp.util.XcpViewConstants;

@ManagedBean
@RequestScoped
public class XcpLogoutBacking extends XcpAbstractBackingBase {

	public void preRenderView() {
		HttpSession session = (HttpSession) getExternalContext().getSession(false);
		session.invalidate();
		this.navigateTo(XcpViewConstants.PG_LOGIN_REDIRECT);
	}
}
