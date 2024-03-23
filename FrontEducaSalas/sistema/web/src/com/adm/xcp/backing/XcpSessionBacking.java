package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import com.adm.ejb.entity.Empresas;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.Operadoresperfis;
import com.adm.ejb.entity.PerfisRest;
import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.entity.XcpRestricoes;
import com.adm.ejb.entity.extend.FavoritosRotinas;
import com.adm.ejb.session.XcpIntegracaoSingleton;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.vo.ChaveVO;
import com.adm.ejb.vo.LoginLogoutVO;
import com.adm.ejb.vo.RestricoesVO;
import com.adm.gui.vo.PlainMenuVO;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.vo.XcpEjbVars;
import com.adm.xcp.vos.MenuAcessoVO;

@ManagedBean
@SessionScoped
public class XcpSessionBacking extends XcpAbstractBacking {

	public static final String ID = "xcpSessionBacking";

	@EJB
	protected OperadoresSession operadoresSession;

	@EJB
	private XcpPersistSession xcpPersistSession;

	@EJB
	private XcpIntegracaoSingleton xcpIntegracaoSingleton;

	private Map<Integer, MenuAcessoVO> listaRotinasAcessiveis = new HashMap<Integer, MenuAcessoVO>();
	private Map<String, MenuAcessoVO> listaAcessos = new HashMap<String, MenuAcessoVO>();
	private Operadores usuario;
	private Empresas empresa;
	private MenuModel menuFav;
	private String currentViewId;
	private Integer tipJanela;
	private Integer numTableRows;
	private Collection<SelectItem> listaItensComboEmpresas;
	private List<Empresas> listaEmpresasOperador;
	private List<Operadoresperfis> listaPerfisOperador;
	private Sessoes sessao;
	private String restricaoOrgao;
	private String restricaoDivisao;
	private String restricaoSetor;
	private String restricaoVinculo;
	private Integer mostraFuncDisp;
	private RestricoesVO restricoes;
	private boolean exibiuMsg;
	private List<PlainMenuVO> listplain;
	private ChaveVO chaveADM;
	private String restricaoHistorico;
	private String restricaoPublicacoes;
	private String restricaoPonto;
	private String restricaoAfast;
	private String restricaoAverb;
	private String restricaoAut;
	private String restricaoPar;

	private String restricaoTransfSetor;
	private String restricaoTransfFuncao;
	private String restricaoTransfFg;
	private String restricaoTransfHorario;
	private String restricaoTransfCentro;
	private String restricaoTransfCargos;
	private String restricaoTransfPadroes;
	private String restricaoTransfNiveis;
	private String restricaoTransfNomes;
	private String restricaoTransfAmp;
	private String restricaoTransfSubs;
	private String restricaoFerias;
	private String restricaoLicenca;
	private String restricaoComissao;
	private String restricaoAssist;

	//VO que e preenchido
	private LoginLogoutVO loginLogoutVO;

	private Map<String, Object> navigationMap = new HashMap<String, Object>();

	public List<Empresas> getListaEmpresasOperador() {
		return this.listaEmpresasOperador;
	}

	public Collection<SelectItem> getItensComboEmpresas() {
		if (this.listaItensComboEmpresas == null) {
			if (this.listaEmpresasOperador == null) {
				return null;
			}
			this.listaItensComboEmpresas = new ArrayList<SelectItem>();
			for (Empresas empresa : this.listaEmpresasOperador) {
				this.listaItensComboEmpresas.add(new SelectItem(empresa.getEmpresa(), empresa.getNome()));
			}
		}
		return this.listaItensComboEmpresas;
	}

	public void addMenuAcesso(MenuAcessoVO vo) {
		String componente = vo.getComponente();
		String codObjeto = vo.getCodObjeto();
		if (!this.listaRotinasAcessiveis.containsKey(vo.getRotina())) {
			this.listaRotinasAcessiveis.put(vo.getRotina(), vo);
		}
		if (Util.isEmpty(codObjeto)) {
			//Se não tem objeto adiciona a permissao para a tela
			this.listaAcessos.put(componente, vo);
		} else {
			//Se tem objeto e é a tela de execucao - adiciona permissao para o objeto 
			if (componente.equals(PG_EXECUCAO_OBJETO_COMPONENTE)) {
				this.listaAcessos.put(String.format("%s:%s", componente, codObjeto), vo);
			} else {
				//Se tem objeto e NÃO é a tela de execucao - adiciona permissao para o objeto e para a tela
				this.listaAcessos.put(String.format("%s:%s", PG_EXECUCAO_OBJETO_COMPONENTE, codObjeto), vo);
				this.listaAcessos.put(componente, vo);
			}
		}
	}

