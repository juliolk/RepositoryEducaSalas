package com.adm.xcp.backing;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.adm.ejb.entity.Menus;
import com.adm.xcp.util.XcpViewConstants;

@ManagedBean
@RequestScoped
public class XcpIndexBacking extends XcpAbstractBacking {

	public void preRenderView() {
		if (this.getSession().isLogged()) {
			this.navigateTo(XcpViewConstants.PG_HOME_REDIRECT);
		} else if (Menus.PERMISSAO_2_PORTAL.equals(this.getTipoAcesso())) {
			this.navigateTo(XcpViewConstants.PG_PORTAL_REDIRECT);
		} else {
			this.navigateTo(XcpViewConstants.PG_LOGIN_REDIRECT);
		}
	}
}
