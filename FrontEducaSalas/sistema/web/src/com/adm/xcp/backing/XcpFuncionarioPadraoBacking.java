package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.Atestados;
import com.adm.ejb.entity.Cor;
import com.adm.ejb.entity.Credenciadores;
import com.adm.ejb.entity.Deficiencias;
import com.adm.ejb.entity.Dependentes;
import com.adm.ejb.entity.DependentesFil;
import com.adm.ejb.entity.Ec;
import com.adm.ejb.entity.Formacao;
import com.adm.ejb.entity.Funcionarios;
import com.adm.ejb.entity.Parentesco;
import com.adm.ejb.entity.Reajustes;
import com.adm.ejb.entity.Ufs;
import com.adm.ejb.entity.base.FuncionariosBase;
import com.adm.ejb.entity.extend.DependentesManut;
import com.adm.ejb.entity.extend.FilhosManut;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.ejb.vo.XcpTabelaColunaVO;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

/**
 * Backing para conter as listas
 * 
 * @author Andreo
 */
public abstract class XcpFuncionarioPadraoBacking extends XcpManutencaoBacking {
	@EJB
	protected FuncionariosSession funcionariosSession;

	private Collection<SelectItem> itensEC;
	private Collection<SelectItem> itensCor;
	private Collection<SelectItem> itensDeficiencias;
	private Collection<SelectItem> itensCredenciadores;
	private Collection<SelectItem> itensParentesco;
	private Collection<SelectItem> itensParentescoFiliacao;
	private Collection<SelectItem> itensUf;

	private List<DependentesManut> listaFilhacao;
	private List<FilhosManut> listaFilhos;
	private List<DependentesManut> listaConjuges;

	private List<DependentesManut> listaOutrosDependentes;
	private boolean informaMatricula;
	private Long matriculaDestino;
	private Boolean exibirUniaoEstavel;

	public Long getMatriculaDestino() {
		return this.matriculaDestino;
	}

	public boolean isInformaMatricula() {
		return this.informaMatricula;
	}

	public void setInformaMatricula(boolean informaMatricula) {
		this.informaMatricula = informaMatricula;
	}

