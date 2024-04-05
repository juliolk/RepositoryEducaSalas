package com.adm.xcp.backing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.adm.ejb.entity.FuncionariosListaView;
import com.adm.ejb.entity.pk.FuncionariosListaViewPK;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.local.ConfigPanelfuncionarioSession;
import com.adm.ejb.session.local.FolhaSession;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.ejb.session.remote.FuncionariosSession.TipoTela;
import com.adm.ejb.vo.FiltroVO;
import com.adm.gui.backing.FuncionariosBacking;
import com.adm.util.ejb.XcapeEntity;
import com.adm.util.exceptions.ParametroNaoEncontradoException;

public abstract class XcpFuncionarioBackingLazy extends XcpManutencaoBacking {

	@EJB
	protected FuncionariosSession funcionariosSession;
	@EJB
	private FolhaSession folhaSession;

	@EJB
	private ConfigPanelfuncionarioSession configPanelSession;

	protected LazyDataModel<?> listaFuncionariosLazy;
	private FuncionariosListaView funcionario;
	private boolean permiteNovo = true;
	private boolean permiteGravar = true;
	private boolean permiteExcluir = true;
	private String dtaAdmissaoFilter = "";
	private Map<String, Boolean> configColunasVisiveis = null;
	private Long parSistema14 = null;

	private Integer situacaoFuncionario = FuncionariosSession.SITUACAO_0_ATIVO;

	public String getDtaAdmissaoFilter() {
		return this.dtaAdmissaoFilter;
	}

	public void setDtaAdmissaoFilter(String dtaAdmissaoFilter) {
		this.dtaAdmissaoFilter = dtaAdmissaoFilter;
	}

