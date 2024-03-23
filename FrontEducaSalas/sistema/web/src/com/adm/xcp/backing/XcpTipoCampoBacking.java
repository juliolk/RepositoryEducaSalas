package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpTipoCampo;
import com.adm.ejb.entity.extend.XcpTipoCampoManut;
import com.adm.ejb.util.ConstantesValidacao;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

@ManagedBean
@ViewScoped
public class XcpTipoCampoBacking extends XcpManutencaoBacking implements Serializable {

	private boolean isPermidido() {
		if (super.isClienteStatic()) {
			if (this.getEntity().getSeqParametro() != null) {
				if (this.getEntity().getSeqParametro().compareTo(Long.valueOf(ConstantesValidacao.NUMERO_PERMITIDO_CLIENTE_CAMPOS)) < 0) {
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
	public boolean isDisabledGravar() {
		if (!this.getEntity().isNovo()) {
			if (!this.isPermidido()) {
				return true;
			}
		}
		return super.isDisabledGravar();
	}

	public XcpTipoCampoBacking() {
		this.setTipomManut(TIPO_MANUT.MESTRE);

		this.setDetalhesBacking(XcpTipoCampoValorBacking.class);
	}

	@Override
	public XcpTipoCampoManut getEntity() {
		XcpTipoCampoManut entity = (XcpTipoCampoManut) super.getEntity();
		if (entity == null) {
			entity = new XcpTipoCampoManut();
			entity.setTipParametro(XcpTipoCampo.TIP_PARAMETRO_0_NUMERICO);
			entity.setIndRespostas(0);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpTipoCampoManut.class, "desTipoCampo"));
	}

	public Collection<SelectItem> getItensTipParametro() {
		return this.getItens(
				"tipParametro",
				XcpTipoCampo.TIP_PARAMETRO_0_NUMERICO,
				XcpTipoCampo.TIP_PARAMETRO_1_VALOR,
				XcpTipoCampo.TIP_PARAMETRO_2_DATA,
				XcpTipoCampo.TIP_PARAMETRO_3_TEXTO,
				XcpTipoCampo.TIP_PARAMETRO_4_TABELA_EXTERNA,
				XcpTipoCampo.TIP_PARAMETRO_5_SECRET);
	}

	@Override
	public boolean gravar() throws Exception {
		if (!this.isPermidido()) {
			addFacesMessage(this.getTraducao("msg_em_cliente_sem_permissao_campo"));
			return false;
		}

		if (!XcpTipoCampo.TIP_PARAMETRO_1_VALOR.equals(this.getEntity().getTipParametro())) {
			this.getEntity().setNumDecimais(0);
		}
		if (XcpTipoCampo.TIP_PARAMETRO_4_TABELA_EXTERNA.equals(this.getEntity().getTipParametro())) {
			if (!Util.isEmpty(this.getEntity().getDesLovClass())) {
				try {
					Class.forName(this.getEntity().getDesLovClass());
				} catch (ClassNotFoundException e) {
					addFacesMessage(this.getTraducao("msg_classe_inexistente"));
					return false;
				}
			}

			this.getEntity().setIndRespostas(0);
		}

		boolean gravar = super.gravar();
		if (gravar) {
			if (INT_0.equals(this.getEntity().getIndRespostas())) {
				MontaQuery q = new MontaQuery("delete from XcpTipoCampoValor e");
				q.addWhere("seqParametro", "=", this.getEntity().getSeqParametro());
				this.xcpPersistSession.executeQuery(this.getEjbVars(), q);
				//Recarrega as respostas
				this.carregarDetalhes();
			}
		}
		return gravar;
	}
}
