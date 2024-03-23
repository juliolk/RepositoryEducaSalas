package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpGrupoDest;
import com.adm.ejb.entity.extend.XcpGrupoDestManut;
import com.adm.ejb.entity.extend.XcpGrupoMensagemManut;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpGrupoDestBacking extends XcpManutencaoBacking<XcpGrupoMensagemBacking> implements Serializable {

	public XcpGrupoDestBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpGrupoMensagemBacking.class);
	}

	@Override
	public XcpGrupoDestManut getEntity() {
		XcpGrupoDestManut entity = (XcpGrupoDestManut) super.getEntity();
		if (entity == null) {
			entity = new XcpGrupoDestManut();
			entity.setTipDest(XcpGrupoDest.TIP_DEST_1_OPERADOR);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		//Busca a entidade PAI e se não for nova busca os detalhes
		XcpGrupoMensagemManut entityMestre = this.getMestreBacking().getEntity();
		if (!entityMestre.isNovo()) {
			MontaQuery q = new MontaQuery(XcpGrupoDestManut.class, "seqDest");
			q.addWhere("codGrupo", "=", entityMestre.getCodGrupo());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public Collection<SelectItem> getItensTipDest() {
		return this.getItens("tipDest", XcpGrupoDest.TIP_DEST_1_OPERADOR, XcpGrupoDest.TIP_DEST_2_SERVIDOR);
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo()) {
			//Seta a chave do PAI
			XcpGrupoMensagemManut entityMestre = this.getMestreBacking().getEntity();
			this.getEntity().setCodGrupo(entityMestre.getCodGrupo());
		}

		if (XcpGrupoDest.TIP_DEST_1_OPERADOR.equals(this.getEntity().getTipDest())) {
			this.getEntity().setMatriculaFk(null);
		} else {
			this.getEntity().setOperadorFk(null);
		}

		return super.gravar();
	}

}