	public Collection<SelectItem> getItensParentescoFiliacao() {
		if (this.itensParentescoFiliacao == null) {
			MontaQuery q = new MontaQuery("select e from Parentesco e");
			StringBuilder notIn = new StringBuilder();
			notIn.append("grau in (");
			notIn.append(Dependentes.GRAU_1_CONJUGE);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_3_PAI);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_4_MAE);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_14_COMPANHEIRO);
			notIn.append(")");
			q.addWhere(notIn.toString());
			q.setOrderBy("descricao");
			List<Parentesco> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensParentescoFiliacao = new ArrayList<SelectItem>();
			for (Parentesco e : lista) {
				this.itensParentescoFiliacao.add(new SelectItem(e.getGrau(), e.getDescricao()));
			}
		}
		return this.itensParentescoFiliacao;
	}

	public Collection<SelectItem> getItensParentesco() {
		if (this.itensParentesco == null) {
			MontaQuery q = new MontaQuery("select e from Parentesco e");

			StringBuilder notIn = new StringBuilder();
			notIn.append("grau not in (");
			notIn.append(Dependentes.GRAU_1_CONJUGE);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_3_PAI);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_4_MAE);
			notIn.append(",");
			notIn.append(Dependentes.GRAU_14_COMPANHEIRO);
			notIn.append(")");
			q.addWhere(notIn.toString());
			q.setOrderBy("descricao");
			List<Parentesco> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensParentesco = new ArrayList<SelectItem>();
			for (Parentesco e : lista) {
				this.itensParentesco.add(new SelectItem(e.getGrau(), e.getDescricao()));
			}
		}
		return this.itensParentesco;
	}

	public Collection<SelectItem> getItensUfNull() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));
		itens.addAll(this.getItensUf());
		return itens;
	}

	public Collection<SelectItem> getItensUf() {
		if (this.itensUf == null) {
			MontaQuery q = new MontaQuery("select e from Ufs e ");
			q.setOrderBy("uf");
			List<Ufs> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensUf = new ArrayList<SelectItem>();
			for (Ufs e : lista) {
				this.itensUf.add(new SelectItem(e.getUf(), e.getUf()));
			}
		}
		return this.itensUf;
	}

	public Collection<SelectItem> getItensECNull() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));
		itens.addAll(this.getItensEC());
		return itens;
	}

	public Collection<SelectItem> getItensEC() {
		if (this.itensEC == null) {
			MontaQuery q = new MontaQuery("select e from Ec e ");
			q.setOrderBy("descricao");
			List<Ec> listaEC = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensEC = new ArrayList<SelectItem>();
			for (Ec ec : listaEC) {
				this.itensEC.add(new SelectItem(ec.getCodigo(), ec.getDescricao()));
			}

		}
		return this.itensEC;
	}

	public Collection<SelectItem> getItensCor() {
		if (this.itensCor == null) {
			MontaQuery q = new MontaQuery("select e from Cor e");
			q.setOrderBy("descricao");
			List<Cor> listaCor = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensCor = new ArrayList<SelectItem>();
			for (Cor cor : listaCor) {
				this.itensCor.add(new SelectItem(cor.getCodigo(), cor.getDescricao()));
			}

		}
		return this.itensCor;
	}

	public Collection<SelectItem> getItensCorNull() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));
		itens.addAll(this.getItensCor());
		return itens;
	}

	public Collection<SelectItem> getItensDeficiencias() {
		if (this.itensDeficiencias == null) {
			MontaQuery q = new MontaQuery("select e from Deficiencias e");
			List<Deficiencias> listaCor = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensDeficiencias = new ArrayList<SelectItem>();
			for (Deficiencias deficiencias : listaCor) {
				this.itensDeficiencias.add(new SelectItem(deficiencias.getCodigo(), deficiencias.getDescricao()));
			}
		}
		return this.itensDeficiencias;
	}

	public Collection<SelectItem> getItensDeficienciasNull() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));
		itens.addAll(this.getItensDeficiencias());
		return itens;
	}

	public Collection<SelectItem> getItensCredenciadoresNull() {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();
		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));
		itens.addAll(this.getItensCredenciadores());
		return itens;
	}

	public Collection<SelectItem> getItensCredenciadores() {
		if (this.itensCredenciadores == null) {
			MontaQuery q = new MontaQuery("select e from Credenciadores e");
			List<Credenciadores> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			this.itensCredenciadores = new ArrayList<SelectItem>();
			for (Credenciadores e : lista) {
				this.itensCredenciadores.add(new SelectItem(e.getCredenciador(), e.getDescricao()));
			}
		}
		return this.itensCredenciadores;
	}

	public void addFilhacao(DependentesManut dep) {
		if (this.listaFilhacao == null) {
			this.listaFilhacao = new ArrayList<>();
		}
		this.listaFilhacao.add(dep);
	}

	public void setListaFilhacao(List<DependentesManut> listaFilhacao) {
		this.listaFilhacao = listaFilhacao;
	}

	protected List<DependentesManut> getListaFilhacao(FuncionariosBase base) {
		if (Util.isEmpty(this.listaFilhacao)) {
			this.listaFilhacao = new ArrayList<DependentesManut>();
		}

		List<DependentesManut> listDep = new ArrayList<DependentesManut>();
		for (DependentesManut fil : this.listaFilhacao) {
			if (Util.in(fil.getGrau(), Dependentes.GRAU_4_MAE, Dependentes.GRAU_3_PAI, Dependentes.GRAU_1_CONJUGE, Dependentes.GRAU_14_COMPANHEIRO)) {
				listDep.add(fil);
			}
		}

		Integer countMae = 0;
		Integer countPai = 0;
		DependentesManut conjuge = null;
		for (DependentesManut d : listDep) {
			if (Util.in(d.getGrau(), Dependentes.GRAU_4_MAE)) {
				countMae++;
			} else if (Util.in(d.getGrau(), Dependentes.GRAU_3_PAI)) {
				countPai++;
			} else {
				conjuge = d;
			}
		}

		if (countMae + countPai < 2) {
			if (countMae == 0) {
				DependentesManut dep = new DependentesManut();
				dep.setDependenteir(Dependentes.DEPENDENTEIR_2_NAO);
				dep.setIndHistorico(INT_0);
				dep.setGrau(Dependentes.GRAU_4_MAE);
				dep.setSequencia(1);
				listDep.add(dep);
			}

			if (countPai == 0) {
				DependentesManut dep = new DependentesManut();
				dep.setDependenteir(Dependentes.DEPENDENTEIR_2_NAO);
				dep.setIndHistorico(INT_0);
				dep.setGrau(Dependentes.GRAU_3_PAI);
				dep.setSequencia(2);
				listDep.add(dep);
			}
		}

		//		if (dependMae != null) {
		//			this.listaFilhacao.remove(dependMae);
		//		}
		//
		//		if (dependPae != null) {
		//			this.listaFilhacao.remove(dependPae);
		//		}
		//
		//		if (dependConjuge != null) {
		//			this.listaFilhacao.remove(dependConjuge);
		//		}
		// Necessário para manter a ordenação na tela em: Mãe, Pai e Conjuge
		//		if (dependMae == null) {
		//			dependMae = new DependentesManut();
		//			dependMae.setGrau(Dependentes.GRAU_4_MAE);
		//			dependMae.setSequencia(1);
		//			dependMae.setDependenteir(Dependentes.DEPENDENTEIR_2_NAO);
		//			dependMae.setIndHistorico(INT_0);
		//		}
		//		if (dependPae == null) {
		//			dependPae = new DependentesManut();
		//			dependPae.setSequencia(2);
		//			dependPae.setGrau(Dependentes.GRAU_3_PAI);
		//			dependPae.setDependenteir(Dependentes.DEPENDENTEIR_2_NAO);
		//			dependPae.setIndHistorico(INT_0);
		//		}
		//		this.listaFilhacao.add(dependMae);
		//		this.listaFilhacao.add(dependPae);
		if (base.getEc() != null) {
			if (base.getEc().equals(Ec.EC_2_CASADO) || this.isUniaoEstavel(base)) {
				if (conjuge == null) {
					conjuge = new DependentesManut();
					conjuge.setSequencia(3);
					conjuge.setGrau(base.getEc().equals(Ec.EC_2_CASADO) ? Dependentes.GRAU_1_CONJUGE : Dependentes.GRAU_14_COMPANHEIRO);
					conjuge.setDependenteir(Dependentes.DEPENDENTEIR_2_NAO);
					conjuge.setIndHistorico(INT_0);
					listDep.add(conjuge);
				} else {
					if (base.getEc().equals(Ec.EC_2_CASADO)) {
						conjuge.setGrau(Dependentes.GRAU_1_CONJUGE);
					} else if (this.isUniaoEstavel(base)) {
						conjuge.setGrau(Dependentes.GRAU_14_COMPANHEIRO);
					}
					//listDep.add(dependConjuge);
				}
			}
		}

		Collections.sort(listDep, new Comparator<DependentesManut>() {

			@Override
			public int compare(DependentesManut o1, DependentesManut o2) {
				return this.getPeso(o1.getGrau()).compareTo(this.getPeso(o2.getGrau()));
			}

			private Integer getPeso(Integer grau) {
				if (Dependentes.GRAU_4_MAE.equals(grau)) {
					return 1;
				} else if (Dependentes.GRAU_3_PAI.equals(grau)) {
					return 2;
				}
				return 3;
			}

		});

		this.listaFilhacao.clear();
		this.listaFilhacao.addAll(listDep);
		return this.listaFilhacao;
	}

	public List<FilhosManut> getListaFilhos() {
		if (this.listaFilhos == null) {
			this.listaFilhos = new ArrayList<FilhosManut>();
		}
		return this.listaFilhos;
	}

	public void setListaFilhos(List<FilhosManut> listaFilhos) {
		this.listaFilhos = listaFilhos;
	}

	public List<DependentesManut> getListaOutrosDependentes() {
		if (this.listaOutrosDependentes == null) {
			this.listaOutrosDependentes = new ArrayList<DependentesManut>();
		}
		return this.listaOutrosDependentes;
	}

	public void setListaOutrosDependentes(List<DependentesManut> listaOutrosDependentes) {
		this.listaOutrosDependentes = listaOutrosDependentes;
	}

	protected void carregaDependentesFuncionario(Integer empresa, Long matricula) throws ClassNotFoundException {
		this.setListaFilhacao(this.funcionariosSession.carregaFilhacao(this.getEjbVars(), empresa, matricula));
		MontaQuery q = new MontaQuery(FilhosManut.class, "sequencia");
		q.addWhere("empresa", "=", empresa);
		q.addWhere("matricula", "=", matricula);
		this.setListaFilhos(this.xcpQuerySession.executeQueryList(this.getEjbVars(), q));
		StringBuilder notIn = new StringBuilder();
		notIn.append("grau not in (");
		notIn.append(Dependentes.GRAU_1_CONJUGE);
		notIn.append(",");
		notIn.append(Dependentes.GRAU_3_PAI);
		notIn.append(",");
		notIn.append(Dependentes.GRAU_4_MAE);
		notIn.append(",");
		notIn.append(Dependentes.GRAU_14_COMPANHEIRO);
		notIn.append(")");
		q = new MontaQuery(DependentesManut.class, "sequencia");
		q.addWhere("empresa", "=", empresa);
		q.addWhere("matricula", "=", matricula);
		q.addWhere("(indHistorico = 0 or indHistorico is null)");
		q.addWhere("(dtafim is null or dtafim >= trunc(sysdate))");
		q.addWhere(notIn.toString());

		List<DependentesManut> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		for (DependentesManut d : lista) {
			MontaQuery qf = new MontaQuery(DependentesFil.class, "seq");
			qf.addWhere("empresa", "=", d.getEmpresa());
			qf.addWhere("matricula", "=", d.getMatricula());
			qf.addWhere("sequencia", "=", d.getSequencia());
			List<DependentesFil> listafil = this.xcpQuerySession.executeQueryList(this.getEjbVars(), qf);
			for (DependentesFil f : listafil) {
				if (DependentesFil.TIPO_1_PAI.equals(f.getTipo())) {
					d.setDepFilPai(f);
				} else if (DependentesFil.TIPO_2_MAE.equals(f.getTipo())) {
					d.setDepFilMae(f);
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append(" select 1 from dependentesanexos e ");
			sb.append("   where e.empresa = :empresa ");
			sb.append("   and e.matricula = :matricula ");
			sb.append("   and e.seqdependente = :sequencia ");

			Map<String, Object> map = new HashMap<>();
			map.put("empresa", d.getEmpresa());
			map.put("matricula", d.getMatricula());
			map.put("sequencia", d.getSequencia());

			d.setAnexosNaCarga(this.xcpQuerySession.existeNative(null, sb.toString(), map));

			XcpTabelaColunaVO vo = this.xcpTabelaColunaSession.findListaXcpObjetosPars(this.getEjbVars(), d);
			d.setXcpTabelaColunaVO(this.xcpTabelaColunaSession.buscaDadosColunas(this.getEjbVars(), vo, d));

		}
		this.setListaOutrosDependentes(lista);

		q = new MontaQuery(DependentesManut.class, "dtinicio desc");
		q.addWhere("empresa", "=", empresa);
		q.addWhere("matricula", "=", matricula);
		q.addWhere("indHistorico", "=", INT_1);
		q.addWhereFix(" and e.grau in (1,14)");
		this.setListaConjuges(this.xcpQuerySession.executeQueryList(this.getEjbVars(), q));
	}

	public String getLabelDepend(Integer grau) {
		return this.getTraducao("lbl_depend_" + grau);
	}

	public String getLabelColunas(Integer grau, String label) {
		if (grau.equals(Dependentes.GRAU_4_MAE)) {
			return label;
		}
		return null;
	}

	public Collection<SelectItem> getItensDependentesIRRF() {
		return this.getItensSemNull("dependenteir", Dependentes.DEPENDENTEIR_1_SIM, Dependentes.DEPENDENTEIR_2_NAO);
	}

	public Collection<SelectItem> getItensBeneficiarioPrev() {
		return this.getItensSemNull("beneficiarioprev", Dependentes.BENEFICIARIOPREV_2_NAO, Dependentes.BENEFICIARIOPREV_1_SIM);
	}

	public Collection<SelectItem> getItensSexo() {
		return this.getItens("sexo", Funcionarios.SEXO_F_FEMININO, Funcionarios.SEXO_M_MASCULINO);
	}

	public Collection<SelectItem> getItensCredencialSit() {
		return this.getItens(
				"credencialsit",
				Funcionarios.CREDENCIALSIT_1_REGULARIZADO,
				Funcionarios.CREDENCIALSIT_2_IRREGULAR,
				Funcionarios.CREDENCIALSIT_3_SUSPENSO,
				Funcionarios.CREDENCIALSIT_4_CASSADO,
				Funcionarios.CREDENCIALSIT_5_CANCELADO);
	}

	public Collection<SelectItem> getItensTipoContaPgto() {
		return this.getItens(
				"tipocontapgto",
				Funcionarios.TIPOCONTAPGTO_1_CONTA_CORRENTE,
				Funcionarios.TIPOCONTAPGTO_2_CONTA_POUPANCA,
				Funcionarios.TIPOCONTAPGTO_3_CONTA_SALARIO,
				Funcionarios.TIPOCONTAPGTO_4_CAIXA_FACIL);
	}

	public Collection<SelectItem> getItensTipoSalBase() {
		return this.getItens(
				"tiposalbase",
				Funcionarios.TIPOSALBASE_1_MENSAL,
				Funcionarios.TIPOSALBASE_2_SEMANAL,
				Funcionarios.TIPOSALBASE_3_TAREFA,
				Funcionarios.TIPOSALBASE_4_DIARIO,
				Funcionarios.TIPOSALBASE_5_HORISTA,
				Funcionarios.TIPOSALBASE_6_PLANTONISTA,
				Funcionarios.TIPOSALBASE_7_OUTROS);
	}

	public Collection<SelectItem> getItensAtestadoInternacao() {
		return this.getItens("atestadoInternacao", Atestados.INTERNACAO_1_SIM, Atestados.INTERNACAO_2_NAO);
	}

	public Collection<SelectItem> getItensAtestadoOrigem() {
		return this.getItens(
				"atestadoOrigem",
				Atestados.ORIGEM_1_EMPRESA,
				Atestados.ORIGEM_2_SINDICATO,
				Atestados.ORIGEM_3_PLANO_SAUDE,
				Atestados.ORIGEM_4_PARTICIPACAO,
				Atestados.ORIGEM_5_OUTROS);
	}

	public Collection<SelectItem> getItensAtestadoCaracteristica() {
		return this.getItens(
				"atestadoCaracteristica",
				Atestados.CARACTERISTICA_1_AC_TRABALHO,
				Atestados.CARACTERISTICA_2_AC_TRAJETO,
				Atestados.CARACTERISTICA_3_DOENCA_OCUP,
				Atestados.CARACTERISTICA_4_ATEST_MEDICO,
				Atestados.CARACTERISTICA_5_CONS_MEDICA);
	}

	public Collection<SelectItem> getItensCurso() {
		return this.getItens(
				"formacaoCurso",
				Formacao.CURSO_1_1_GRAU,
				Formacao.CURSO_2_2_GRAU,
				Formacao.CURSO_3_ESPECIALIZACAO,
				Formacao.CURSO_4_EXTENSAO,
				Formacao.CURSO_5_GRADUACAO,
				Formacao.CURSO_6_MESTRADO,
				Formacao.CURSO_7_DOUTORADO);
	}

	public Collection<SelectItem> getItensModalidadePg() {
		return this.getItens(
				"modalidadepg",
				Funcionarios.MODALIDADEPG_1_CHEQUE,
				Funcionarios.MODALIDADEPG_2_CREDITO_CONTA,
				Funcionarios.MODALIDADEPG_3_MOEDA,
				Funcionarios.MODALIDADEPG_4_ORDENS_PAG,
				Funcionarios.MODALIDADEPG_5_OUTROS);
	}

	public Collection<SelectItem> getItensTipoSalario() {
		return this.getItensSemNull(
				"tiposalario",
				Reajustes.TIPOSALARIO_1_MENSAL,
				Reajustes.TIPOSALARIO_2_SEMANAL,
				Reajustes.TIPOSALARIO_3_TAREFA,
				Reajustes.TIPOSALARIO_4_DIARIO,
				Reajustes.TIPOSALARIO_5_HORISTA,
				Reajustes.TIPOSALARIO_6_TAREFA,
				Reajustes.TIPOSALARIO_7_OUTROS);
	}

	protected String getDesPathDoc(Integer empresa, Long matricula, Integer tipDoc) {
		StringBuilder sb = new StringBuilder();
		sb.append("empresa=");
		sb.append(empresa);
		sb.append("|matricula=");
		sb.append(matricula);
		sb.append("|tipdocumento=");
		sb.append(tipDoc);
		sb.append("|detect=true");

		String path = sb.toString();
		sb = new StringBuilder();
		sb.append("/docpes?pars=");
		sb.append(Converter.toHex(path));
		return sb.toString();
	}

	public List<DependentesManut> getListaConjuges() {
		if (this.listaConjuges == null) {
			this.listaConjuges = new ArrayList<DependentesManut>();
		}
		return this.listaConjuges;
	}

	public void setListaConjuges(List<DependentesManut> listaConjuges) {
		this.listaConjuges = listaConjuges;
	}

	public boolean isExibirUniaoEstavel() {
		if (this.getExibirUniaoEstavel() == null) {
			try {
				this.setExibirUniaoEstavel(LONG_1.equals(this.getParametroLong("Funcionarios", 1)));
			} catch (Exception e) {
				this.setExibirUniaoEstavel(false);
			}
		}
		return this.getExibirUniaoEstavel();
	}

	public boolean isUniaoEstavel(FuncionariosBase func) {
		return Ec.EC_6_UNIAO_ESTAVEL.equals(func.getEc())
				|| (Util.in(func.getEc(), Ec.EC_1_SOLTEIRO, Ec.EC_3_SEPARADO, Ec.EC_4_VIUVO, Ec.EC_7_DIVORCIADO) && this.isExibirUniaoEstavel() && INT_1.equals(func.getUniaoestavel()));
	}

	public Boolean getExibirUniaoEstavel() {
		return this.exibirUniaoEstavel;
	}

	public void setExibirUniaoEstavel(Boolean exibirUniaoEstavel) {
		this.exibirUniaoEstavel = exibirUniaoEstavel;
	}

}
