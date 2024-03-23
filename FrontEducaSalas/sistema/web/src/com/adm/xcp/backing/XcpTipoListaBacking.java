package com.adm.xcp.backing;

import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpTipoLista;
import com.adm.ejb.entity.extend.XcpTipoListaManut;
import com.adm.ejb.util.ConstantesValidacao;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpTipoListaBacking extends XcpManutencaoBacking {

	private boolean isPermidido() {
		if (super.isClienteStatic()) {
			if (this.getEntity().getCodLov() != null) {
				if (!this.getEntity().getCodLov().matches(ConstantesValidacao.OJBETO_VALIDO_CLIENTE(super.getCodClienteStatic()))) {
					return false;
				}
			}
		}
		return true;
	}

	private String montaNomePadrao() {
		if (super.isClienteStatic()) {
			StringBuffer sb = new StringBuffer();
			sb.append("CLI_");
			sb.append(super.getCodClienteStatic());
			sb.append("_");
			return sb.toString();
		}
		return null;
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

	@Override
	public XcpTipoListaManut getEntity() {
		XcpTipoListaManut entity = (XcpTipoListaManut) super.getEntity();
		if (entity == null) {
			entity = new XcpTipoListaManut();
			entity.setCodLov(this.montaNomePadrao());
			this.setEntity(entity);
		}
		return entity;
	}

	public void setEntity(XcpTipoLista entity) {
		super.setEntity(entity);
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpTipoListaManut.class, "des_lov"));
	}

	public Collection<SelectItem> getItensTipCodigo() {
		return this.getItens(
				"tipCodigo",
				XcpTipoLista.TIP_CODIGO_0_LONG,
				XcpTipoLista.TIP_CODIGO_1_STRING,
				XcpTipoLista.TIP_CODIGO_2_BIGDECIMAL,
				XcpTipoLista.TIP_CODIGO_3_INTEGER,
				XcpTipoLista.TIP_CODIGO_4_DATE);
	}

	@Override
	public boolean gravar() throws Exception {
		if (!this.isPermidido()) {
			addFacesMessage(this.getTraducao("msg_nome_invalido_lov"));
			return false;
		}
		return super.gravar();
	}
}
