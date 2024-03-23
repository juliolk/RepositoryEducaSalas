package com.adm.xcp.backing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.Menus;
import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.XcpExecucao;
import com.adm.ejb.entity.XcpExecucaoAgenda;
import com.adm.ejb.entity.XcpExecucaoSalva;
import com.adm.ejb.entity.XcpObjeto;
import com.adm.ejb.entity.XcpObjetoModelo;
import com.adm.ejb.entity.XcpObjetoParametro;
import com.adm.ejb.entity.XcpTipoCampo;
import com.adm.ejb.entity.custom.XcpObjetosPars;
import com.adm.ejb.entity.extend.XcpExecucaoAgendaManut;
import com.adm.ejb.session.XcpAssinaturaDigitalSessionBean;
import com.adm.ejb.session.remote.XcpExecObjSession;
import com.adm.ejb.util.XcpExecObjUtil;
import com.adm.ejb.vo.XcpExecObjConsultaColunaVO;
import com.adm.ejb.vo.XcpExecObjGrupoVO;
import com.adm.ejb.vo.XcpExecObjInputVO;
import com.adm.ejb.vo.XcpExecObjVO;
import com.adm.util.Converter;
import com.adm.util.DBConnect;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.exceptions.MensagemException;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.event.XcpLovEvent;
import com.adm.xcp.view.helper.XcpExecObjHelper;
import com.xcp.creator.XcpCreatorItemComboIntf;

@ManagedBean
@ViewScoped
public class XcpExecObjBacking extends XcpAbstractBacking implements XcpEjbConstants {

	private static Logger log = Logger.getLogger(XcpExecObjBacking.class.getName());
	private Rotinas rotinas = null;
	private Long currentMatricula;

	@EJB
	private XcpAssinaturaDigitalSessionBean xcpAssinaturaDigitalSession;

	@Override
	public Rotinas getRotina() {
		if (this.rotinas == null) {
			return super.getRotina();
		}
		return this.rotinas;
	}

	@EJB
	private XcpExecObjSession xcpExecObjSession;

	private XcpExecObjHelper helper = new XcpExecObjHelper();

	private StreamedContent file;

