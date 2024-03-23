package com.adm.xcp.backing;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.adm.ejb.entity.ClimaPesquisa;
import com.adm.ejb.entity.Funcionarios;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.Periodos;
import com.adm.ejb.entity.PontoAvaliacao;
import com.adm.ejb.entity.SetoresResponsaveis;
import com.adm.ejb.entity.SolicitacoesPortal;
import com.adm.ejb.entity.Vinculos;
import com.adm.ejb.entity.base.BoletimBase;
import com.adm.ejb.entity.extend.BoletimManut;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.PontoAjusteSessionBean;
import com.adm.ejb.session.local.FolhaSession;
import com.adm.ejb.session.remote.ClimaPesquisaSession;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.vos.AvisoDashboardVO;

/**
 * Session Bean implementation class AvisosDashboardSessionBean
 */
@ManagedBean
@SessionScoped
public class XcpAvisosDashboardBacking extends XcpAbstractBacking {

	public static final int ROTINA_PESQUISAS_CLIMA = 338;
	public static final int ROTINA_AVALIACOES = 725;
	public static final int ROTINA_AUTO_AVALIACOES = 1037;
	public static final int ROTINA_DEFESA_AVALIADOR = 1042;
	public static final int ROTINA_REVISAO = 1526;
	public static final int ROTINA_REVISAO_PDAP = 1604;
	public static final int ROTINA_MINHAS_AVALIACOES = 1607;
	public static final int ROTINA_RECURSO = 1050;
	public static final int ROTINA_ATUALIZACAO_CADASTRAL = 623;
	public static final int ROTINA_ESPELHO_PONTO = 707;
	public static final int ROTINA_AVAL_PARES = 1919;

	@EJB
	private FuncionariosSession funcionariosSession;
	@EJB
	private ClimaPesquisaSession pesquisaSession;
	@EJB
	private FolhaSession folhaSession;
	@EJB
	private PontoAjusteSessionBean session;

	private Map<Integer, AvisoDashboardVO> listaAvisosDashboard;

	public XcpAvisosDashboardBacking() {
		this.listaAvisosDashboard = new HashMap<Integer, AvisoDashboardVO>();
	}

	public void atualizarAvisoRotina(Integer codigoRotina) {
		try {
			this.loadAvisoRotina(codigoRotina);
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public AvisoDashboardVO getAvisoRotina(Integer codigoRotina) throws Exception {
		if (!this.listaAvisosDashboard.containsKey(codigoRotina)) {
			this.loadAvisoRotina(codigoRotina);
		}
		return this.listaAvisosDashboard.get(codigoRotina);
	}

	public Map<Integer, AvisoDashboardVO> getListaAvisosDashboard() throws Exception {
		if (this.listaAvisosDashboard.isEmpty()) {
			this.loadAvisosDashboard();
		}
		return this.listaAvisosDashboard;
	}

	private void loadAvisosDashboard() throws Exception {

		if (INT_1.equals(this.getSession().getUsuario().getConsignatario())) {
			return;
		}

		this.loadAvisoRotina(ROTINA_PESQUISAS_CLIMA);
		this.loadAvisoRotina(ROTINA_AVALIACOES);
		this.loadAvisoRotina(ROTINA_AUTO_AVALIACOES);
		this.loadAvisoRotina(ROTINA_DEFESA_AVALIADOR);
		this.loadAvisoRotina(ROTINA_REVISAO);
		this.loadAvisoRotina(ROTINA_REVISAO_PDAP);
		this.loadAvisoRotina(ROTINA_MINHAS_AVALIACOES);
		this.loadAvisoRotina(ROTINA_ATUALIZACAO_CADASTRAL);
		this.loadAvisoRotina(ROTINA_ESPELHO_PONTO);
		this.loadAvisoRotina(ROTINA_AVAL_PARES);
		//Nao carrega mais os avisos de recurso, conforme orientacao de ariel
		//		this.loadAvisoRotina(ROTINA_RECURSO);
	}

	private void loadAvisoRotina(Integer codigoRotina) throws Exception {
		switch (codigoRotina) {
		case ROTINA_PESQUISAS_CLIMA:
			this.listaAvisosDashboard.put(ROTINA_PESQUISAS_CLIMA, this.loadAvisoPesquisasClima());
			break;

		case ROTINA_AVALIACOES:
			this.listaAvisosDashboard.put(ROTINA_AVALIACOES, this.loadAvisoAvaliacoes());
			break;

		case ROTINA_AUTO_AVALIACOES:
			this.listaAvisosDashboard.put(ROTINA_AUTO_AVALIACOES, this.loadAvisoAutoAvaliacoes());
			break;

		case ROTINA_DEFESA_AVALIADOR:
			this.listaAvisosDashboard.put(ROTINA_DEFESA_AVALIADOR, this.loadAvisoDefesa());
			break;

		case ROTINA_REVISAO:
			this.listaAvisosDashboard.put(ROTINA_REVISAO, this.loadAvisoRevisao());
			break;

		case ROTINA_REVISAO_PDAP:
			this.listaAvisosDashboard.put(ROTINA_REVISAO_PDAP, this.loadAvisoRevisaoPdap());
			break;

		case ROTINA_MINHAS_AVALIACOES:
			this.listaAvisosDashboard.put(ROTINA_MINHAS_AVALIACOES, this.loadAvisoMinhasAvaliacoes());
			break;

		case ROTINA_RECURSO:
			this.listaAvisosDashboard.put(ROTINA_RECURSO, this.loadAvisoRecurso());
			break;

		case ROTINA_ATUALIZACAO_CADASTRAL:
			this.listaAvisosDashboard.put(
					ROTINA_ATUALIZACAO_CADASTRAL,
					this.loadAvisoAtualizacao(this.getSession().getCodEmpresa(), this.getSession().getUsuario().getMatricula()));
			break;
		case ROTINA_ESPELHO_PONTO:
			this.listaAvisosDashboard.put(
					ROTINA_ESPELHO_PONTO,
					this.loadAvisoEspelho(this.getSession().getCodEmpresa(), this.getSession().getUsuario().getMatricula()));
			break;
		case ROTINA_AVAL_PARES:
			this.listaAvisosDashboard.put(ROTINA_AVAL_PARES, this.loadAvisoPares());
		default:
			break;
		}
	}

	private AvisoDashboardVO loadAvisoPares() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaAvaliacoesPares(this.getEjbVars(), operador, false);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_AVAL_PARES, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s avaliação(ções) em par(es) para responder", aval.size()));
		}
		return aviso;
	}

