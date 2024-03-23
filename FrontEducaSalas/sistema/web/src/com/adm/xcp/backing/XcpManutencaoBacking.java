package com.adm.xcp.backing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import org.primefaces.component.datatable.DataTable;

import com.adm.ejb.entity.EsocEvento;
import com.adm.ejb.entity.EsocFeriasEventos;
import com.adm.ejb.entity.EsocFuncEventos;
import com.adm.ejb.entity.EsocMovEventos;
import com.adm.ejb.entity.extend.AcidentestrabManut;
import com.adm.ejb.entity.extend.EsocFeriasEventosManut;
import com.adm.ejb.entity.extend.EsocFuncEventosManut;
import com.adm.ejb.entity.extend.EsocMovEventosManut;
import com.adm.ejb.entity.extend.EsocTipoManut;
import com.adm.ejb.entity.extend.FeriasManut;
import com.adm.ejb.entity.extend.FuncionariosManut;
import com.adm.ejb.entity.extend.MovimentacoesManut;
import com.adm.ejb.session.local.EsocialSession;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpTabelaColunaSession;
import com.adm.ejb.view.AcessofuncView;
import com.adm.ejb.vo.EsocEventosVO;
import com.adm.ejb.vo.XcpExecObjInputVO;
import com.adm.ejb.vo.XcpTabelaColunaVO;
import com.adm.ejb.vo.XcpTabelaVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;

public abstract class XcpManutencaoBacking<MESTRE_BACK extends XcpManutencaoBacking> extends XcpAbstractBacking {

	public static enum TIPO_MANUT {
		NORMAL,
		MESTRE,
		DETALHE,
		WIZARD
	}

	public static final int MODO_FORM = 1;
	public static final int MODO_LISTA = 2;
	public static final int MODO_WIZARD = 3;
	public static final int MODO_CUSTOM = 4;

	private Integer modo;
	private TIPO_MANUT tipomManut;
	private boolean pesquisaNoVoltar = false;
	private Class<MESTRE_BACK> mestreBackingClass;
	private Class<? extends XcpManutencaoBacking>[] detalhesBacking;
	private XcpTabelaColunaVO xcpTabelaColunaVO = null;

	/**
	 * Indica se terá ou não coluna complementar
	 */
	private boolean complemento = true;
	/**
	 * Indica se as colunas complementares serão exibidas de forma customizada
	 * ou de forma padrão como definido nos xcpPanelManut e xcpPanelManutLazy
	 */
	private boolean complementoCustomizado = false;

	private XcapeEntity entity;
	protected List lista;
	private List listaFiltrados;
	private List<EsocEventosVO> listEvtEsocial;

	@EJB
	protected XcpPersistSession xcpPersistSession;

	@EJB
	protected OperadoresSession operadoresSession;

	@EJB
	protected XcpTabelaColunaSession xcpTabelaColunaSession;

	@EJB
	private EsocialSession esocialSession;

	public XcpManutencaoBacking() {
		this.setTipomManut(TIPO_MANUT.NORMAL);
	}

	//GETS CONSTANTES
	public int getModoForm() {
		return MODO_FORM;
	}

	public int getModoLista() {
		return MODO_LISTA;
	}

	public int getModoZizard() {
		return MODO_WIZARD;
	}

	public int getModoCustom() {
		return MODO_CUSTOM;
	}

	//GETS / SETS

	//CONTROLE TELA
	public Integer getModo() {
		if (this.modo == null) {
			return this.getModoDefault();
		} else {
			return this.modo;
		}
	}

	protected Integer getModoDefault() {
		return MODO_LISTA;
	}

	public void setModo(Integer modo) {
		this.modo = modo;
	}