	public MenuAcessoVO getMenuAcesso(String url, String codObjeto) {
		String componente = url.replace("/secure/", "");
		if (componente.equals(PG_EXECUCAO_OBJETO_COMPONENTE) && !Util.isEmpty(codObjeto)) {
			return this.listaAcessos.get(String.format("%s:%s", componente, codObjeto));
		} else {
			return this.listaAcessos.get(componente);
		}
	}

	public boolean isAcessivel(String url, String codObjeto) {
		if (url == null) {
			return false;
		}
		if (!url.contains("/secure/")) {
			return false;
		}
		if (url.contains("XcpTraducaoSmart") || url.contains("AlteraSenhaForm")) {
			return true;
		}

		if (this.getMenuAcesso(url, codObjeto) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Retorna os dados para abertura da janela, caso não tenha acesso
	 * retornará null
	 * 
	 * @param rotina
	 * @return MenuAcessoVO
	 */
	public MenuAcessoVO getMenuAcesso(Integer rotina) {
		if (this.listaRotinasAcessiveis.containsKey(rotina)) {
			return this.listaRotinasAcessiveis.get(rotina);
		}
		return null;
	}

	private String montaRestricao(List<?> restricao) {
		StringBuilder sb = new StringBuilder();
		for (Object res : restricao) {
			sb.append(res.toString());
			sb.append(", ");
		}
		String ret = sb.toString();
		if (ret.length() < 1) {
			return null;
		}
		return ret.substring(0, ret.length() - 2);
	}

	public RestricoesVO getRestricoes() {
		return this.restricoes;
	}

	public String getRestricaoDivisao() {
		return this.restricaoDivisao;
	}

	public String getRestricaoOrgao() {
		return this.restricaoOrgao;
	}

	public String getRestricaoSetor() {
		return this.restricaoSetor;
	}

	public String getRestricaoVinculo() {
		return this.restricaoVinculo;
	}

	public Sessoes registraLogin(Operadores usuario, Empresas empresa, List<Empresas> listaEmpresasValidas, Sessoes sessao) {
		this.usuario = usuario;
		this.empresa = empresa;
		this.listaEmpresasOperador = listaEmpresasValidas;
		this.listaPerfisOperador = this.operadoresSession.perfisLiberados(this.getEjbVars(), usuario.getOperador());

		List<Integer> listHist = new ArrayList<Integer>();
		List<Integer> listPub = new ArrayList<Integer>();
		List<Integer> listPonto = new ArrayList<Integer>();
		List<Integer> listAfast = new ArrayList<Integer>();
		List<Integer> listAverb = new ArrayList<Integer>();
		List<Integer> listAut = new ArrayList<Integer>();
		List<Integer> listPar = new ArrayList<Integer>();

		List<Long> listTransfSetor = new ArrayList<Long>();
		List<Long> listTransfFuncoes = new ArrayList<Long>();
		List<Long> listTransfFG = new ArrayList<Long>();
		List<Long> listTransfHor = new ArrayList<Long>();
		List<Long> listTransfCentro = new ArrayList<Long>();
		List<Long> listTransfCargo = new ArrayList<Long>();
		List<Long> listTransfPadroes = new ArrayList<Long>();
		List<Long> listTransfNiveis = new ArrayList<Long>();
		List<Long> listTransfNomes = new ArrayList<Long>();
		List<Long> listTransfAmpliacao = new ArrayList<Long>();
		List<Long> listTransfSubs = new ArrayList<Long>();

		List<Integer> listFer = new ArrayList<Integer>();
		List<Integer> listLic = new ArrayList<Integer>();
		List<Integer> listCom = new ArrayList<Integer>();
		List<Integer> listAssist = new ArrayList<Integer>();

		if (!Util.isEmpty(this.listaPerfisOperador)) {
			for (Operadoresperfis o : this.listaPerfisOperador) {
				MontaQuery q = new MontaQuery("Select e from PerfisRest e");
				q.addWhere("perfil", "=", o.getPerfil());
				List<PerfisRest> listRest = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
				for (PerfisRest r : listRest) {
					if (PerfisRest.TIPO_1_HISTORICO.equals(r.getTipo())) {
						listHist.add(r.getCodHist());
					} else if (PerfisRest.TIPO_2_PONTO.equals(r.getTipo())) {
						listPonto.add(r.getCodPonto());
					} else if (PerfisRest.TIPO_3_AFASTAMENTO.equals(r.getTipo())) {
						listAfast.add(r.getCodAfast());
					} else if (PerfisRest.TIPO_4_AVERBACAO.equals(r.getTipo())) {
						listAverb.add(r.getCodAver());
					} else if (PerfisRest.TIPO_5_AUTORIZACAO.equals(r.getTipo())) {
						listAut.add(r.getCodAut());
					} else if (PerfisRest.TIPO_6_PARAMETROS.equals(r.getTipo())) {
						listPar.add(r.getCodGrupoPar());
					} else if (PerfisRest.TIPO_7_PUBLICACOES.equals(r.getTipo())) {
						listPub.add(r.getCodHist());
					} else if (PerfisRest.TIPO_8_TRANSFERENCIAS.equals(r.getTipo())) {

						Long valor = r.getCodMot();
						if (valor == null) {
							valor = Converter.toLong(r.getCodReaj());
						}

						if (PerfisRest.TIPOABA_1_SETORES.equals(r.getTipoaba())) {
							listTransfSetor.add(valor);
						} else if (PerfisRest.TIPOABA_2_FUNCOES.equals(r.getTipoaba())) {
							listTransfFuncoes.add(valor);
						} else if (PerfisRest.TIPOABA_3_FG.equals(r.getTipoaba())) {
							listTransfFG.add(valor);
						} else if (PerfisRest.TIPOABA_4_HORARIOS.equals(r.getTipoaba())) {
							listTransfHor.add(valor);
						} else if (PerfisRest.TIPOABA_5_CENTRODECUSTO.equals(r.getTipoaba())) {
							listTransfCentro.add(valor);
						} else if (PerfisRest.TIPOABA_6_CARGOS.equals(r.getTipoaba())) {
							listTransfCargo.add(valor);
						} else if (PerfisRest.TIPOABA_7_PADROES.equals(r.getTipoaba())) {
							listTransfPadroes.add(valor);
						} else if (PerfisRest.TIPOABA_8_NIVEIS.equals(r.getTipoaba())) {
							listTransfNiveis.add(valor);
						} else if (PerfisRest.TIPOABA_9_NOMES.equals(r.getTipoaba())) {
							listTransfNomes.add(valor);
						} else if (PerfisRest.TIPOABA_11_AMPLIACAO.equals(r.getTipoaba())) {
							listTransfAmpliacao.add(valor);
						} else if (PerfisRest.TIPOABA_12_SUBSTITUICOES.equals(r.getTipoaba())) {
							listTransfSubs.add(valor);
						}

					} else if (PerfisRest.TIPO_9_FERIAS.equals(r.getTipo())) {
						listFer.add(r.getCodFerias());
					} else if (PerfisRest.TIPO_10_LICENCAPREMIO.equals(r.getTipo())) {
						listLic.add(r.getCodLic());
					} else if (PerfisRest.TIPO_11_COMISSAO.equals(r.getTipo())) {
						listCom.add(r.getCodCom());
					} else if (PerfisRest.TIPO_12_ATENDIMENTO_MEDICO.equals(r.getTipo())) {
						listAssist.add(r.getCodAssist());
					}
				}
			}
		}

		this.restricaoHistorico = this.montaRestricao(listHist);
		this.restricaoPublicacoes = this.montaRestricao(listPub);
		this.restricaoPonto = this.montaRestricao(listPonto);
		this.restricaoAfast = this.montaRestricao(listAfast);
		this.restricaoAverb = this.montaRestricao(listAverb);
		this.restricaoAut = this.montaRestricao(listAut);
		this.restricaoPar = this.montaRestricao(listPar);

		this.restricaoTransfSetor = this.montaRestricao(listTransfSetor);
		this.restricaoTransfFuncao = this.montaRestricao(listTransfFuncoes);
		this.restricaoTransfFg = this.montaRestricao(listTransfFG);
		this.restricaoTransfHorario = this.montaRestricao(listTransfHor);
		this.restricaoTransfCentro = this.montaRestricao(listTransfCentro);
		this.restricaoTransfCargos = this.montaRestricao(listTransfCargo);
		this.restricaoTransfPadroes = this.montaRestricao(listTransfPadroes);
		this.restricaoTransfNiveis = this.montaRestricao(listTransfNiveis);
		this.restricaoTransfNomes = this.montaRestricao(listTransfNomes);
		this.restricaoTransfAmp = this.montaRestricao(listTransfAmpliacao);
		this.restricaoTransfSubs = this.montaRestricao(listTransfSubs);

		this.restricaoFerias = this.montaRestricao(listFer);
		this.restricaoLicenca = this.montaRestricao(listLic);
		this.restricaoComissao = this.montaRestricao(listCom);
		this.restricaoAssist = this.montaRestricao(listAssist);

		sessao.setEmpresa(empresa.getEmpresa());
		sessao = this.xcpPersistSession.update(this.getEjbVars(), sessao);
		this.sessao = sessao;

		RestricoesVO restricoes = this.operadoresSession.buscaRestricoesOperador(this.getEjbVars(), usuario.getOperador());
		this.mostraFuncDisp = restricoes.getMostraFuncDisp();
		this.restricaoDivisao = this.montaRestricao(restricoes.getDivisao());
		this.restricaoOrgao = this.montaRestricao(restricoes.getOrgaos());
		this.restricaoSetor = this.montaRestricao(restricoes.getSetor());
		this.restricaoVinculo = this.montaRestricao(restricoes.getVinculo());
		this.restricoes = restricoes;

		XcpRestricoes ett = this.xcpQuerySession.find(this.getEjbVars(), XcpRestricoes.class, usuario.getOperador());
		if (ett == null) {
			ett = new XcpRestricoes();
		}

		ett.setOperador(usuario.getOperador());
		ett.setRestricaoOrgao(this.restricaoOrgao);
		ett.setRestricaoDivisao(this.restricaoDivisao);
		ett.setRestricaoSetor(this.restricaoSetor);
		this.xcpPersistSession.update(this.getEjbVars(), ett);

		//Adiciona o usuario na lista de usuarios logados!
		XcpApplicationBacking xcpApplicationBacking = getBacking(XcpApplicationBacking.class);
		xcpApplicationBacking.getUsuariosLogados().add(sessao);
		return sessao;
	}

	public List<Operadoresperfis> getListaPerfisOperador() {
		return this.listaPerfisOperador;
	}

	public Operadores getUsuario() {
		return this.usuario;
	}

	public Empresas getEmpresa() {
		return this.empresa;
	}

	public Integer getCodEmpresaCombo() {
		return this.getEmpresa().getEmpresa();
	}

	public void setCodEmpresaCombo(Integer codEmpresa) {
		if (codEmpresa == null) {
			return;
		}
		if (this.getEmpresa() == null) {
			return;
		}
		if (!codEmpresa.equals(this.getEmpresa().getEmpresa())) {
			for (Empresas empresa : this.listaEmpresasOperador) {
				if (empresa.getEmpresa().equals(codEmpresa)) {
					this.empresa = empresa;
					Sessoes sessao = this.getSessao();
					sessao.setEmpresa(this.getCodEmpresa());
					sessao = this.xcpPersistSession.update(this.getEjbVars(), sessao);
					this.sessao = sessao;
					break;
				}
			}
		}
	}

	public boolean isLogged() {
		return this.getUsuario() != null;
	}

	public MenuModel getMenuFav() {
		if (this.menuFav == null) {
			this.menuFav = new DefaultMenuModel();
			MontaQuery m = new MontaQuery("Select e from FavoritosRotinas e");
			m.addWhere("operador", "=", this.getSession().getUsuario().getOperador());
			m.setOrderBy("e.rotinasFk.descricao");
			List<FavoritosRotinas> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), m);

			for (FavoritosRotinas fr : list) {
				Rotinas rotinasFk = fr.getRotinasFk();
				DefaultMenuItem item = new DefaultMenuItem(rotinasFk.getDescricao());
				item.setImmediate(true);

				String tela = rotinasFk.getComponente();

				//Se nao tem componente é uma execucao de objeto
				if (Util.isEmpty(tela)) {
					tela = PG_EXECUCAO_OBJETO_COMPONENTE;
				}
				String codObjeto = "";
				if (tela.equals(PG_EXECUCAO_OBJETO_COMPONENTE) && rotinasFk.getCodObjeto() != null) {
					codObjeto = rotinasFk.getCodObjeto();
				}

				item.setOnclick(String.format("return XcpMenu.openPage('%s', '%s', '%s');return false;", tela, codObjeto, rotinasFk.getDescricao()));

				this.menuFav.addElement(item);
			}
		}

		return this.menuFav;
	}

