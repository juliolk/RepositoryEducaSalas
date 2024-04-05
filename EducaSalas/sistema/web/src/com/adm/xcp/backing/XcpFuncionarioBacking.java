package com.adm.xcp.backing;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import org.primefaces.component.datatable.DataTable;

import com.adm.ejb.entity.FuncionariosListaView;
import com.adm.ejb.entity.pk.FuncionariosListaViewPK;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.local.ConfigPanelfuncionarioSession;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.ejb.session.remote.FuncionariosSession.TipoTela;
import com.adm.ejb.vo.FiltroVO;
import com.adm.gui.backing.FuncionariosBacking;
import com.adm.util.exceptions.ParametroNaoEncontradoException;

public abstract class XcpFuncionarioBacking extends XcpManutencaoBacking {

	@EJB
	protected FuncionariosSession funcionariosSession;
	private List<FuncionariosListaView> listaFuncionarios;
	private List<FuncionariosListaView> listaFuncionariosFiltrados;
	private FuncionariosListaView funcionario;
	private boolean permiteNovo = true;
	private boolean permiteGravar = true;
	private boolean permiteExcluir = true;
	private Map<String, Boolean> configColunasVisiveis = null;
	private Long parSistema14 = null;

	@EJB
	private ConfigPanelfuncionarioSession configPanelSession;

	private Integer situacaoFuncionario = FuncionariosSession.SITUACAO_0_ATIVO;

	@PostConstruct
	private void init() {
		this.configColunasVisiveis = this.configPanelSession.getConfigPanelFuncionarios(this.getEjbVars(), this.tipoTela);
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

	public List<FuncionariosListaView> getListaFuncionarios() throws Exception {
		if (this.listaFuncionarios == null) {
			this.listaFuncionarios = this.pesquisarFuncionarios();
		}
		return this.listaFuncionarios;
	}

	public void setListaFuncionarios(List<FuncionariosListaView> listaFuncionarios) {
		this.listaFuncionarios = listaFuncionarios;
	}

	public List<FuncionariosListaView> getListaFuncionariosFiltrados() {
		return this.listaFuncionariosFiltrados;
	}

	public void setListaFuncionariosFiltrados(List<FuncionariosListaView> listaFuncionariosFiltrados) {
		this.listaFuncionariosFiltrados = listaFuncionariosFiltrados;
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

	public void actionPesquisarFuncionarios(String id) throws Exception {
		Map<String, String> filterMap = this.getFilterMap();
		DataTable tab = (DataTable) this.findComponent("table_func_" + id);
		if (tab != null) {
			tab.reset();
			tab.setValueExpression("sortBy", null);
		}
		this.setListaFuncionariosFiltrados(null);
		this.setListaFuncionarios(this.pesquisarFuncionarios());
		this.setFilterMap(filterMap);
	}

	private List<FuncionariosListaView> pesquisarFuncionarios() throws Exception {
		return this.funcionariosSession.buscaListaFuncionarios(
				this.getEjbVars(),
				this.getCodEmpresa(),
				this.getSession().getUsuario().getOperador(),
				this.getTipoTela(),
				this.getFiltroAdicional(),
				this.getSituacaoFuncionario());
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
		return this.getItensSemNull(
				"situacaoFuncionario",
				FuncionariosSession.SITUACAO_0_ATIVO,
				FuncionariosSession.SITUACAO_1_DESLIGADO,
				FuncionariosSession.SITUACAO_2_AFASTADO);
	}

	public Boolean canRender(String coluna) {
		return this.configColunasVisiveis.get(coluna.toLowerCase());
	}

	//Se esta sobrescrevendo o imprimir, provavelmeente esta querendo pegar o in_matricula selecionado
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