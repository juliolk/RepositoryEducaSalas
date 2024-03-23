package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.extend.XcpTipoCampoManut;
import com.adm.ejb.entity.extend.XcpTipoCampoValorManut;
import com.adm.ejb.util.ConstantesValidacao;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

@ManagedBean
@ViewScoped
public class XcpTipoCampoValorBacking extends XcpManutencaoBacking<XcpTipoCampoBacking> implements Serializable {

	private boolean isPermidido() {
		if (super.isClienteStatic()) {
			if (this.getMestreBacking().getEntity().getSeqParametro() != null) {
				if (this.getMestreBacking().getEntity().getSeqParametro().compareTo(Long.valueOf(ConstantesValidacao.NUMERO_PERMITIDO_CLIENTE_CAMPOS)) < 0) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isDisabledExcluir() {
		if (!this.getEntity().isNovo()) {
			if (!this.isPermidido()) {
				return true;
			}
		}
		return super.isDisabledExcluir();
	}

	@Override
	public boolean isDisabledNovo() {
		if (!this.isPermidido()) {
			return true;
		}
		return super.isDisabledNovo();
	}

	@Override
	public boolean isDisabledGravar() {
		if (!this.getEntity().isNovo()) {
			if (!this.isPermidido()) {
				return true;
			}
		}
		return super.isDisabledGravar();
	}

	public XcpTipoCampoValorBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpTipoCampoBacking.class);
	}

	@Override
	public XcpTipoCampoValorManut getEntity() {
		XcpTipoCampoValorManut entity = (XcpTipoCampoValorManut) super.getEntity();
		if (entity == null) {
			entity = new XcpTipoCampoValorManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		//Busca a entidade PAI e se não for uma nova busca os detalhes
		XcpTipoCampoManut xcpTipoCampo = this.getMestreBacking().getEntity();
		if (!xcpTipoCampo.isNovo()) {
			MontaQuery q = new MontaQuery(XcpTipoCampoValorManut.class, "seqLinha");
			q.addWhere("seqParametro", "=", xcpTipoCampo.getSeqParametro());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean gravar() throws Exception {
		if (!this.isPermidido()) {
			addFacesMessage(this.getTraducao("msg_em_cliente_sem_permissao_campo"));
			return false;
		}

		//Seta a chave do PAI
		this.getEntity().setSeqParametro(this.getMestreBacking().getEntity().getSeqParametro());
		if (this.getEntity().isNovo()) {
			MontaQuery qmax = new MontaQuery("select max(e.seqLinha) from XcpTipoCampoValor as e");
			qmax.addWhere("seqParametro", "=", this.getEntity().getSeqParametro());
			Long max = this.xcpQuerySession.executeQuerySingle(this.getEjbVars(), qmax);
			this.getEntity().setSeqLinha(Util.nvl(max, 0L) + 1L);
		}

		return super.gravar();
	}
}