	public void setMenuFav(MenuModel menuFav) {
		this.menuFav = menuFav;
	}

	public Integer getTableRows() {
		if (this.numTableRows == null) {
			if (this.getUsuario() != null && this.getUsuario().getLinhas() != null) {
				this.numTableRows = this.getUsuario().getLinhas();
			} else {
				this.numTableRows = 10;
			}
		}

		return this.numTableRows;
	}

	public void setTableRows(Integer numTableRows) {
		this.numTableRows = numTableRows;
	}

	public Integer getTipJanela() {
		if (this.tipJanela == null) {
			if (this.getUsuario() != null && this.getUsuario().getJanela() != null) {
				this.tipJanela = this.getUsuario().getJanela();
			} else {
				this.tipJanela = Operadores.TIP_JANELA_1_MESMA;
			}
		}

		return this.tipJanela;
	}

	public void setTipJanela(Integer tipJanela) {
		this.tipJanela = tipJanela;
	}

	public void actionSalvarGlobalConf() {
		if (this.getUsuario() != null) {
			this.getUsuario().setJanela(this.tipJanela);
			this.getUsuario().setLinhas(this.numTableRows);
			if (this.getUsuario().getOperador() != null) {
				XcpEjbVars ejbVars = this.getEjbVars();
				ejbVars.getSystemVars().put(XcpEjbVars.ROTINA, Rotinas.HOME_952);
				this.operadoresSession.gravaLinhasConsulta(ejbVars, this.getUsuario().getOperador(), this.getTableRows());
				this.operadoresSession.gravaJanela(ejbVars, this.getUsuario().getOperador(), this.getTipJanela());
			}
		}
		if (Operadores.TIP_JANELA_4_MULTI.equals(this.tipJanela)) {
			this.navigateTo(PG_HOME);
		}
	}

