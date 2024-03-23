package com.adm.xcp.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.adm.xcp.backing.XcpLayoutAppBacking;

public class XcpFaceletsFunctions implements XcpViewConstants {

	public static String getLayout(String template) {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if ("manut".equals(template)) {
			if (externalContext.getRequestMap().containsKey(PARAM_XCP_TEMPLATE_NAME)) {
				template = (String) externalContext.getRequestMap().get(PARAM_XCP_TEMPLATE_NAME);
			}
			
			// Se for acesso via portal mudar o template principal (manut)
			if(XcpLayoutAppBacking.getTipoAcessoStatic() == 2){
				template = "nportal";
			}
		}

		//Adiciona no request qual o template esta usando
		externalContext.getRequestMap().put(PARAM_XCP_TEMPLATE_NAME_CURRENT, template);
		return "/templates/" + template + ".xhtml";
	}

	public static String toJQueryId(String clientId) {

		if (clientId == null) {
			return null;
		}

		clientId = clientId.replace(":", "\\\\:");
		return "#" + clientId;
	}
}