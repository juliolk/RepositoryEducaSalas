package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpObjetoParametro;
import com.adm.ejb.entity.XcpTipoCampo;
import com.adm.ejb.entity.extend.XcpObjetoManut;
import com.adm.ejb.entity.extend.XcpObjetoParametroManut;
import com.adm.util.MontaQuery;

@ManagedBean
@ViewScoped
public class XcpObjetoParametroBacking extends XcpManutencaoBacking<XcpObjetoBacking> implements Serializable {

	public XcpObjetoParametroBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpObjetoBacking.class);
	}

	@Override
	public XcpObjetoParametroManut getEntity() {
		XcpObjetoParametroManut entity = (XcpObjetoParametroManut) super.getEntity();
		if (entity == null) {
			entity = new XcpObjetoParametroManut();
			entity.setNumGrupo(0L);
			entity.setNumOrdem(0L);
			entity.setIndObrigatorio(0);
			entity.setTipParametro(XcpObjetoParametro.TIP_PARAMETRO_0_UNICO);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		//Busca a entidade PAI e se não for nova busca os detalhes
		XcpObjetoManut entityMestre = this.getMestreBacking().getEntity();
		if (!entityMestre.isNovo()) {
			MontaQuery q = new MontaQuery(XcpObjetoParametroManut.class, "numGrupo,numOrdem,desParametro");
			q.addWhere("codObjeto", "=", entityMestre.getCodObjeto());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public Collection<SelectItem> getItensTipParametro() {
		if (this.getModo() == MODO_FORM) {
			if (this.getEntity().getSeqParametroFk() != null) {
				if (XcpTipoCampo.TIP_PARAMETRO_4_TABELA_EXTERNA.equals(this.getEntity().getSeqParametroFk().getTipParametro())
						|| INT_1.equals(this.getEntity().getSeqParametroFk().getIndRespostas())) {
					//Se é LOV OU tem respostas : pode ser: UNICO e MULTIPLO					
					return this.getItens(
							"tipParametro",
							XcpObjetoParametro.TIP_PARAMETRO_0_UNICO,
							XcpObjetoParametro.TIP_PARAMETRO_2_MULTIPLOS,
							XcpObjetoParametro.TIP_PARAMETRO_3_MULTIPLOS_PRE_SELECAO);
				} else { //Senao pode ser: UNICO E INTERVALO
					return this.getItens("tipParametro", XcpObjetoParametro.TIP_PARAMETRO_0_UNICO, XcpObjetoParametro.TIP_PARAMETRO_1_INTERVALO);
				}
			} else {
				return this.getItens("tipParametro");
			}
		}
		return this.getItens(
				"tipParametro",
				XcpObjetoParametro.TIP_PARAMETRO_0_UNICO,
				XcpObjetoParametro.TIP_PARAMETRO_1_INTERVALO,
				XcpObjetoParametro.TIP_PARAMETRO_2_MULTIPLOS,
				XcpObjetoParametro.TIP_PARAMETRO_3_MULTIPLOS_PRE_SELECAO);
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo()) {
			//Seta a chave do PAI
			XcpObjetoManut entityMestre = this.getMestreBacking().getEntity();
			this.getEntity().setCodObjeto(entityMestre.getCodObjeto());
		}

		if (XcpTipoCampo.TIP_PARAMETRO_4_TABELA_EXTERNA.equals(this.getEntity().getSeqParametroFk().getTipParametro())) {
			this.getEntity().setDesMetodoEntidade(null);
		}

		boolean gravar = super.gravar();
		if (gravar) {
			this.getMestreBacking().validateObjeto();
		}
		return gravar;
	}

	@Override
	public boolean remover() throws Exception {
		MontaQuery delete = new MontaQuery("delete from XcpExecucaoResposta e");
		delete.addWhere("seqObjetoParametro", "=", this.getEntity().getSeqObjetoParametro());
		this.xcpPersistSession.executeQuery(this.getEjbVars(), delete);

		return super.remover();
	}

	public String getChaveGrupo(XcpObjetoParametroManut row) {
		return String.format("lbl_grupo_%s_%s", row.getCodObjeto(), row.getNumGrupo());
	}
}
