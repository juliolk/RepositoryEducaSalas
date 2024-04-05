package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpScripts;
import com.adm.ejb.entity.extend.XcpScriptsManut;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpScriptsBacking extends XcpManutencaoBacking implements Serializable {

	public XcpScriptsBacking() {
	}

	@Override
	public XcpScriptsManut getEntity() {
		XcpScriptsManut entity = (XcpScriptsManut) super.getEntity();
		if (entity == null) {
			entity = new XcpScriptsManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpScriptsManut.class, "numScript"));
	}

	public Collection<SelectItem> getItensTipObjeto() {
		return this.getItens(
				"tipObjetoScript",
				XcpScripts.TIP_OBJETO_1_SEQUENCE,
				XcpScripts.TIP_OBJETO_2_PROCEDURE,
				XcpScripts.TIP_OBJETO_3_DATABASE,
				XcpScripts.TIP_OBJETO_4_PACKAGE,
				XcpScripts.TIP_OBJETO_5_TRIGGER,
				XcpScripts.TIP_OBJETO_6_MATERIALIZED,
				XcpScripts.TIP_OBJETO_7_INDEX,
				XcpScripts.TIP_OBJETO_8_TABLE,
				XcpScripts.TIP_OBJETO_9_FUNCTION,
				XcpScripts.TIP_OBJETO_10_VIEW,
				XcpScripts.TIP_OBJETO_11_TYPE,
				XcpScripts.TIP_OBJETO_12_COMMENT,
				XcpScripts.TIP_OBJETO_13_PACKAGE_BODY);

	}

}