	public boolean isDetailsEdit() {
		if (this.getDetalhesBacking() != null) {
			if (!Util.isEmpty(this.getDetalhesBacking())) {
				for (Class<? extends XcpManutencaoBacking> detClass : this.getDetalhesBacking()) {
					if (getBacking(detClass).getModo() != MODO_LISTA) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void actionNomeEntidade() {
		XcpTabelaVO tabela = new XcpTabelaVO(this.entity);
		addFacesMessage(tabela.getDesTabela(), MSG_INFO);
	}

	private void iniciaComplementos() {
		if (this.isComplemento()) {
			if (this.xcpTabelaColunaVO == null) {
				this.setXcpTabelaColunaVO(this.xcpTabelaColunaSession.findListaXcpObjetosPars(this.getEjbVars(), this.getEntity()));
			}
			try {
				this.setXcpTabelaColunaVO(this.xcpTabelaColunaSession.buscaDadosColunas(this.getEjbVars(), this.getXcpTabelaColunaVO(), this.getEntity()));
			} catch (ClassNotFoundException e) {
				this.addFacesMessage(e);
			}
		}
	}

	public TIPO_MANUT getTipomManut() {
		return this.tipomManut;
	}

	public void setTipomManut(TIPO_MANUT tipomManut) {
		this.tipomManut = tipomManut;
	}

	public Class<MESTRE_BACK> getMestreBackingClass() {
		return this.mestreBackingClass;
	}

	public void setMestreBackingClass(Class<MESTRE_BACK> mestreBackingClass) {
		this.mestreBackingClass = mestreBackingClass;
	}

	public MESTRE_BACK getMestreBacking() {
		if (this.getMestreBackingClass() != null) {
			return getBacking(this.getMestreBackingClass());
		}
		return null;
	}

	public Class<? extends XcpManutencaoBacking>[] getDetalhesBacking() {
		return this.detalhesBacking;
	}

	public void setDetalhesBacking(Class<? extends XcpManutencaoBacking>... detalhesBacking) {
		this.detalhesBacking = detalhesBacking;
	}

	public void setComplemento(boolean complemento) {
		this.complemento = complemento;
	}

	public boolean isComplemento() {
		return this.complemento;
	}

	public boolean isComplementoCustomizado() {
		return this.complementoCustomizado;
	}

	public void setComplementoCustomizado(boolean complementoCustomizado) {
		this.complementoCustomizado = complementoCustomizado;
	}

	public void setPesquisaNoVoltar(boolean pesquisaNoVoltar) {
		this.pesquisaNoVoltar = pesquisaNoVoltar;
	}

	//DADOS

	public List getLista() throws Exception {
		if (this.lista == null && Util.in(this.getTipomManut(), TIPO_MANUT.NORMAL, TIPO_MANUT.MESTRE)) {
			this.setLista(this.pesquisar());
		}
		return this.lista;
	}

	public void setLista(List<?> lista) {
		this.lista = lista;
	}

	public List<?> getListaFiltrados() {
		return this.listaFiltrados;
	}

	public void setListaFiltrados(List<?> listaFiltrados) {
		this.listaFiltrados = listaFiltrados;
	}

	public XcapeEntity getEntity() {
		return this.entity;
	}

	public void setEntity(XcapeEntity entity) {
		this.entity = entity;
	}

	public XcapeEntity getSelectedEntity() {
		return this.getEntity();
	}

	public void setSelectedEntity(XcapeEntity entity) throws CloneNotSupportedException {
		this.setEntity(Util.deepClone(entity));
	}

	// ACOES

	public abstract List<?> pesquisar() throws Exception;

	/**
	 * Metodo utilizado para quando e necessario fazer algum processamento extra
	 * na linha alem de selecionar a entidade e mudar para o modo form.
	 * 
	 * @param entity
	 * @throws Exception
	 */
	public void selecionaEntidade(XcapeEntity entity) throws Exception {
		this.iniciaComplementos();
		this.carregarDetalhes();
	}

	protected void carregarDetalhes() throws Exception {
		//Carrega as entidade DETALHES
		if (!Util.isEmpty(this.getDetalhesBacking())) {
			for (Class<? extends XcpManutencaoBacking> detClass : this.getDetalhesBacking()) {
				getBacking(detClass).actionPesquisar(null);
			}
		}
	}

	public boolean remover() throws Exception {
		this.xcpPersistSession.remove(this.getEjbVars(), this.getEntity());
		return true;
	}

	public boolean gravar() throws Exception {
		this.setEntity(this.xcpPersistSession.update(this.getEjbVars(), this.getEntity()));
		return true;
	}

	public void actionNovo(String id) throws Exception {
		this.setModo(MODO_FORM);
		this.setEntity(null);
		this.selecionaEntidade(null);

	}

	public void actionGravar(String id) throws Exception {
		if (this.gravar()) {
			if (this.isComplemento()) {
				this.xcpTabelaColunaSession.gravaDadosColunas(this.getEjbVars(), this.xcpTabelaColunaVO, this.getEntity());
			}
			addFacesMessage(super.getTraducao("msg_gravado_ok"), MSG_INFO);
			if (Util.in(this.getTipomManut(), TIPO_MANUT.NORMAL, TIPO_MANUT.DETALHE)) {
				this.setEntity(null);
				this.actionPesquisar(id);
			} else {
				this.iniciaComplementos();
				this.pesquisaNoVoltar = true;
			}
		}
	}

	public void actionRemover(String id) throws Exception {
		if (this.remover()) {
			this.xcpTabelaColunaSession.removeDadosColunas(this.getEjbVars(), this.xcpTabelaColunaVO, this.getEntity());
			addFacesMessage(super.getTraducao("msg_removido_ok"), MSG_INFO);
			this.setEntity(null);

			this.actionPesquisar(id);
		}
	}

	public void actionDuplicar() throws Exception {
		this.getEntity().setPK(null);
		this.selecionaEntidade(this.getEntity());
		addFacesMessage(super.getTraducao("msg_duplicado_gravar_ok"), MSG_INFO);
	}

	public void actionVoltar(String id) throws Exception {
		if (this.pesquisaNoVoltar) {
			this.actionPesquisar(id);
		}
		this.setModo(MODO_LISTA);
	}

	public void actionPesquisar(String id) throws Exception {
		Map<String, String> filterMap = this.getFilterMap();
		DataTable tab = (DataTable) this.findComponent("table_" + id);
		if (tab != null) {
			tab.reset();
			tab.setValueExpression("sortBy", null);
		}
		this.setListaFiltrados(null);
		this.setLista(this.pesquisar());
		this.setModo(MODO_LISTA);
		this.pesquisaNoVoltar = false;
		this.setFilterMap(filterMap);
	}

	public void onRowSelect() throws Exception {
		this.setModo(MODO_FORM);
		this.selecionaEntidade(this.getEntity());
	}

	public void actionImprimir(String id) throws Exception {
		XcpExecObjBacking.executar(this.imprimir());
	}

	public XcpExecObjInputVO imprimir() {
		return new XcpExecObjInputVO(this.getRotina().getCodObjeto());
	}

	private AcessofuncView permAcessoView;

	public boolean isDisabledNovo() {
		return INT_0.equals(this.getPermAcessoView().getIncluir());
	}

	public boolean isDisabledGravar() {
		/*
		 * Alterado para corretamente tratar telas que somente podem incluir e nao
		 * alterar registros.
		 */
		boolean incluir = INT_1.equals(this.getPermAcessoView().getIncluir());
		boolean novo = this.getEntity().isNovo();

		if (incluir && novo) {
			return false;
		}

		return INT_0.equals(this.getPermAcessoView().getAlterar());
	}

	public boolean isDisabledExcluir() {
		return INT_0.equals(this.getPermAcessoView().getExcluir());
	}

	private AcessofuncView getPermAcessoView() {
		if (this.permAcessoView == null) {
			this.permAcessoView = this.operadoresSession.buscaPermissao(
					this.getEjbVars(),
					this.getCodEmpresa(),
					this.getSession().getUsuario().getOperador(),
					this.getRotina().getRotina());

			if (this.permAcessoView == null) {
				this.permAcessoView = new AcessofuncView();
			}
		}
		return this.permAcessoView;
	}

	//TODO - aqui nao é lugar disso!
	public Collection<SelectItem> getItensMesesAno() {
		return this.getItens("meses_ano", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31);
	}

	public XcpTabelaColunaVO getXcpTabelaColunaVO() {
		return this.xcpTabelaColunaVO;
	}

	public void setXcpTabelaColunaVO(XcpTabelaColunaVO xcpTabelaColunaVO) {
		this.xcpTabelaColunaVO = xcpTabelaColunaVO;
	}

	public Long getCodServidorEmailEmpresa() throws Exception {
		return this.getParametroLong("Sistema", 6);
	}

	public boolean isRenderedPainelEsocial(Integer tipo) {
		if (this.getEntity().isNovo()) {
			return false;
		}

		boolean ativo;
		try {
			ativo = this.esocialSession.isAtivo(this.getEjbVars(), this.getCodEmpresa());
		} catch (Exception e) {
			return false;
		}

		if (!ativo) {
			return false;
		}

		//		Integer tiponum = Converter.toInteger(tipo);
		if (Objects.equals(tipo, 1)) {
			FuncionariosManut manut = (FuncionariosManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocFuncEventos e");
			q.addWhere("empresa", "=", manut.getEmpresa());
			q.addWhere("matricula", "=", manut.getMatricula());
			q.setOrderBy("datainc desc");
			List<EsocFuncEventos> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			if (Util.isEmpty(list)) {
				return false;
			}
		} else if (Objects.equals(tipo, 2)) {
			MovimentacoesManut manut = (MovimentacoesManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocMovEventos e");
			q.addWhere("mov", "=", manut.getMvto());
			q.setOrderBy("datainc desc");
			List<EsocMovEventos> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			if (Util.isEmpty(list)) {
				return false;
			}
		} else if (Objects.equals(tipo, 3)) {
			FeriasManut manut = (FeriasManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocFeriasEventosManut e");
			q.addWhere("empresa", "=", manut.getEmpresa());
			q.addWhere("matricula", "=", manut.getMatricula());
			q.addWhere("dtvcto", "=", manut.getDtvcto());
			q.addWhere("seqfer", "=", manut.getSeq());
			q.setOrderBy("datainc desc");
			List<EsocFeriasEventos> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			if (Util.isEmpty(list)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Valida o intervalo informado em cadastros de tabelas basicas do esocial.
	 * Nao usado por enquanto, pois nao esta claro como tratar historicos
	 */
	public boolean isValidoPainelEsocial(Date dtiniesoc, Date dtfimesoc) {
		if (!this.isRenderedPainelEsocial(0)) {
			return true;
		}

		if (!Util.isPeriodoValido(dtiniesoc, dtfimesoc)) {
			addFacesMessage(this.getTraducao("msg_inicio_maior_esoc"));
			return false;
		}

		if (dtfimesoc != null) {
			Calendar calatu = Calendar.getInstance();
			calatu.set(Calendar.HOUR_OF_DAY, 0);
			calatu.set(Calendar.MINUTE, 0);
			calatu.set(Calendar.SECOND, 0);
			calatu.set(Calendar.MILLISECOND, 0);
			calatu.set(Calendar.DAY_OF_MONTH, 1);

			Date dtatu = calatu.getTime();

			Calendar cal = Calendar.getInstance();
			cal.setTime(dtfimesoc);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);

			Date dtfim = cal.getTime();

			if (dtfim.compareTo(dtatu) > 0) {
				addFacesMessage(this.getTraducao("msg_fim_esoc_maior_atu"));
				return false;
			}
		}

		return true;
	}

	public void actionOpenEventos(Integer tipo) {
		if (tipo == null) {
			return;
		}
		List<EsocEventosVO> listevt = new ArrayList<EsocEventosVO>();
		if (tipo == 1) {
			FuncionariosManut manut = (FuncionariosManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocFuncEventosManut e");
			q.addWhere("empresa", "=", manut.getEmpresa());
			q.addWhere("matricula", "=", manut.getMatricula());
			q.setOrderBy("datainc desc");
			List<EsocFuncEventosManut> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			for (EsocFuncEventosManut e : list) {
				EsocTipoManut evento = this.xcpQuerySession.find(this.getEjbVars(), EsocTipoManut.class, e.getSeqeventoFk().getTipo());
				EsocEventosVO vo = new EsocEventosVO();
				vo.setData(e.getDatainc());
				vo.setDescricao(evento.getDescricao());
				vo.setEvento(evento.getEvento());
				vo.setLote(e.getSeqeventoFk().getLote());
				vo.setSeq(e.getSeqevento());
				if (e.getSeqeventoFk() != null) {
					vo.setRecibo(e.getSeqeventoFk().getRecibo());
				}
				listevt.add(vo);
			}
		} else if (tipo == 2) {
			MovimentacoesManut manut = (MovimentacoesManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocMovEventosManut e");
			q.addWhere("mov", "=", manut.getMvto());
			q.setOrderBy("datainc desc");
			List<EsocMovEventosManut> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			for (EsocMovEventosManut e : list) {
				EsocTipoManut evento = this.xcpQuerySession.find(this.getEjbVars(), EsocTipoManut.class, e.getSeqeventoFk().getTipo());
				EsocEventosVO vo = new EsocEventosVO();
				vo.setData(e.getDatainc());
				vo.setDescricao(evento.getDescricao());
				vo.setEvento(evento.getEvento());
				vo.setLote(e.getSeqeventoFk().getLote());
				vo.setSeq(e.getSeqevento());
				if (e.getSeqeventoFk() != null) {
					vo.setRecibo(e.getSeqeventoFk().getRecibo());
				}
				listevt.add(vo);
			}
		} else if (tipo == 3) {
			FeriasManut manut = (FeriasManut) this.getEntity();
			MontaQuery q = new MontaQuery("Select e from EsocFeriasEventosManut e");
			q.addWhere("empresa", "=", manut.getEmpresa());
			q.addWhere("matricula", "=", manut.getMatricula());
			q.addWhere("dtvcto", "=", manut.getDtvcto());
			q.addWhere("seqfer", "=", manut.getSeq());
			q.setOrderBy("datainc desc");
			List<EsocFeriasEventosManut> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			for (EsocFeriasEventosManut e : list) {
				EsocTipoManut evento = this.xcpQuerySession.find(this.getEjbVars(), EsocTipoManut.class, e.getSeqeventoFk().getTipo());
				EsocEventosVO vo = new EsocEventosVO();
				vo.setData(e.getDatainc());
				vo.setDescricao(evento.getDescricao());
				vo.setEvento(evento.getEvento());
				vo.setLote(e.getSeqeventoFk().getLote());
				vo.setSeq(e.getSeqevento());
				if (e.getSeqeventoFk() != null) {
					vo.setRecibo(e.getSeqeventoFk().getRecibo());
				}
				listevt.add(vo);
			}

		} else if (tipo == 4) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			AcidentestrabManut manut = (AcidentestrabManut) this.getEntity();

			StringBuilder sb = new StringBuilder();
			sb.append(" Select e from EsocAlteracao a, EsocEvento e  ");
			sb.append("   where a.tipSituacao = 2                    ");
			sb.append("     and a.seqevento = e.seq                  ");
			sb.append("     and a.empresa = ");
			sb.append(manut.getEmpresa());
			sb.append("     and a.pk like 'S2210|-|");
			sb.append(manut.getMatricula());
			sb.append("|-|");
			sb.append(sdf.format(manut.getDatahora()));
			sb.append("'");
			List<EsocEvento> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb.toString(), new HashMap<String, Object>());
			for (EsocEvento e : list) {
				EsocTipoManut tipoevento = this.xcpQuerySession.find(this.getEjbVars(), EsocTipoManut.class, e.getTipo());
				EsocEventosVO vo = new EsocEventosVO();
				vo.setData(e.getDtcriacao());
				vo.setDescricao(tipoevento.getDescricao());
				vo.setEvento(tipoevento.getEvento());
				vo.setLote(e.getLote());
				vo.setSeq(e.getSeq());
				vo.setRecibo(e.getRecibo());
				listevt.add(vo);
			}
		}

		getRequestContext().execute("PF('wlistesoc').show()");
		this.setListEvtEsocial(listevt);
	}

	public List<EsocEventosVO> getListEvtEsocial() {
		return this.listEvtEsocial;
	}

	public void setListEvtEsocial(List<EsocEventosVO> listEvtEsocial) {
		this.listEvtEsocial = listEvtEsocial;
	}
}