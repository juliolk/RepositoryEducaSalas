package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.extend.XcpComandoGruposManut;
import com.adm.ejb.entity.extend.XcpComandoManut;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpComandoGruposBacking extends XcpManutencaoBacking<XcpComandoBacking> implements Serializable {

	public XcpComandoGruposBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpComandoBacking.class);
	}

	@Override
	public XcpComandoGruposManut getEntity() {
		XcpComandoGruposManut entity = (XcpComandoGruposManut) super.getEntity();
		if (entity == null) {
			entity = new XcpComandoGruposManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		//Busca a entidade PAI e se não for nova busca os detalhes
		XcpComandoManut entityMestre = this.getMestreBacking().getEntity();
		if (!entityMestre.isNovo()) {
			MontaQuery q = new MontaQuery(XcpComandoGruposManut.class, "codGrupoFk.desGrupo");
			q.addWhere("seqComando", "=", entityMestre.getSeqComando());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo()) {
			//Seta a chave do PAI
			XcpComandoManut entityMestre = this.getMestreBacking().getEntity();
			this.getEntity().setSeqComando(entityMestre.getSeqComando());
		}

		return super.gravar();
	}

}
