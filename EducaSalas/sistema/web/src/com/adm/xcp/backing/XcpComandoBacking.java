package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpComando;
import com.adm.ejb.entity.extend.XcpComandoManut;
import com.adm.ejb.timer.XcpComandoSession;
import com.adm.util.MontaQuery;
import com.adm.util.ejb.XcapeEntity;

@ManagedBean
@ViewScoped
public class XcpComandoBacking extends XcpManutencaoBacking implements Serializable {

	@EJB
	private XcpComandoSession session;

	public XcpComandoBacking() {
		this.setTipomManut(TIPO_MANUT.MESTRE);
		this.setDetalhesBacking(XcpComandoGruposBacking.class);
	}

	@Override
	public XcpComandoManut getEntity() {
		XcpComandoManut entity = (XcpComandoManut) super.getEntity();
		if (entity == null) {
			entity = new XcpComandoManut();
			entity.setTipAviso(XcpComando.TIP_AVISO_1_CAIXA);
			entity.setTipCorpo(XcpComando.TIP_CORPO_2_TEXTO);
			entity.setTipDestino(XcpComando.TIP_DESTINO_1_SISTEMA);
			entity.setTipTempo(XcpComando.TIP_TEMPO_1_DIARIAMENTE);
			entity.setIndMarcaLin(INT_0);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public void selecionaEntidade(XcapeEntity entity) throws Exception {

		if (entity != null) {
			if (!entity.isNovo()) {
				XcpComandoManut manut = (XcpComandoManut) entity;
				XcpComando prox = this.xcpQuerySession.find(this.getEjbVars(), XcpComando.class, manut.getSeqComando());
				manut.setDthProxExecucao(prox.getDthProxExecucao());
			}
		}
		super.selecionaEntidade(entity);
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpComandoManut.class, "desComando"));
	}

	public Collection<SelectItem> getItensTipTempo() {
		return this.getItens("tipTempo", XcpComando.TIP_TEMPO_1_DIARIAMENTE, XcpComando.TIP_TEMPO_2_CICLICO, XcpComando.TIP_TEMPO_3_DIAMES, XcpComando.TIP_TEMPO_4_MESANO);
	}

	public Collection<SelectItem> getItensTipAviso() {
		return this.getItens("tipAviso", XcpComando.TIP_AVISO_1_CAIXA, XcpComando.TIP_AVISO_2_EMAIL);
	}

	public Collection<SelectItem> getItensTipDestino() {
		return this.getItens("tipDestino", XcpComando.TIP_DESTINO_1_SISTEMA, XcpComando.TIP_DESTINO_2_PORTAL);
	}

	public Collection<SelectItem> getItensTipCorpo() {
		return this.getItens("tipCorpo", XcpComando.TIP_CORPO_1_PLANO, XcpComando.TIP_CORPO_2_TEXTO);
	}
	
	public Collection<SelectItem> getItensTipMes() {
		return this.getItens("tipMes",1,2,3,4,5,6,7,8,9,10,11,12);
	}

	@Override
	public boolean gravar() throws Exception {

		if (XcpComando.TIP_AVISO_1_CAIXA.equals(this.getEntity().getTipAviso())) {
			this.getEntity().setCodServerEmailFk(null);
		}

		if (XcpComando.TIP_CORPO_1_PLANO.equals(this.getEntity().getTipCorpo())) {
			this.getEntity().setSeqTextoCorpoFk(null);
		} else {
			this.getEntity().setDesCorpo(null);
		}

		if (XcpComando.TIP_TEMPO_1_DIARIAMENTE.equals(this.getEntity().getTipTempo())) {
			this.getEntity().setDia(null);
			this.getEntity().setMinutos(null);
		} else if (XcpComando.TIP_TEMPO_2_CICLICO.equals(this.getEntity().getTipTempo())) {
			this.getEntity().setDia(null);
			this.getEntity().setHora(null);
		} else if (XcpComando.TIP_TEMPO_3_DIAMES.equals(this.getEntity().getTipTempo())) {
			this.getEntity().setMinutos(null);
		} else if (XcpComando.TIP_TEMPO_4_MESANO.equals(this.getEntity().getTipTempo())) {
			this.getEntity().setMinutos(null);
		}

		if (XcpComando.TIP_DESTINO_2_PORTAL.equals(this.getEntity().getTipDestino())) {
			String desSql = this.getEntity().getDesSql();
			if (XcpComando.TIP_AVISO_2_EMAIL.equals(this.getEntity().getTipAviso())) {
				if (!desSql.contains(XcpComando.CAMPO_DES_EMAIL_DEST)) {
					addFacesMessage(this.getTraducao("msg_nec_des_email_dest"));
					return false;
				}
			} else {
				if (!desSql.contains(XcpComando.CAMPO_COD_OPER_DEST)) {
					addFacesMessage(this.getTraducao("msg_nec_cod_oper_dest"));
					return false;
				}
			}
		}

		if (super.gravar()) {
			this.session.updateTimer(this.getEjbVars(), this.getEntity().getSeqComando());
			this.pesquisar();
			this.actionPesquisar("XcpComando");
			this.setModo(MODO_LISTA);
			return true;
		}

		return false;
	}
	
	public void actionGravarEnviar() throws Exception {
		if (this.gravar()) {
			this.session.executarComando(this.getEjbVars(), this.getEntity().getSeqComando());			
		}
	}
}
