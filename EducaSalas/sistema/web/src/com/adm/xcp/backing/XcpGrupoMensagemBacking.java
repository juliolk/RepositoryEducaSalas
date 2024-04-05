package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.extend.XcpGrupoMensagemManut;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpGrupoMensagemBacking extends XcpManutencaoBacking implements Serializable {

	public XcpGrupoMensagemBacking() {
		this.setTipomManut(TIPO_MANUT.MESTRE);
		this.setDetalhesBacking(XcpGrupoDestBacking.class);
	}

	@Override
	public XcpGrupoMensagemManut getEntity() {
		XcpGrupoMensagemManut entity = (XcpGrupoMensagemManut) super.getEntity();
		if (entity == null) {
			entity = new XcpGrupoMensagemManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpGrupoMensagemManut.class, "desGrupo"));
	}

}