	@PostConstruct
	public synchronized void postConstruct() {
		try {
			//Controlar a concorrencia para nao entrar em loop
			getBacking(XcpViewScopedBacking.class).setConstructXcpExecObjBacking(true);
			String codObjeto = Converter.toString(getExternalContext().getRequestParameterMap().get(PARAM_XCP_COD_OBJETO));
			if (codObjeto != null) {

				StringBuilder sb = new StringBuilder();
				sb.append("select e from Rotinas as e");
				sb.append(" where e.codObjeto = :codObjeto ");

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("codObjeto", codObjeto);
				List list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb.toString(), map);
				if (!Util.isEmpty(list)) {
					this.rotinas = (Rotinas) list.get(0);
				}

				XcpObjeto xcpObjeto = this.xcpQuerySession.find(this.getEjbVars(), XcpObjeto.class, codObjeto);
				if (xcpObjeto != null) {
					this.getHelper().setMostrarCancelar(false);
					this.carregar(xcpObjeto, null, null);

					if (INT_1.equals(xcpObjeto.getIndVincdoc())) {
						addFacesMessage(this.getTraducao("msg_vinculacao"), MSG_INFO);
					}

					try {
						Long sizeLimit = this.getParametroLong("Sistema", 20);
						this.getHelper().setUploadSizeLimit(sizeLimit != null ? sizeLimit : 10485760L);
					} catch (ParametroNaoEncontradoException e) {
						this.getHelper().setUploadSizeLimit(10485760L);
					}

				}

			}
		} finally {
			getBacking(XcpViewScopedBacking.class).setConstructXcpExecObjBacking(false);
		}
	}

	public boolean isRenderedCancelar() {
		return this.getHelper().isMostrarCancelar() && INT_1.equals(this.getHelper().getCodObjeto().getIndCancelar());
	}

	public XcpExecObjHelper getHelper() {
		return this.helper;
	}

	public String getLabelExecutar() {
		XcpObjeto xcpObjeto = this.getHelper().getExecObjVO().getXcpObjeto();
		if (xcpObjeto != null) {
			Integer indVincdoc = xcpObjeto.getIndVincdoc();
			if (INT_1.equals(indVincdoc)) {
				return this.getTraducao("btn_executar_vincular", this.getRotina().getRotina());
			}
		}
		return this.getTraducao("btn_executar", this.getRotina().getRotina());
	}

	public String getLabelExecutarPreview() {
		return this.getTraducao("btn_executar_preview", this.getRotina().getRotina());
	}

	public String getLabelLimpar() {
		return this.getTraducao("btn_limpar", this.getRotina().getRotina());
	}

	public String getLabelCancelar() {
		return this.getTraducao("btn_cancelar", this.getRotina().getRotina());
	}

	public String getLabelUpload() {
		return this.getTraducao("btn_upload", this.getRotina().getRotina());
	}

	public Collection<SelectItem> getItensTipFormato() {
		return this.getItensSemNull(
				"tipFormato",
				XcpExecucao.TIP_FORMATO_PDF,
				XcpExecucao.TIP_FORMATO_DOCX,
				XcpExecucao.TIP_FORMATO_XLSX,
				XcpExecucao.TIP_FORMATO_PPTX,
				XcpExecucao.TIP_FORMATO_TXT,
				XcpExecucao.TIP_FORMATO_RTF,
				XcpExecucao.TIP_FORMATO_ODT,
				XcpExecucao.TIP_FORMATO_ODS,
				XcpExecucao.TIP_FORMATO_HTML,
				XcpExecucao.TIP_FORMATO_CSV,
				XcpExecucao.TIP_FORMATO_XML);
	}

	public void selectCodObjeto(XcpLovEvent event) {
		this.carregar((XcpObjeto) event.getNewValue(), null, null);
		getRequestContext().update("panelHeaderContent");
	}

	public Map getParamsLovConfig() {
		Map pars = new HashMap();
		if (this.getHelper().getCodObjeto() != null) {
			pars.put("codObjeto", this.getHelper().getCodObjeto().getCodObjeto());
		} else {
			pars.put("codObjeto", null);
		}
		return pars;
	}

	public void selectCarregarConfig(XcpLovEvent event) {
		if (event.getNewValue() != null) {
			this.carregar(this.getHelper().getCodObjeto(), (XcpExecucaoSalva) event.getNewValue(), this.getHelper().getExecObjVO().getInput());
		}
	}

	private void carregar(XcpObjeto objeto, XcpExecucaoSalva carregarConfig, XcpExecObjInputVO input) {
		if (objeto == null) {
			this.getHelper().setExecObjVO(null);
		} else {
			try {
				this.getHelper().setCodObjeto(objeto);
				Long seqExecucao = null;
				if (carregarConfig != null) {
					seqExecucao = carregarConfig.getSeqExecucao();
				}
				this.getHelper().setExecObjVO(this.xcpExecObjSession.carregarXcpExecucao(this.getEjbVars(), objeto, input, seqExecucao));
			} catch (Exception e) {
				this.addFacesMessage(e);
			}
		}
	}

	public StreamedContent getFile() {
		return this.file;
	}

	public String getChaveGrupo(XcpExecObjGrupoVO row) {
		String codObjeto = null;
		if (this.getHelper().getCodObjeto() != null) {
			codObjeto = this.getHelper().getCodObjeto().getCodObjeto();
		}
		return String.format("lbl_grupo_%s_%s", codObjeto, row.getNumGrupo());
	}

	//ACOES

	public void actionLimpar() {
		//TODO não limpar os que vem de uma tela
		if (this.getHelper().getExecObjVO() != null) {
			this.getHelper().getExecObjVO().setTipFormato(null);
			this.getHelper().getExecObjVO().setSeqModelo(null);
			for (XcpObjetosPars par : this.getHelper().getExecObjVO().getListaParametros()) {
				par.setDateValue(null);
				par.setDateValueEnd(null);
				par.setTextValue(null);
				par.setTextValueEnd(null);
				par.setNumberValue(null);
				par.setNumberValueEnd(null);
				par.setLovValue(null);
				par.setMultiValue(null);
				par.setComboValue(null);

				if (INT_0.equals(par.isMultiploPreSelecao())) {
					par.setXcpExecObjItemMultLov(null);
				}
			}
		}
	}

	public void actionCancelar() {
		this.getHelper().setCodObjeto(null);
		this.getHelper().setExecObjVO(null);
		if (this.getHelper().isExecInDialod()) {
			getRequestContext().execute("PF('w_dlgXcpExecObj').hide();");
		} else {
			getRequestContext().update("focusDefault");
			getRequestContext().update("panelHeaderContent");
		}
	}

	public void actionClose(CloseEvent event) {
		if (this.getHelper().isSucesso()) {
			addFacesMessage(this.getTraducao("msg_relatorio_executado_sucesso"), MSG_INFO);
		}
		this.helper = new XcpExecObjHelper();
	}

	private void actionExecutar(boolean preview) throws Exception {
		XcpExecObjVO execObj = this.getHelper().getExecObjVO();

		if (execObj != null) {
			List<XcpObjetosPars> lista = execObj.getListaParametros();
			if (!Util.isEmpty(lista)) {
				for (XcpObjetosPars objPar : lista) {
					/*
					 * Solicitado por ariel dia 9/11/2017. Caso tenha recebido valor
					 * em branco, transforma para nulo para enviar ao relatorio
					 */
					if (XcpTipoCampo.TIP_PARAMETRO_3_TEXTO.equals(objPar.getTipParametro())) {
						if (XcpObjetoParametro.TIP_PARAMETRO_0_UNICO.equals(objPar.getTipParametroIntervalo())) {
							if (Util.isEmpty(objPar.getTextValue())) {
								objPar.setTextValue(null);
							}
						} else if (XcpObjetoParametro.TIP_PARAMETRO_1_INTERVALO.equals(objPar.getTipParametroIntervalo())) {
							if (Util.isEmpty(objPar.getTextValue())) {
								objPar.setTextValue(null);
							}
							if (Util.isEmpty(objPar.getTextValueEnd())) {
								objPar.setTextValueEnd(null);
							}
						}
					}

					if (objPar.isRequired()) {
						if (objPar.isMultiplo() || objPar.isMultiploPreSelecao()) {
							List<XcpCreatorItemComboIntf> listValue = objPar.getMultiValue();
							if (Util.isEmpty(listValue)) {
								addFacesMessage(this.getTraducao("msg_campo_obrigatorio", objPar.getLabel()));
								return;
							}
						}
					}
				}
			}

			execObj.setPreview(preview);

			if (execObj.getXcpObjeto() != null) {

				log.debug("executando objeto: " + execObj.getXcpObjeto().getCodObjeto());

				execObj.setDesArquivo(execObj.getXcpObjeto().getDesNomeArqDownload());

				//Executa o objeto
				execObj = this.xcpExecObjSession.executar(this.getEjbVars(), execObj);

				this.getHelper().setExecObjVO(execObj);

				if (XcpObjeto.TIP_OBJETO_1_RELATORIO.equals(execObj.getXcpObjeto().getTipObjeto()) || execObj.isForceReport()) {
					//Se é relatorio carrega o arquivo para downlaod
					if (!execObj.isIgnoreReport()) {
						if (!execObj.isReportFound()) {
							//Execução realizada com sucesso. Sem relatório para download
							addFacesMessage(this.getTraducao("msg_proc_exec_sucesso_semrelatorio"), MSG_INFO);
						} else {
							this.downloadRelatorio(execObj);
							if (preview) {
								Path p = Paths.get(PATH_TEMPORARY_FILES).resolve(execObj.getXcpExecucao().getSeqExecucao() + ".pdf");
								Files.copy(this.getFile().getStream(), p, StandardCopyOption.REPLACE_EXISTING);
								this.getHelper().setLastExecucao(execObj.getXcpExecucao().getSeqExecucao());
								getRequestContext().execute("PF('wDlgXcpExecPreview').show();");
							} else {
								getRequestContext().execute("PF('wBtnDownloadRelatorioGlobal').getJQ().click();");
							}
						}

					} else {
						addFacesMessage(this.getTraducao("msg_processo_executado_sucesso"), MSG_INFO);
					}
				} else if (XcpObjeto.TIP_OBJETO_2_CONSULTA.equals(execObj.getXcpObjeto().getTipObjeto())) {
					//Se é consulta troca para a tela da listag7em
					this.getHelper().setIncludeAtual(XcpExecObjHelper.INCLUDE_CONSULTA);
					getRequestContext().update("form");
				} else if (Util.in(execObj.getXcpObjeto().getTipObjeto(), XcpObjeto.TIP_OBJETO_3_PROCESSO, XcpObjeto.TIP_OBJETO_4_ARQ_SQL)) {
					//Se é processo mostra uma mensagem de sucesso
					if (INT_1.equals(execObj.getXcpObjeto().getIndDownload())) {
						//Se tem download
						this.downloadObjProcessado(execObj);
						getRequestContext().execute("PF('wBtnDownloadRelatorioGlobal').getJQ().click();");
					} else {
						addFacesMessage(this.getTraducao("msg_processo_executado_sucesso"), MSG_INFO);
					}
				}
			}
		}
	}

	public void actionExecutar(ActionEvent event) throws Exception {
		boolean preview = false;
		if (event != null) {
			Object valor = event.getComponent().getAttributes().get("preview");
			if (valor != null) {
				preview = valor.equals("true");
			}
		}
		this.actionExecutar(preview);
	}

	public boolean isDisabledBtnExecutar() {
		return this.getHelper().getTipSalvarConfig() != null || this.getHelper().isAgendarAberto();
	}

	//RELATORIO

	public void actionFinalRelatorio() {
		if (this.getHelper().isExecInDialod()) {
			getRequestContext().execute("PF('w_dlgXcpExecObj').hide();");
			this.getHelper().setSucesso(true);
		} else {
			addFacesMessage(this.getTraducao("msg_relatorio_executado_sucesso"), MSG_INFO);
			//Recarrega
			this.carregar(this.getHelper().getCodObjeto(), null, null);
			getRequestContext().update("focusDefault");
		}
	}

	//CONSULTA

	public void actionExportarConsulta(String tipFormato) throws JRException, DRException, SQLException, FileNotFoundException, IOException {
		try {
			XcpExecObjVO execObj = this.getHelper().getExecObjVO();

			execObj.setTipFormato(tipFormato);
			execObj.setColunasLarguras(new ArrayList<Integer>());
			if (!Util.isEmpty(this.getHelper().getColunasLargura())) {
				String[] widths = this.getHelper().getColunasLargura().split(",");

				for (String w : widths) {
					if (!Util.isEmpty(w)) {
						if (w.contains(".")) {
							w = w.substring(0, w.indexOf("."));
						}

						execObj.getColunasLarguras().add(Converter.toInteger(w));
					}
				}
			}

			execObj = this.xcpExecObjSession.exportarConsulta(this.getEjbVars(), execObj);

			this.getHelper().setExecObjVO(execObj);

			this.downloadRelatorio(execObj);

			getRequestContext().execute("PF('wBtnDownloadConsulta').getJQ().click();");

		} catch (Exception e) {
			this.addFacesMessage(e);
		}

	}

	public void actionVoltarConsulta() {
		this.getHelper().setIncludeAtual(XcpExecObjHelper.INCLUDE_CONTENT);
	}

	public void onTabConsultaColumnReorder(AjaxBehaviorEvent event) {
		DataTable table = (DataTable) event.getSource();

		this.getHelper().getExecObjVO().getColunasOrdem().clear();
		for (org.primefaces.component.api.UIColumn column : table.getColumns()) {
			String columnKey = column.getColumnKey();
			this.getHelper().getExecObjVO().getColunasOrdem().add(Converter.toInteger(columnKey.substring(columnKey.lastIndexOf(":") + 1)));
		}
	}

	public void onTabConsultaColumnSelect(AjaxBehaviorEvent event) {
		//Limpa a ordenação e o filtro para não ficar com os dados errados
		DataTable table = (DataTable) this.findComponent("tabConsulta");
		if (table != null) {
			table.reset();
			table.setValueExpression("sortBy", null);
		}
		this.getHelper().getExecObjVO().setDadosConsultaFiltrados(null);
	}

	public String getFileName() {
		XcpExecObjVO execObj = this.getHelper().getExecObjVO();
		return XcpExecObjUtil.getOutputFileName(execObj.getXcpExecucao());
	}

	private void downloadRelatorio(XcpExecObjVO execObj) throws Exception {
		this.file = null;
		//Busca do banco para fazer download
		if (execObj.getArquivoGerado() != null) {
			String fileName = XcpExecObjUtil.getOutputFileName(execObj.getXcpExecucao());
			//this.file = new DefaultStreamedContent(execObj.getArquivoGerado(), Util.toMimeType(fileName), fileName);
			this.file = new DefaultStreamedContent(new FileInputStream(execObj.getArquivoGerado()), Util.toMimeType(fileName), fileName);
			return;
		}
		try (Connection conn = DBConnect.getConnection(this.getEjbVars());
				PreparedStatement pstmt = conn.prepareStatement("SELECT obj_relatorio from XCP_EXECUCAO where seq_execucao = ?");) {
			pstmt.setLong(1, execObj.getXcpExecucao().getSeqExecucao());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Blob blob = rs.getBlob("obj_relatorio");
					if (blob != null) {
						String fileName = XcpExecObjUtil.getOutputFileName(execObj.getXcpExecucao());
						this.file = new DefaultStreamedContent(blob.getBinaryStream(), Util.toMimeType(fileName), fileName);
					}
				}
			}
		}
	}

	private void downloadObjProcessado(XcpExecObjVO execObj) throws Exception {
		this.file = null;

		//Busca do banco para fazer download
		try (Connection conn = DBConnect.getConnection(this.getEjbVars());
				PreparedStatement pstmt = conn.prepareStatement("SELECT tip_formato, obj_processado, obj_relatorio from XCP_EXECUCAO where seq_execucao = ?");) {
			pstmt.setLong(1, execObj.getXcpExecucao().getSeqExecucao());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String tip = rs.getString("tip_formato");
					execObj.getXcpExecucao().setTipFormato(tip);
					if ("zip".equalsIgnoreCase(tip)) {
						Blob blob = rs.getBlob("obj_relatorio");
						if (blob != null) {
							this.file = new DefaultStreamedContent(
									blob.getBinaryStream(),
									"application/x-download",
									XcpExecObjUtil.getOutputFileNameZip(execObj.getXcpExecucao()));
						}
					} else {
						Clob clob = rs.getClob("obj_processado");
						if (clob != null) {
							this.file = new DefaultStreamedContent(
									clob.getAsciiStream(),
									"application/x-download",
									XcpExecObjUtil.getOutputFileName(execObj.getXcpExecucao()));
						}
					}
				}
			}
		}
	}

	//SALVAR CONFIGURACAO

	public Collection<SelectItem> getItensTipSalvarConfig() {
		return this.getItensSemNull("tipSalvarConfig", 0, 1);
	}

	public void toggleSalvarConfig(ToggleEvent event) {
		if (event.getVisibility() == Visibility.HIDDEN) {
			this.getHelper().setTipSalvarConfig(null);
			this.getHelper().setSalvarConfig(null);
		}
	}

	public void changeTipSalvarConfig(AjaxBehaviorEvent event) {
		this.getHelper().setSalvarConfig(null);

		getRequestContext().reset(event.getComponent().findComponent("lovSalvarConfig").getClientId());
		getRequestContext().reset(event.getComponent().findComponent("txtCodConfiguracao").getClientId());
		getRequestContext().reset(event.getComponent().findComponent("txtDesConfiguracao").getClientId());
	}

	public void actionSalvar(ActionEvent event) throws Exception {
		XcpExecObjVO execObj = this.getHelper().getExecObjVO();
		if (execObj != null && execObj.getXcpObjeto() != null && this.getHelper().getSalvarConfig() != null) {

			if (this.getHelper().getSalvarConfig().isNovo()) {
				this.getHelper().getSalvarConfig().setEmpresa(this.getCodEmpresa());
				this.getHelper().getSalvarConfig().setOperador(this.getSession().getUsuario().getOperador());
			}

			//Grava a execucao e prepara os parametros de execucao
			execObj = this.xcpExecObjSession.salvarConfig(this.getEjbVars(), execObj, this.getHelper().getSalvarConfig());

			this.getHelper().setTipSalvarConfig(null);
			this.getHelper().setSalvarConfig(null);

			addFacesMessage(this.getTraducao("msg_configuracao_salva"), MSG_INFO);
		}
	}

	//AGENDAR
	public void toggleAgendarExecucao(ToggleEvent event) {
		this.getHelper().setAgendarAberto(event.getVisibility() == Visibility.VISIBLE);
		//Limpa
		this.getHelper().setAgenda(null);
		this.getHelper().setAgengaEnviarEmail(false);
	}

	public void actionAgendar(ActionEvent event) throws Exception {
		XcpExecucaoAgendaManut agenda = this.getHelper().getAgenda();
		agenda.setIndSegundos(this.getHelper().getCodObjeto().getIndSegundos());
		agenda.setDesGrupoAgenda(this.getHelper().getCodObjeto().getDesGrupoAgenda());

		if (XcpExecucaoAgenda.TIP_AGENDA_2_DIA_SEMANA.equals(agenda.getTipAgenda())) {
			List<String> dias = this.getHelper().getDiasSelecionados();
			agenda.setIndSegunda(dias.contains("1") ? 1 : 0);
			agenda.setIndTerca(dias.contains("2") ? 1 : 0);
			agenda.setIndQuarta(dias.contains("3") ? 1 : 0);
			agenda.setIndQuinta(dias.contains("4") ? 1 : 0);
			agenda.setIndSexta(dias.contains("5") ? 1 : 0);
			agenda.setIndSabado(dias.contains("6") ? 1 : 0);
			agenda.setIndDomingo(dias.contains("7") ? 1 : 0);
		} else if (XcpExecucaoAgenda.TIP_AGENDA_5_EXECUCAO_UNICA.equals(agenda.getTipAgenda())) {
			if (agenda.getHraExecucao() == null || agenda.getHraExecucao().before(new Date())) {
				addFacesMessage(this.getTraducao("msg_data_execucao_maior_atual"), MSG_ERROR);
				return;
			}
		}

		XcpExecObjVO execObj = this.getHelper().getExecObjVO();
		if (execObj != null && execObj.getXcpObjeto() != null) {
			/*
			 * Testa se tem algum agendamento ciclico. Se existe cancela o
			 * agendamento para evitar que processos se sobreponham e criem
			 * problemas de concorrencia
			 */
			StringBuilder sb = new StringBuilder();
			sb.append(" Select exe.cod_objeto from xcp_execucao_agenda ag, xcp_execucao exe ");
			sb.append(" 	where ag.seq_execucao = exe.seq_execucao ");
			sb.append(" 	and ag.dth_inatividade is null ");
			sb.append("    and exe.cod_objeto like :codobjeto ");
			sb.append("    and ag.tip_agenda = :tipagenda ");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("codobjeto", execObj.getXcpObjeto().getCodObjeto());
			map.put("tipagenda", XcpExecucaoAgenda.TIP_AGENDA_4_CICLICO);

			if (this.xcpQuerySession.existeNative(this.getEjbVars(), sb.toString(), map)) {
				addFacesMessage(this.getTraducao("msg_agenda_ciclico"));
				return;
			}

			//Grava a execucao e prepara os parametros de execucao
			execObj = this.xcpExecObjSession.agendar(this.getEjbVars(), execObj, agenda);
			addFacesMessage(this.getTraducao("msg_execucao_agendada"), MSG_INFO);

			this.getHelper().setAgenda(null);
			this.getHelper().setDiasSelecionados(null);
			this.getHelper().setAgengaEnviarEmail(false);
		}

	}

	public Collection<SelectItem> getItensTipAgenda() {
		return this.getItensSemNull(
				"tipAgenda",
				XcpExecucaoAgenda.TIP_AGENDA_1_DIARIAMENTE,
				XcpExecucaoAgenda.TIP_AGENDA_2_DIA_SEMANA,
				XcpExecucaoAgenda.TIP_AGENDA_3_DIA_MES,
				XcpExecucaoAgenda.TIP_AGENDA_4_CICLICO,
				XcpExecucaoAgenda.TIP_AGENDA_5_EXECUCAO_UNICA);
	}

	public Collection<SelectItem> getItensDiasSemana() {
		return this.getItensSemNull("diasSemana", "1", "2", "3", "4", "5", "6", "7");
	}

	//UPLOAD
	public void actionUpload(FileUploadEvent event) {
		try {

			File tempFile = File.createTempFile("xcp_exec_upload_", "_" + event.getFile().getFileName(), new File(PATH_TEMPORARY_FILES));

			try (InputStream in = event.getFile().getInputstream()) {
				Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			this.getHelper().getExecObjVO().setUploadFile(tempFile);
			this.getHelper().getExecObjVO().setUploadName(event.getFile().getFileName());

			//TODO Cadastrar mensagem "Upload realizado com sucesso"
			addFacesMessage(this.getTraducao("msg_upload_sucesso"), MSG_INFO);

			getRequestContext().execute("PF('wDlgXcpExecUpload').hide();");

		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	// EXEUCAO A PARTIR DE UMA TELA

	public static XcpExecucao executar(XcpExecObjInputVO input) throws MensagemException {
		XcpExecObjBacking backing = getBacking(XcpExecObjBacking.class);

		XcpObjeto xcpObjeto = backing.xcpQuerySession.find(backing.getEjbVars(), XcpObjeto.class, input.getCodObjeto());
		XcpExecObjVO execObjVO = null;
		if (xcpObjeto != null) {
			//backing.getHelper().setInput(input);
			backing.carregar(xcpObjeto, null, input);

			MontaQuery q = new MontaQuery("select count(1) from XcpObjetoModelo e");
			q.addWhere("codObjeto", "=", xcpObjeto.getCodObjeto());
			q.addWhere("tipo", "=", XcpObjetoModelo.TIP_MODELO_1_PRINCIPAL);
			Integer countMod = Converter.toInteger(backing.xcpQuerySession.executeQuerySingle(backing.getEjbVars(), q));

			log.info("countMod: " + countMod);

			q = new MontaQuery("select count(1) from XcpObjetoParametro e");
			q.addWhere("codObjeto", "=", xcpObjeto.getCodObjeto());
			Integer countPar = Converter.toInteger(backing.xcpQuerySession.executeQuerySingle(backing.getEjbVars(), q));

			log.info("countPar: " + countPar);

			//Se nao tem filstros, parametros e se nao permite agendamento
			if ((Util.in(countMod, INT_0, INT_1) && INT_0.equals(countPar) && INT_0.equals(xcpObjeto.getIndAgendamento())) || input.isForceRegister()) {
				try {
					backing.actionExecutar(null);
					execObjVO = backing.getHelper().getExecObjVO();
					if (execObjVO != null) {
						return execObjVO.getXcpExecucao();
					} else {
						log.info("Nao executou... ");
						return null;
					}

				} catch (Exception e) {
					throw new MensagemException(e);
				}
			}

			backing.getHelper().setExecInDialod(true);

			RequestContext requestContext = getRequestContext();
			requestContext.update("formXcpExecObj");

			if (backing.getHelper().getExecObjVO().isTodosOcultos() || input.isForceRegister()) {
				requestContext.execute("$('#formXcpExecObj\\\\:btnExecutar').click();");
			} else {

				requestContext.execute("PF('w_dlgXcpExecObj').content.css('max-height', $(window).height() * 0.8 +'px'); PF('w_dlgXcpExecObj').show();");
			}
		} else {
			throw new MensagemException("msg_objeto_nao_encontrado", input.getCodObjeto());
		}

		log.info("Nao achou o objeto... ");
		return null;
	}

	public void actionOpenDialogAssinar() {
		this.getHelper().setAssinaturas(null);
		getRequestContext().execute("PF('wDlgXcpExecAssinatura').show()");
	}

	public void actionAssinar() throws Exception {
		this.xcpAssinaturaDigitalSession.solicitarAssinatura(this.getEjbVars(), this.getHelper().getAssinaturas(), this.getHelper().getLastExecucao());
		//TODO Enviado a fila de assinatura
		addFacesMessage(this.getTraducao("msg_envia_fila_assinatura"), MSG_INFO);
		getRequestContext().execute("PF('wDlgXcpExecAssinatura').hide()");
	}

	public boolean isRenderedAssinatura() {
		if (this.getHelper().getUsaAssinatura() == null) {
			try {
				this.getHelper().setUsaAssinatura(LONG_1.equals(this.getParametroGlobalLong("Assinatura", 1)));
			} catch (ParametroNaoEncontradoException e) {
				this.getHelper().setUsaAssinatura(false);
			}
		}
		return this.getHelper().getUsaAssinatura();
	}

	public boolean isRenderedVisualizar() {

		if (Menus.PERMISSAO_2_PORTAL.equals(this.getTipoAcesso())) {
			return false;
		}

		XcpExecObjVO execObj = this.getHelper().getExecObjVO();
		if (execObj != null) {
			return XcpObjeto.TIP_OBJETO_1_RELATORIO.equals(execObj.getXcpObjeto().getTipObjeto()) || execObj.isForceReport();
		}
		return false;
	}

	public Map<String, Object> getParamsTipoDoc() {
		XcpObjeto xcpObjeto = this.getHelper().getExecObjVO().getXcpObjeto();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("utilizacao", xcpObjeto.getTipUtilizacao());
		return map;
	}

	//x linhas atendem 
	public String getDesMsgFooter() {
		XcpExecObjVO execObjVO = this.getHelper().getExecObjVO();

		if (Util.isEmpty(execObjVO.getDadosConsulta())) {
			return null;
		}

		Integer qtd = null;
		if (execObjVO.getDadosConsultaFiltrados() == null) {
			qtd = execObjVO.getDadosConsulta().size();
		} else {
			qtd = execObjVO.getDadosConsultaFiltrados().size();
		}
		return this.getTraducao("msg_footer_table", qtd + "");
	}

	public String getDesPathFoto() {

		StringBuilder sb = new StringBuilder();
		sb.append("empresa=");
		sb.append(this.getCodEmpresa());
		sb.append("|matricula=");
		sb.append(this.getCurrentMatricula());
		sb.append("|tipdocumento=");
		sb.append(1);

		return "/docpes?pars=" + Converter.toHex(sb.toString());
	}

	public boolean isRenderedImage(XcpExecObjConsultaColunaVO col) {
		return col.getLabel().equals("Matrícula_");
	}

	public void actionMouseOver(AjaxBehaviorEvent event) {
		this.setCurrentMatricula(Converter.toLong(event.getComponent().getAttributes().get("matricula")));
	}

	public Long getCurrentMatricula() {
		return this.currentMatricula;
	}

	public void setCurrentMatricula(Long currentMatricula) {
		this.currentMatricula = currentMatricula;
	}
}