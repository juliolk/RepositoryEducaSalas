package com.adm.xcp.backing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.primefaces.component.datatable.DataTable;

import com.adm.ejb.entity.FuncionariosListaView;
import com.adm.ejb.entity.PadroesNiveis;
import com.adm.ejb.entity.Vinculos;
import com.adm.ejb.entity.extend.DependentesManut;
import com.adm.ejb.entity.extend.FuncionariosManut;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.ejb.session.remote.FuncionariosSession.TipoTela;
import com.adm.ejb.vo.FiltroVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

/**
 * Backing para apresentação das abas dos dados pessoais e de dependentes
 * 
 * @author Andreo
 */
public abstract class XcpFuncionarioDetBacking extends XcpFuncionarioPadraoBacking {

	private List<FuncionariosListaView> listaFuncionarios;
	private List<FuncionariosListaView> listaFuncionariosFiltrados;
	private FuncionariosListaView funcionario;
	private boolean permiteNovo = true;
	private Integer situacaoFuncionario = FuncionariosSession.SITUACAO_0_ATIVO;
	private FuncionariosManut funcionarioDet;
	private List<SelectItem> itensNiveis;

	public FuncionariosManut getFuncionarioDet() {
		return this.funcionarioDet;
	}

	public void setFuncionarioDet(FuncionariosManut funcionarioDet) {
		if (funcionarioDet != null) {
			try {
				super.carregaDependentesFuncionario(funcionarioDet.getEmpresa(), funcionarioDet.getMatricula());
			} catch (ClassNotFoundException e) {
				this.addFacesMessage(e);
			}
		} else {
			this.setListaFilhacao(null);
			this.setListaOutrosDependentes(null);
			this.setListaFilhos(null);
			this.setListaConjuges(null);
		}
		this.funcionarioDet = funcionarioDet;
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

	public void setTipoTela(TipoTela tipoTela) {
		this.tipoTela = tipoTela;
	}

	public void actionPesquisarFuncionarios(String id) throws Exception {
		DataTable tab = (DataTable) this.findComponent("table_func_" + id);
		if (tab != null) {
			tab.reset();
			tab.setValueExpression("sortBy", null);
		}
		this.setListaFuncionariosFiltrados(null);
		this.setListaFuncionarios(this.pesquisarFuncionarios());
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

	public List<DependentesManut> getListaFilhacao() {
		return super.getListaFilhacao(this.getFuncionarioDet());
	}

	public void onRowSelectFuncionario() throws Exception {
		FuncionariosListaView func = this.getFuncionario();
		this.setFuncionarioDet(null);
		if (func != null) {
			FuncionariosPK pk = new FuncionariosPK();
			pk.setEmpresa(func.getCodEmpresa());
			pk.setMatricula(func.getNumMatricula());
			this.setFuncionarioDet(this.xcpQuerySession.find(this.getEjbVars(), FuncionariosManut.class, pk));
		}
		this.selecionaFuncionario(func);
	}

	public void selecionaFuncionario(FuncionariosListaView funcionario) throws Exception {
		this.actionPesquisar("xcpFuncionarioDetBacking");
		this.setModo(MODO_FORM);
	}

	public Collection<SelectItem> getItensSituacaoFuncionario() {
		return this.getItensSemNull(
				"situacaoFuncionario",
				FuncionariosSession.SITUACAO_0_ATIVO,
				FuncionariosSession.SITUACAO_1_DESLIGADO,
				FuncionariosSession.SITUACAO_2_AFASTADO);
	}

	public String getDesPathDoc(Integer tipDoc) {
		return super.getDesPathDoc(this.getCodEmpresa(), this.funcionarioDet.getMatricula(), tipDoc);
	}

	public boolean isRenderedContrato() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_35_CONTRATOS);
	}

	public boolean isRenderedEstatutarioEdital() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_2_ESTATUTARIO);
	}

	public boolean isRenderedEstatutario() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_30_ESTATUTARIO);
	}

	public boolean isRenderedEstagiario() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_99_ESTAGIARIO, Vinculos.RAIS_55_APRENDIZ);
	}

	public boolean isRenderedAdidos() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_3_ADIDOS);
	}

	public boolean isRenderedFuncionario() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_10_FUNCIONARIOS, Vinculos.RAIS_60_CONTRATOS);
	}

	public boolean isRenderedLovConv() {
		return Util.in(this.getFuncionarioDet().getVinculoFk().getRais(), Vinculos.RAIS_3_ADIDOS, Vinculos.RAIS_99_ESTAGIARIO, Vinculos.RAIS_55_APRENDIZ);
	}

	public List<SelectItem> getItensNiveis() {
		if (this.itensNiveis == null) {

			MontaQuery q = new MontaQuery("select p from PadroesNiveis p, CargosPadroes e");
			q.addWhere("e.padrao = p.padrao");
			q.addWhere("e.empresa = p.empresa");
			q.addWhere("padrao", "=", this.getFuncionarioDet().getPadrao());
			q.addWhere("cargo", "=", this.getFuncionarioDet().getCargo());
			q.addWhere("empresa", "=", this.getFuncionarioDet().getEmpresa());
			List<PadroesNiveis> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensNiveis = new ArrayList<SelectItem>();
			for (PadroesNiveis e : lista) {
				this.itensNiveis.add(new SelectItem(e.getClasse(), e.getClasse()));
			}
			this.itensNiveis.add(new SelectItem(null, null));
		}

		return this.itensNiveis;
	}

	public BigDecimal getHrsemanais() {
		BigDecimal hr = this.getFuncionarioDet().getHrmensais();
		if (hr != null) {
			return hr.divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP);
		}
		return null;
	}
}