	public XcpFuncionarioBackingLazy() {
		this.listaFuncionariosLazy = new LazyDataModel<FuncionariosListaView>() {
			private static final long serialVersionUID = 1L;

			private List<FuncionariosListaView> lista;

			@Override
			public List<FuncionariosListaView> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

				if (XcpFuncionarioBackingLazy.this.getSession().getUsuario().getOperador() == null) {
					return null;
				}

				String sortOrderString = "";
				if (sortOrder == SortOrder.ASCENDING)
					sortOrderString = "asc";
				else if (sortOrder == SortOrder.DESCENDING)
					sortOrderString = "desc";

				Map<String, Object> result = XcpFuncionarioBackingLazy.this.funcionariosSession.buscaListaFuncionariosLazy(
						XcpFuncionarioBackingLazy.this.getEjbVars(),
						XcpFuncionarioBackingLazy.this.getCodEmpresa(),
						XcpFuncionarioBackingLazy.this.getSession().getUsuario().getOperador(),
						XcpFuncionarioBackingLazy.this.getTipoTela(),
						XcpFuncionarioBackingLazy.this.getFiltroAdicional(),
						XcpFuncionarioBackingLazy.this.getSituacaoFuncionario(),
						first,
						pageSize,
						sortField,
						sortOrderString,
						filters);
				Integer rowCount = (Integer) result.get("count");
				this.setRowCount(rowCount);

				this.lista = (List<FuncionariosListaView>) result.get("list");
				return this.lista;
			}

			@Override
			public FuncionariosListaView getRowData(String rowKey) {
				try {

					for (FuncionariosListaView f : this.lista) {
						if (f.getPK().toString().equals(rowKey)) {
							return XcpFuncionarioBackingLazy.this.xcpQuerySession.find(
									XcpFuncionarioBackingLazy.this.getEjbVars(),
									FuncionariosListaView.class,
									f.getPK());
						}
					}

				} catch (Exception e) {
					XcpFuncionarioBackingLazy.this.addFacesMessage(e);
				}
				return null;
			}

			@Override
			public Object getRowKey(FuncionariosListaView pk) {
				XcapeEntity xpk = pk;
				return xpk.getPK();
			}

		};
	}

	@PostConstruct
	private void init() {
		this.configColunasVisiveis = this.configPanelSession.getConfigPanelFuncionarios(this.getEjbVars(), this.tipoTela);
	}

	/*
	 * Metodo para limpar a selecao no momento em que alguma ação é feita no
	 * componente.
	 */
	protected void limpaSelecao() {
		this.setFuncionario(null);
		this.setLista(null);
	}

	public LazyDataModel<?> getListaFuncionariosLazy() {
		return this.listaFuncionariosLazy;
	}

	public void setListaFuncionariosLazy(LazyDataModel<?> listaFuncionariosLazy) {
		this.listaFuncionariosLazy = listaFuncionariosLazy;
	}

	public void setPermiteExcluir(boolean permiteExcluir) {
		this.permiteExcluir = permiteExcluir;
	}

	public void setPermiteGravar(boolean permiteGravar) {
		this.permiteGravar = permiteGravar;
	}

	public boolean isPermiteExcluir() {
		return this.permiteExcluir;
	}

	public boolean isPermiteGravar() {
		return this.permiteGravar;
	}

	public boolean isPermiteNovo() {
		return this.permiteNovo;
	}

	public void setPermiteNovo(boolean permiteNovo) {
		this.permiteNovo = permiteNovo;
	}

	public Integer getSituacaoFuncionario() {
		return this.situacaoFuncionario;
	}

	public void setSituacaoFuncionario(Integer situacaoFuncionario) {
		this.situacaoFuncionario = situacaoFuncionario;
	}

	private TipoTela tipoTela;
	private List<FiltroVO> filtroAdicional = null;

	protected List<FiltroVO> getFiltroAdicional() {
		return this.filtroAdicional;
	}

	protected void setFiltroAdicional(List<FiltroVO> filtroAdicional) {
		this.filtroAdicional = filtroAdicional;
	}

	public TipoTela getTipoTela() {
		return this.tipoTela;
	}

	public TipoTela getTipoTelaAFDT() {
		return TipoTela.AFDT;
	}

	public TipoTela getTipoTelaFerias() {
		return TipoTela.FERIAS;
	}

	public TipoTela getTipoTelaPonto() {
		return TipoTela.PONTO;
	}

	public void setTipoTela(TipoTela tipoTela) {
		this.tipoTela = tipoTela;
	}

	public FuncionariosListaView getFuncionario() {
		return this.funcionario;
	}

	public void setFuncionario(FuncionariosListaView funcionario) {
		this.funcionario = funcionario;
	}

	public void onRowSelectFuncionario() throws Exception {
		this.selecionaFuncionario(this.getFuncionario());
	}

	public void selecionaFuncionario(FuncionariosListaView funcionario) throws Exception {
		this.actionPesquisar("xcpFuncionarioBacking");
	}

	public void actionAbrirEdicaoFuncionario() {
		if (this.getFuncionario() == null) {
			return;
		}
		FuncionariosListaViewPK listPK = this.getFuncionario().getPK();
		FuncionariosPK pk = new FuncionariosPK();
		pk.setEmpresa(listPK.getCodEmpresa());
		pk.setMatricula(listPK.getNumMatricula());
		this.getSession().putNavigationParam(FuncionariosBacking.NAVEGACAO_FUNC_PK, pk);
	}

	public Collection<SelectItem> getItensSituacaoFuncionario() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		// TODO Ver necessidade da opção Todos
		// itens.add(new SelectItem(-1, "Todos"));
		itens.addAll(this.getItensSemNull(
				"situacaoFuncionario",
				FuncionariosSession.SITUACAO_0_ATIVO,
				FuncionariosSession.SITUACAO_1_DESLIGADO,
				FuncionariosSession.SITUACAO_2_AFASTADO));
		return itens;
	}

	public Boolean canRender(String coluna) {
		return this.configColunasVisiveis.get(coluna.toLowerCase());
	}

	public void actionLimpaSelecao() {
		this.limpaSelecao();
	}

	public boolean isDisabledRelatorio() {
		Method[] declaredMethods = this.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			if ("imprimir".equals(method.getName()) && method.getParameterTypes().length == 0) {
				return this.getFuncionario() == null;
			}
		}
		return false;
	}

	private Long getParSistema14() {
		if (this.parSistema14 == null) {
			try {
				this.parSistema14 = this.getParametroLong("Sistema", 14);
			} catch (ParametroNaoEncontradoException e) {
				//orgao
				this.parSistema14 = LONG_1;
			}
		}
		return this.parSistema14;
	}

	public void setParSistema14(Long parSistema14) {
		this.parSistema14 = parSistema14;
	}

	public String getDesLabelLocal() {
		if (LONG_1.equals(this.getParSistema14())) {
			return this.getTraducao("lbl_numOrgaoFunc");
		}
		return this.getTraducao("lbl_numSetorFunc");
	}

	public String getDesLocal() {
		if (LONG_1.equals(this.getParSistema14())) {
			return this.getFuncionario().getDesOrgao();
		}
		return this.getFuncionario().getDesSetor();
	}

	public String getDesSituacaoFuncional() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getFuncionario().getTipMovimento());
		sb.append(" - ");
		sb.append(this.getFuncionario().getDesMovimento());
		return sb.toString();
	}
}