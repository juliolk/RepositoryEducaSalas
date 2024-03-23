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
	public List<EsocEventosVO> getListEvtEsocial() {
		return this.listEvtEsocial;
	}

	public void setListEvtEsocial(List<EsocEventosVO> listEvtEsocial) {
		this.listEvtEsocial = listEvtEsocial;
	}
}