	//	public AvisoDashboardVO loadAvisoAtualizacao() {
	//		
	//	}

	private Date truncDay(Date date) {
		Date datetrunc = Util.trunc(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetrunc);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public AvisoDashboardVO loadAvisoEspelho(Integer emp, Long mat) throws Exception {
		Long valor = null;
		try {
			valor = this.getParametroLong("EspelhopontoWeb", 4);
		} catch (Exception e) {
			valor = LONG_0;
		}

		if (LONG_1.equals(valor)) {
			Periodos per = this.folhaSession.buscaPeriodo(this.getEjbVars(), this.getCodEmpresa(), FolhaSession.TIPO_FOLHA_18_FOLHA_PONTO);

			StringBuilder sb = new StringBuilder();
			sb.append(" Select e from PontoManut e     			 ");
			sb.append(" where e.empresa   = :empresa   			 ");
			sb.append("   and e.matricula = :matricula 			 ");
			sb.append("   and e.tipopontoFk.indGeraPendPort = 1 ");
			sb.append("   and e.data between :dtini and :dtfim  ");
			//Se tem justificativa libera para todas as divergencias, libera
			sb.append("   and not exists (                      ");
			sb.append("	     Select 1 from PontoAvaliacao f     ");
			sb.append("		    where f.empresa 	= e.empresa     ");
			sb.append("		     and f.matricula = e.matricula   ");
			sb.append("		     and f.data 		= e.data        	  ");
			sb.append("		     and f.tipopontoOrig = e.tipoponto   ");
			//pendente
			sb.append("		     and f.situacao  =   					");
			sb.append(PontoAvaliacao.SITUACAO_0_INCLUIDO);
			sb.append("		) ");

			//Ainda nao vai ser testado isso
			//	sb.append("   and not exists (                      ");
			//	sb.append("	     Select 1 from AfdtAvaliacao f     ");
			//	sb.append("		    where f.empresa 	= e.empresa     ");
			//	sb.append("		     and f.matricula = e.matricula   ");
			//	sb.append("		     and f.datah 		= e.data        ");
			//	sb.append("		) ");

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("empresa", emp);
			map.put("matricula", mat);
			map.put("dtini", per.getDtinicio());
			map.put("dtfim", Util.nvl(per.getDtfim(), new Date()));

			List list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb.toString(), map);
			if (!Util.isEmpty(list)) {
				Long diamax = this.getParametroLong("EspelhopontoWeb", 7);
				if (diamax != null && diamax > 0) {
					if (this.session.isDentroLimite(this.getEjbVars(), per.getDtinicio(), diamax)) {
						AvisoDashboardVO aviso = new AvisoDashboardVO(ROTINA_ESPELHO_PONTO, "", "");
						aviso.setDestaqueAviso("1");
						aviso.setConteudoAviso(String.format("Você deve realizar as correções do espelho ponto"));
						aviso.setMsgListener(this.getTraducao("msg_bloqueado_atu_ponto"));
						return aviso;
					}
				} else {
					AvisoDashboardVO aviso = new AvisoDashboardVO(ROTINA_ESPELHO_PONTO, "", "");
					aviso.setDestaqueAviso("1");
					aviso.setConteudoAviso(String.format("Você deve realizar as correções do espelho ponto"));
					aviso.setMsgListener(this.getTraducao("msg_bloqueado_atu_ponto"));
					return aviso;
				}
			}

			/*
			 * Procura todos os setores em que a matricula logada é responsavel
			 */
			StringBuilder sb2 = new StringBuilder();
			sb2.append(" Select e from SetoresResponsaveis e ");
			sb2.append("  where (current_date between e.dtaini and e.dtafim ");
			sb2.append("			or (current_date > e.dtaini and e.dtafim is null)) ");
			sb2.append("  and e.matricula = :matricula ");
			sb2.append("  and e.empresa = :empresa ");

			map = new HashMap<String, Object>();
			map.put("matricula", mat);
			map.put("empresa", emp);

			List<SetoresResponsaveis> listResp = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb2.toString(), map);

			if (!Util.isEmpty(listResp)) {

				/*
				 * Verifica se algum subordinado nao tem alguma justificativa
				 * pendente de avaliacao
				 */
				StringBuilder sb3 = new StringBuilder();
				sb3.append(" Select 1 from PontoAvaliacao e, Funcionarios f  ");
				sb3.append("   where e.empresa 	  = f.empresa ");
				sb3.append("    and e.matricula 	  = f.matricula ");
				sb3.append("    and e.empresa 	  = :empresa     	 	 ");
				sb3.append("    and f.setor        = :setor   				 ");
				sb3.append("    and e.data between :dtini and :dtfim ");
				sb3.append("    and e.situacao     = :situacao   		 ");

				for (SetoresResponsaveis s : listResp) {

					map = new HashMap<String, Object>();
					map.put("empresa", emp);
					map.put("setor", s.getSetor());
					map.put("dtini", per.getDtinicio());
					map.put("dtfim", Util.nvl(per.getDtfim(), new Date()));
					map.put("situacao", PontoAvaliacao.SITUACAO_0_INCLUIDO);

					List listPend = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb3.toString(), map);
					if (!Util.isEmpty(listPend)) {
						AvisoDashboardVO aviso = new AvisoDashboardVO(ROTINA_ESPELHO_PONTO, "", "");
						aviso.setDestaqueAviso("1");
						aviso.setConteudoAviso(String.format("Você deve realizar as verificacoes do espelho ponto"));
						aviso.setMsgListener(this.getTraducao("msg_bloqueado_atu_ocor"));
						return aviso;
					}
				}
			}
		}
		return null;
	}

	public AvisoDashboardVO loadAvisoAtualizacao(Integer emp, Long mat) {

		boolean atualiza = true;
		if (mat == null) {
			atualiza = false;
		} else {
			FuncionariosPK pk = new FuncionariosPK();
			pk.setEmpresa(emp);
			pk.setMatricula(mat);
			Funcionarios f = this.xcpQuerySession.find(this.getEjbVars(), Funcionarios.class, pk);
			if (f.getVinculo() != null) {
				Vinculos v = this.xcpQuerySession.find(this.getEjbVars(), Vinculos.class, f.getVinculo());
				if (v.getRecadobrig() == null || INT_0.equals(v.getRecadobrig())) {
					atualiza = false;
				} else if (f.getDtrecadastramento() != null) {
					//Se esta nulo, forca a atualizacao
					/*
					 * Procura o proximo aniversario que ocorreu apos o
					 * ultimo recadastramento. Se a data atual for superior a
					 * esse, significa que tem que refazer o recadastarmentro
					 */
					Calendar calRecad = Calendar.getInstance();
					calRecad.setTime(f.getDtrecadastramento());

					Calendar calProxAni = Calendar.getInstance();
					calProxAni.setTime(Util.trunc(f.getDtnascimento()));
					calProxAni.set(Calendar.DAY_OF_MONTH, 1);
					calProxAni.set(Calendar.YEAR, calRecad.get(Calendar.YEAR));

					if (calRecad.getTime().compareTo(calProxAni.getTime()) > 0) {
						//o aniversario que conta eh ano que vem
						calProxAni.set(Calendar.YEAR, calRecad.get(Calendar.YEAR) + 1);
					}

					if (this.truncDay(new Date()).compareTo(calProxAni.getTime()) < 0) {
						atualiza = false;
					}
				}
			} else {
				atualiza = false;
			}
		}

		if (atualiza) {
			MontaQuery q = new MontaQuery("Select e from SolicitacoesPortal e");
			q.addWhere("empresa", "=", emp);
			q.addWhere("matricula", "=", mat);
			q.addWhere("situacao", "=", SolicitacoesPortal.SITUACAO_1_SOLICITA);
			List list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);

			if (!Util.isEmpty(list)) {
				AvisoDashboardVO aviso = new AvisoDashboardVO(ROTINA_ATUALIZACAO_CADASTRAL, "", "");
				SolicitacoesPortal sol = (SolicitacoesPortal) list.get(0);
				if (INT_1.equals(sol.getPendencia())) {
					aviso.setDestaqueAviso("Pendente");
					aviso.setConteudoAviso(String.format("Complete as informações conforme mensagem recebida!"));
				} else {
					aviso.setDestaqueAviso("Em análise");
					aviso.setConteudoAviso(String.format("Sua atualização está em análise!"));
				}
				return aviso;
			}

			AvisoDashboardVO aviso = new AvisoDashboardVO(ROTINA_ATUALIZACAO_CADASTRAL, "", "");
			aviso.setDestaqueAviso("1");
			aviso.setConteudoAviso(String.format("Você deve realizar a atualizacao cadastral!"));
			return aviso;
		}

		return null;
	}

	private AvisoDashboardVO loadAvisoPesquisasClima() {
		AvisoDashboardVO aviso = null;
		List<ClimaPesquisa> pesquisas = null;
		String totalPesquisas;

		pesquisas = this.pesquisaSession.buscaPesquisas(this.getEjbVars(), this.getSession().getCodEmpresa(), this.getSession().getUsuario().getMatricula(), null);
		if (!Util.isEmpty(pesquisas)) {
			aviso = new AvisoDashboardVO(ROTINA_PESQUISAS_CLIMA, "", "");
			totalPesquisas = String.valueOf(pesquisas.size());

			aviso.setDestaqueAviso(totalPesquisas);
			aviso.setConteudoAviso("Você possui " + totalPesquisas
					+ (pesquisas.size() == 1 ? " Pesquisa de Clima para responder" : " Pesquisas de Clima para responder"));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoAvaliacoes() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimBase> aval = this.funcionariosSession.buscaAvaliacoes(this.getEjbVars(), operador, false);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_AVALIACOES, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s avaliação(ões) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoAutoAvaliacoes() {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaAutoAvaliacoes(this.getEjbVars(), operador, false);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_AUTO_AVALIACOES, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s auto-avaliação(ões) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoDefesa() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaDefesaAvaliador(this.getEjbVars(), operador);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_DEFESA_AVALIADOR, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s defesa(s) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoRecurso() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaRecursoAvaliado(this.getEjbVars(), operador);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_RECURSO, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s recurso(s) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoRevisao() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaRevisoes(this.getEjbVars(), operador);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_REVISAO, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s revisão(ões) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoRevisaoPdap() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaRevisoesPdap(this.getEjbVars(), operador);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_REVISAO_PDAP, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s revisão(ões) para responder", aval.size()));
		}
		return aviso;
	}

	private AvisoDashboardVO loadAvisoMinhasAvaliacoes() throws Exception {
		AvisoDashboardVO aviso = null;
		Operadores operador = this.getSession().getUsuario();

		List<BoletimManut> aval = this.funcionariosSession.buscaCountMinhasAvaliacoes(this.getEjbVars(), operador);
		if (!Util.isEmpty(aval)) {
			aviso = new AvisoDashboardVO(ROTINA_MINHAS_AVALIACOES, "", "");
			aviso.setDestaqueAviso(Converter.toString(aval.size()));
			aviso.setConteudoAviso(String.format("Você possui %s avaliacão(ões) para dar ciência", aval.size()));
		}
		return aviso;
	}
}
