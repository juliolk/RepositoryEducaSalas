package com.adm.xcp.view.helper;

import java.util.ArrayList;
import java.util.List;

import com.adm.ejb.entity.Assinaturas;
import com.adm.ejb.entity.XcpExecucaoAgenda;
import com.adm.ejb.entity.XcpExecucaoSalva;
import com.adm.ejb.entity.XcpObjeto;
import com.adm.ejb.entity.XcpObjetoGrupos;
import com.adm.ejb.entity.custom.XcpObjetosPars;
import com.adm.ejb.entity.extend.XcpExecucaoAgendaManut;
import com.adm.ejb.vo.XcpExecObjGrupoVO;
import com.adm.ejb.vo.XcpExecObjVO;

public class XcpExecObjHelper {

	public static final String INCLUDE_CONTENT = "/secure/XcpExecObj/XcpExecObjContent.xhtml";
	public static final String INCLUDE_CONSULTA = "/secure/XcpExecObj/XcpExecObjConsulta.xhtml";

	private String includeAtual = INCLUDE_CONTENT;

	private XcpObjeto codObjeto;

	private XcpExecObjVO execObjVO;

	//Indicador para mostrar mensagem de sucesso depois q fechar a dialog. pois senao a mensagem aparece e some
	private boolean sucesso = false;
	//Indicador para mostrar o botao cancelar.. é falso qnd entrou pelo menu passando nome do objeto.
	private boolean mostrarCancelar = true;

	private boolean execInDialod = false;

	private XcpExecucaoSalva carregarConfig;

	private Integer tipSalvarConfig;
	private XcpExecucaoSalva salvarConfig;

	private boolean agendarAberto = false;
	private XcpExecucaoAgendaManut agenda;
	private List<String> diasSelecionados;
	private boolean agengaEnviarEmail;
	private String colunasLargura;

	private Long lastExecucao;
	private Assinaturas assinaturas;
	private Boolean usaAssinatura;
	
	private Long uploadSizeLimit;

	public String getIncludeAtual() {
		return this.includeAtual;
	}

	public void setIncludeAtual(String includeAtual) {
		this.includeAtual = includeAtual;
	}

	public XcpObjeto getCodObjeto() {
		return this.codObjeto;
	}

	public void setCodObjeto(XcpObjeto codObjeto) {
		this.codObjeto = codObjeto;
	}

	public XcpExecObjVO getExecObjVO() {
		return this.execObjVO;
	}

	public void setExecObjVO(XcpExecObjVO execObjVO) {
		this.execObjVO = execObjVO;
	}

	public boolean isExecInDialod() {
		return this.execInDialod;
	}

	public void setExecInDialod(boolean execInDialod) {
		this.execInDialod = execInDialod;
	}

	public boolean isSucesso() {
		return this.sucesso;
	}

	public void setSucesso(boolean sucesso) {
		this.sucesso = sucesso;
	}

	public boolean isMostrarCancelar() {
		return this.mostrarCancelar;
	}

	public void setMostrarCancelar(boolean mostrarCancelar) {
		this.mostrarCancelar = mostrarCancelar;
	}

	public XcpExecucaoSalva getSalvarConfig() {
		if (this.salvarConfig == null) {
			this.salvarConfig = new XcpExecucaoSalva();
		}
		return this.salvarConfig;
	}

	public void setSalvarConfig(XcpExecucaoSalva salvarConfig) {
		this.salvarConfig = salvarConfig;
	}

	public XcpExecucaoSalva getCarregarConfig() {
		return this.carregarConfig;
	}

	public void setCarregarConfig(XcpExecucaoSalva carregarConfig) {
		this.carregarConfig = carregarConfig;
	}

	public Integer getTipSalvarConfig() {
		return this.tipSalvarConfig;
	}

	public void setTipSalvarConfig(Integer tipSalvarConfig) {
		this.tipSalvarConfig = tipSalvarConfig;
	}

	public String getColunasLargura() {
		return this.colunasLargura;
	}

	public void setColunasLargura(String colunasLargura) {
		this.colunasLargura = colunasLargura;
	}

	public boolean isAgendarAberto() {
		return this.agendarAberto;
	}

	public void setAgendarAberto(boolean agendarAberto) {
		this.agendarAberto = agendarAberto;
	}

