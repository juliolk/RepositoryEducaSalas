package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.extend.XcpTabelaColunaManut;
import com.adm.ejb.entity.extend.XcpTabelaManut;
import com.adm.gui.backing.XcpTabelaBacking;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpTabelaColunaBacking extends XcpManutencaoBacking<XcpTabelaBacking> implements Serializable {

	public XcpTabelaColunaBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpTabelaBacking.class);
	}

	@Override
	public XcpTabelaColunaManut getEntity() {
		XcpTabelaColunaManut entity = (XcpTabelaColunaManut) super.getEntity();
		if (entity == null) {
			entity = new XcpTabelaColunaManut();
			entity.setIndObrigatorio(INT_0);
			entity.setIndDisponivelRecadastramento(INT_0);
			entity.setIndSolicitaAnexo(INT_0);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		XcpTabelaManut entityMestre = this.getMestreBacking().getEntity();
		if (!entityMestre.isNovo()) {
			MontaQuery q = new MontaQuery(XcpTabelaColunaManut.class, "desTabela");
			q.addWhere("desTabela", "=", entityMestre.getCodTabela());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo()) {
			//Seta a chave do PAI
			XcpTabelaManut entityMestre = this.getMestreBacking().getEntity();
			this.getEntity().setDesTabela(entityMestre.getCodTabela());
		}
		
		if (this.getEntity().getIndDisponivelRecadastramento() == null ||
				this.getEntity().getIndDisponivelRecadastramento().equals(INT_0)) {
			this.getEntity().setIndSolicitaAnexo(INT_0);
		}

		return super.gravar();
	}
}