	public boolean isRenderEmpresas() {
		if (this.listaEmpresasOperador != null) {
			if (this.listaEmpresasOperador.size() > 1) {
				return true;
			}
		}
		return false;
	}

	public String getCurrentViewId() {
		return this.currentViewId;
	}

	public void setCurrentViewId(String currentViewId) {
		this.currentViewId = currentViewId;
	}

	public LoginLogoutVO getLoginLogoutVO() {
		return this.loginLogoutVO;
	}

	public void setLoginLogoutVO(LoginLogoutVO loginLogoutVO) {
		this.loginLogoutVO = loginLogoutVO;
	}

	public Collection<SelectItem> getItensTipJanela() {
		return this.getItensSemNull(
				"tipJanela",
				Operadores.TIP_JANELA_1_MESMA,
				Operadores.TIP_JANELA_2_POPUP,
				Operadores.TIP_JANELA_3_ABA,
				Operadores.TIP_JANELA_4_MULTI);
	}

	public Object getNavigationParam(String key) {
		return this.navigationMap.remove(key);
	}

	public void putNavigationParam(String key, Object obj) {
		this.navigationMap.put(key, obj);
	}

	public Sessoes getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessoes sessao) {
		this.sessao = sessao;
	}

	public boolean isTraducao() {
		// "XcpTraducaoForm.xhtml"
		MenuAcessoVO acesso = this.getMenuAcesso(Integer.valueOf("509"));
		if (acesso == null) {
			return false;
		}
		return true;

	}

	public boolean isExibiuMsg() {
		return this.exibiuMsg;
	}

	public void setExibiuMsg(boolean exibiuMsg) {
		this.exibiuMsg = exibiuMsg;
	}

	/**
	 * Para buscar um mapa de configs com chaves de um domínio comum.
	 * Por exemplo, de uma tela específica. Ex: calculosForm.opcao1,
	 * calculosForm.opcao2
	 * 
	 * @param chave
	 * @return Mapa com a(s) lista(s) de configurações
	 */
	public Map<String, List<String>> getMapConfigOperador(String chave) {
		return this.operadoresSession.buscaConfigOperador(this.getEjbVars(), this.getUsuario().getOperador(), chave);
	}

	/**
	 * Para buscar uma lista de valores de uma chave específica.
	 * Valores serão retornados como String.
	 * 
	 * @param chave
	 * @return Lista com os valores de uma chave específica
	 */
	public List<String> getConfigOperadorAsList(String chave) {
		Map<String, List<String>> listaConfig = this.getMapConfigOperador(chave);
		if (listaConfig.isEmpty()) {
			return new ArrayList<String>();
		}
		return listaConfig.get(chave);
	}

	/**
	 * Para buscar um único valor de configuração como String
	 * 
	 * @param chave
	 * @return Valor de uma chave como String
	 */
	public String getConfigOperadorAsString(String chave) {
		List<String> listaValores = this.getConfigOperadorAsList(chave);
		if (listaValores.isEmpty()) {
			return null;
		}
		return listaValores.get(0);
	}

	/**
	 * Para buscar um único valor de configuração como Integer
	 * 
	 * @param chave
	 * @return Valor de uma chave como Integer
	 */
	public Integer getConfigOperadorAsInteger(String chave) {
		return Integer.parseInt(this.getConfigOperadorAsString(chave));
	}

	/**
	 * Grava a lista de valores como uma configuração do operador
	 * 
	 * @param chave
	 * @param listaValores
	 */
	public void gravaConfigOperador(String chave, List<String> listaValores) {
		this.operadoresSession.gravaConfigOperador(this.getEjbVars(), this.getSessao().getOperador(), chave, listaValores);
	}

	/**
	 * Grava o valor como uma configuração do operador
	 * 
	 * @param chave
	 * @param valor
	 */
	public void gravaConfigOperador(String chave, String valor) {
		this.operadoresSession.gravaConfigOperador(this.getEjbVars(), this.getSessao().getOperador(), chave, valor);
	}

	public List<PlainMenuVO> getListplain() {
		return this.listplain;
	}

	public void setListplain(List<PlainMenuVO> listplain) {
		this.listplain = listplain;
	}

	public ChaveVO getChaveADM() {
		if (this.chaveADM == null) {
			this.chaveADM = new ChaveVO();
			this.chaveADM.setDesChave(UUID.randomUUID().toString());
			this.chaveADM.setDesUsuario(Converter.toString(this.getUsuario().getNome()));
			this.chaveADM.setDtaCheck(new Date());
			this.xcpIntegracaoSingleton.put(this.getEjbVars(), this.chaveADM);
		}
		return this.chaveADM;
	}

	public String getRestricaoPublicacoes() {
		return this.restricaoPublicacoes;
	}

	public String getRestricaoHistorico() {
		return this.restricaoHistorico;
	}

	public String getRestricaoPonto() {
		return this.restricaoPonto;
	}

	public String getRestricaoAverb() {
		return this.restricaoAverb;
	}

	public String getRestricaoAfast() {
		return this.restricaoAfast;
	}

	public String getRestricaoAut() {
		return this.restricaoAut;
	}

	public String getRestricaoPar() {
		return this.restricaoPar;
	}

	public Integer getMostraFuncDisp() {
		return this.mostraFuncDisp;
	}

	public String getRestricaoTransfHorario() {
		return this.restricaoTransfHorario;
	}

	public void setRestricaoTransfHorario(String restricaoTransfHorario) {
		this.restricaoTransfHorario = restricaoTransfHorario;
	}

	public String getRestricaoTransfSetor() {
		return this.restricaoTransfSetor;
	}

	public String getRestricaoTransfFuncao() {
		return this.restricaoTransfFuncao;
	}

	public String getRestricaoTransfFg() {
		return this.restricaoTransfFg;
	}

	public String getRestricaoTransfCentro() {
		return this.restricaoTransfCentro;
	}

	public String getRestricaoTransfCargos() {
		return this.restricaoTransfCargos;
	}

	public String getRestricaoTransfPadroes() {
		return this.restricaoTransfPadroes;
	}

	public String getRestricaoTransfNomes() {
		return this.restricaoTransfNomes;
	}

	public String getRestricaoTransfAmp() {
		return this.restricaoTransfAmp;
	}

	public String getRestricaoFerias() {
		return this.restricaoFerias;
	}

	public String getRestricaoLicenca() {
		return this.restricaoLicenca;
	}

	public String getRestricaoComissao() {
		return this.restricaoComissao;
	}

	public String getRestricaoTransfNiveis() {
		return this.restricaoTransfNiveis;
	}

	public String getRestricaoTransfSubs() {
		return this.restricaoTransfSubs;
	}

	public String getRestricaoAssist() {
		return this.restricaoAssist;
	}

	public void setRestricaoAssist(String restricaoAssist) {
		this.restricaoAssist = restricaoAssist;
	}

}