	public XcpExecucaoAgendaManut getAgenda() {
		if (this.agenda == null) {
			this.agenda = new XcpExecucaoAgendaManut();
			this.agenda.setIndSegunda(0);
			this.agenda.setIndTerca(0);
			this.agenda.setIndQuarta(0);
			this.agenda.setIndQuinta(0);
			this.agenda.setIndSexta(0);
			this.agenda.setIndSabado(0);
			this.agenda.setIndDomingo(0);
			this.agenda.setTipAgenda(XcpExecucaoAgenda.TIP_AGENDA_1_DIARIAMENTE);
		}
		return this.agenda;
	}

	public void setAgenda(XcpExecucaoAgendaManut agenda) {
		this.agenda = agenda;
	}

	public List<String> getDiasSelecionados() {
		if (this.diasSelecionados == null) {
			this.diasSelecionados = new ArrayList<String>(0);
		}
		return this.diasSelecionados;
	}

	public void setDiasSelecionados(List<String> diasSelecionados) {
		this.diasSelecionados = diasSelecionados;
	}

	public boolean isAgengaEnviarEmail() {
		return this.agengaEnviarEmail;
	}

	public void setAgengaEnviarEmail(boolean agengaEnviarEmail) {
		this.agengaEnviarEmail = agengaEnviarEmail;
	}

	public Boolean getGrupoCollapsed(XcpExecObjGrupoVO grupoVO) {
		// Aberto
		Boolean collapsed = false;

		// Tipo Sempre Aberto
		if (grupoVO.getTipInicializacao() == null || grupoVO.getTipInicializacao() == XcpObjetoGrupos.TIP_INICIALIZACAO_SEMPREABERTO) {
			// Aberto
			collapsed = false;
		}
		// Tipo Aberto se possuir valor
		// ou Tipo Sempre Fechado vindo de uma Execução Salva
		else if ((grupoVO.getTipInicializacao() == XcpObjetoGrupos.TIP_INICIALIZACAO_ABERTOSEPOSSUIRVALOR)
				|| (grupoVO.getTipInicializacao() == XcpObjetoGrupos.TIP_INICIALIZACAO_SEMPREFECHADO && this.carregarConfig != null)) {
			// Inicializa fechado
			collapsed = true;
			for (XcpObjetosPars param : grupoVO.getListaParametros()) {

				if (param.isMultiplo() || param.isMultiploPreSelecao()) {
					// Se for multiplo, abre se possuir itens na lista de valores multiplos
					collapsed = (param.getMultiValue().size() == 0);
					break;
				} else if (param.getTextValue() != null || param.getTextValueEnd() != null || param.getNumberValue() != null || param.getNumberValueEnd() != null
						|| param.getDateValue() != null || param.getDateValueEnd() != null || param.getLovValue() != null || param.getComboValue() != null) {
					// Abre se possuir algum valor preenchido
					collapsed = false;
					break;
				}

			}
		}
		// Tipo Sempre Fechado
		else if (grupoVO.getTipInicializacao() == XcpObjetoGrupos.TIP_INICIALIZACAO_SEMPREFECHADO) {
			// Fechado
			collapsed = true;

			// Para os grupos do tipo "sempre fechado", remove os valores salvos da ultima execução
			for (XcpObjetosPars param : grupoVO.getListaParametros()) {
				param.setMultiValue(null);
				param.setXcpExecObjItemMultLov(null);
				param.setTextValue(null);
				param.setTextValueEnd(null);
				param.setNumberValue(null);
				param.setNumberValueEnd(null);
				param.setDateValue(null);
				param.setDateValueEnd(null);
				param.setLovValue(null);
				param.setComboValue(null);
			}

		}

		return collapsed;
	}

	public Long getLastExecucao() {
		return this.lastExecucao;
	}

	public void setLastExecucao(Long lastExecucao) {
		this.lastExecucao = lastExecucao;
	}

	public Assinaturas getAssinaturas() {
		return this.assinaturas;
	}

	public void setAssinaturas(Assinaturas assinaturas) {
		this.assinaturas = assinaturas;
	}

	public Boolean getUsaAssinatura() {
		return this.usaAssinatura;
	}

	public void setUsaAssinatura(Boolean usaAssinatura) {
		this.usaAssinatura = usaAssinatura;
	}

	public Long getUploadSizeLimit() {
		return uploadSizeLimit;
	}

	public void setUploadSizeLimit(Long uploadSizeLimit) {
		this.uploadSizeLimit = uploadSizeLimit;
	}
	
	
}
