package com.adm.xcp.backing;

import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import com.adm.ejb.entity.Acessos;
import com.adm.ejb.entity.Empresas;
import com.adm.ejb.entity.Funcionarios;
import com.adm.ejb.entity.Ldap;
import com.adm.ejb.entity.Menus;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.Operadoresperfis;
import com.adm.ejb.entity.Permissao;
import com.adm.ejb.entity.RegraSenha;
import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.entity.XcpLogLogin;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.XcpServerCacheSessionBean;
import com.adm.ejb.session.local.XcpServerEmailSession;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.util.XcpMonitoraCache;
import com.adm.ejb.view.AcessofuncView;
import com.adm.ejb.vo.LoginLogoutVO;
import com.adm.ejb.vo.XcpSendEmailVO;
import com.adm.gui.vo.PlainMenuVO;
import com.adm.util.Constants;
import com.adm.util.Converter;
import com.adm.util.DataUtil;
import com.adm.util.Formatter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.exceptions.MensagemException;
import com.adm.util.vo.XcpEjbVars;
import com.adm.xcp.listener.ICPBrasilCertReader;
import com.adm.xcp.servlet.AzureServlet;
import com.adm.xcp.util.IpAddressUtil;
import com.adm.xcp.util.XcpPermissaoAcaoCache;
import com.adm.xcp.util.XcpTraducaoCache;
import com.adm.xcp.util.XcpViewConstants;
import com.adm.xcp.vos.AzureVO;
import com.adm.xcp.vos.MenuAcessoVO;
import com.adm.xcp.vos.MenuVO;
import com.adm.xcp.vos.PesquisaTelaVO;

@SessionScoped
@ManagedBean
public class XcpLoginBacking extends XcpAbstractBacking {

	private static final Logger logger = Logger.getLogger(XcpLoginBacking.class);

	public static final String PAR_FROM_LOGIN = "XcpLoginBacking.fromLogin";
	//lpublic static final String PAR_LOGIN_LOGOUT_VO = "XcpLoginBacking.loginLogoutVO";

	@EJB
	private OperadoresSession operadoresSession;

	@EJB
	private XcpQuerySession session;

	@EJB
	private XcpServerCacheSessionBean cache;

	@EJB
	private XcpPersistSession persist;

	@EJB
	private XcpServerEmailSession emailSession;

	private String desCaminhoLogoTelaLogin = "";
	private String desSenha;
	private String desUsuario;
	private String desCaptcha;
	private Operadores operadorSel;
	private Integer codEmpresa = null;
	private List<MenuVO> list;
	private Integer activeMenuIdx;
	private String desNomeCabecalho = null;
	private List<PesquisaTelaVO> listPesq;
	private Boolean renderedEsqueciSenha;
	private Boolean renderedCaptcha;
	private String complErrAzure;

	public void actionLoginAzure(ComponentSystemEvent event) throws Exception {
		HttpSession session = (HttpSession) getExternalContext().getSession(false);
		AzureVO vo = (AzureVO) session.getAttribute(AzureServlet.AZURE_VO);

		if (!Util.isEmpty(vo.getMsgErro())) {
			addFacesMessage(this.getTraducao(vo.getMsgErro()));
			this.setComplErrAzure(vo.getCompl());
			return;
		}
		this.desUsuario = vo.getOperador().getNome();
		this.logarInternal(false);
		getFacesContext().getApplication().getNavigationHandler().handleNavigation(getFacesContext(), null, XcpViewConstants.PG_HOME_REDIRECT);
		session.removeAttribute(AzureServlet.AZURE_VO);
	}

	public String getCaminhoLogoTelaLogin() {
		if (this.desCaminhoLogoTelaLogin == "") {
			XcpLayoutAppBacking layoutBacking = getBacking(XcpLayoutAppBacking.class);
			this.desCaminhoLogoTelaLogin = layoutBacking.getCaminhoLogoTelaLogin();
		}
		return this.desCaminhoLogoTelaLogin;
	}

	public void actionLoginCert(ComponentSystemEvent event) {

		try {
			ICPBrasilCertReader reader = new ICPBrasilCertReader();
			if (this.operadorSel == null) {
				HttpServletRequest req = getRequest();
				X509Certificate certChain[] = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
				if (certChain != null) {
					for (int i = 0; i < certChain.length; i++) {
						reader.read(certChain[i]);

						/**
						 * Testa-se a pessoa juridica antes pois um e-CNPJ tem tanto
						 * CPF
						 * quanto CNPJ. Como nao vamos suportar ECNPJ, o mesmo acaba
						 * sendo desprezado no teste abaixo
						 */
						if (reader.getDesCnpj() != null) {

							if ("92657592000137".equals(reader.getDesCnpj())) {
								this.operadorSel = this.operadoresSession.buscaOperador(null, "ADMRH", Operadores.PERMISSAO_1_SISTEMA);
							}
							// Verificado com o Ariel que nao vamos ter login por ECNPJ.
							//TODO Verificar se é isso mesmo
							//this.operadorSel = this.operadoresSession.buscaOperador("ADMRH");
							//this.operadorSel = this.operadoresSession.buscaOperadorCpf(1078847035L);
						} else if (reader.getDesCpf() != null) {
							//Pessoa juridica tambem tem cpf, no caso, do representante
							this.operadorSel = this.operadoresSession.buscaOperadorCpf(this.getEjbVars(), Converter.toLong(reader.getDesCpf()));
						}

						if (this.operadorSel != null) {
							break;
						}
					}
				} else {
					//TODO Cadastrar mensagem "Problemas na leitura do certificado. Verifique que o mesmo e valido"
					addFacesMessage(this.getTraducao("msg_certificado_invalido"));
					return;
				}
			}

			if (this.operadorSel == null) {
				//TODO Cadastrar mensagem "Operador nao encontrado com o identificado"
				addFacesMessage(this.getTraducao("msg_operador_n_encontrado", reader.getDesCpf() == null ? reader.getDesCnpj() : reader.getDesCpf()));
				return;
			}
			if (!this.loginProceed(this.getEjbVars(), this.getTipoAcesso())) {
				return;
			}

			/*
			 * FIXME Neste ponto tem que apagar o principal que o login module
			 * criou e setar o correto (usuario logado) para funcionar o log de
			 * chamadas de procedures. (Verificar requisito quando for
			 * desenvolvido)
			 */
			//Precisa do redirect pois senao a tela fica toda "bagunçada" quando abre.
			getFacesContext().getApplication().getNavigationHandler().handleNavigation(getFacesContext(), null, XcpViewConstants.PG_HOME_REDIRECT);
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public String logarCert() {
		return XcpViewConstants.PG_LOGIN_CERT_REDIRECT;
	}

	private boolean loginProceed(XcpEjbVars ejbVars, Integer tipoAcesso) throws Exception {

		String nomeCabecalho = this.getOperadorSel().getNome();
		if (Operadores.PERMISSAO_2_PORTAL.equals(this.operadorSel.getPermissao())) {
			if (this.operadorSel.getMatricula() != null) {
				nomeCabecalho = this.operadoresSession.buscaNomeOperadorPortal(this.getEjbVars(), this.operadorSel.getEmpresa(), this.operadorSel.getMatricula());
			}
		}
		this.setDesNomeCabecalho(nomeCabecalho);

		// Se tiver que testar a escala de trabalho do trabalhador
		if (new Integer("1").equals(this.operadorSel.getAcesso())) {
			if (!this.operadoresSession.isOnWorkTime(this.getEjbVars(), this.operadorSel)) {
				//TODO Cadastrar mensagem "Nao e possivel utilizar o sistema nesse horario. Verifique sua escala de trabalho"
				addFacesMessage(this.getTraducao("msg_horario_nao_trabalhar"));
				return false;
			}
		}

		try {
			this.codEmpresa = this.operadoresSession.buscaEmpresaOperador(ejbVars, this.operadorSel.getOperador());
			List<Empresas> listaEmpresas = this.operadoresSession.buscaEmpresas(ejbVars, this.operadorSel.getOperador());

			Empresas emp = null;
			for (Empresas empresa : listaEmpresas) {
				if (this.codEmpresa.equals(empresa.getEmpresa())) {
					emp = empresa;
				}
			}

			this.montaMenu(ejbVars, tipoAcesso, this.operadorSel.getSeqUsuXcpDemandas());

			if (this.getList() == null || this.getList().size() < 1) {

				MontaQuery q = new MontaQuery("select e from Operadoresperfis e");
				q.addWhere("operador", "=", this.operadorSel.getOperador());
				List<Operadoresperfis> list = this.xcpQuerySession.executeQueryList(ejbVars, q);

				q = new MontaQuery("select e from Permissao e");
				q.addWhere("operador", "=", this.operadorSel.getOperador());
				List<Permissao> listPerm = this.xcpQuerySession.executeQueryList(ejbVars, q);

				if (Util.isEmpty(list) && Util.isEmpty(listPerm)) {
					//Operador sem perfil cadastrado e sem permissoes avulsas. Verifique o cadastro
					addFacesMessage(this.getTraducao("msg_operador_sem_perfil_perm"));
					return false;
				} else if (Util.isEmpty(list)) {
					//Operador sem perfil cadastrado. Verifique o cadastro
					addFacesMessage(this.getTraducao("msg_operador_sem_perfil"));
					return false;
				}

				list = this.operadoresSession.perfisLiberados(ejbVars, this.operadorSel.getOperador());
				if (Util.isEmpty(list)) {
					//Operador sem perfis liberados. Verificar data de liberacao/expiracao dos existentes.
					addFacesMessage(this.getTraducao("msg_operador_sem_perfil_lib"));
					return false;
				}

				addFacesMessage(this.getTraducao("msg_sem_perfil_valido"));
				return false;
			}

			Long tipSessao = this.getParametroGlobalLong("Seguranca", 3);
			if (Objects.equals(tipSessao, 2L)) {

				List<Sessoes> listrem = new ArrayList<Sessoes>();

				StringBuilder sb = new StringBuilder();
				sb.append(" update sessoes set tipo = :tipo, ");
				sb.append("	 	termino = :termino,				");
				sb.append("		oper_cancel = :operador			");
				sb.append("	 where sessao = :sessao		");

				XcpApplicationBacking xcpApplicationBacking = getBacking(XcpApplicationBacking.class);

				for (Sessoes s : xcpApplicationBacking.getUsuariosLogados()) {
					if (Objects.equals(s.getOperador(), this.operadorSel.getOperador())) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("tipo", Sessoes.TIPO_3_CANCELADA);
						map.put("termino", new Date());
						map.put("operador", this.operadorSel.getOperador());
						map.put("sessao", s.getSessao());
						this.persist.executeNativeQuery(this.getEjbVars(), sb.toString(), map);
						listrem.add(s);
					}
				}

				if (listrem.size() > 0) {
					synchronized (xcpApplicationBacking.getUsuariosLogados()) {
						for (Iterator it = xcpApplicationBacking.getUsuariosLogados().iterator(); it.hasNext();) {
							Sessoes s = (Sessoes) it.next();
							for (Sessoes srem : listrem) {
								if (Objects.equals(s.getSessao(), srem.getSessao())) {
									it.remove();
								}
							}
						}
					}
				}
			}

			//Limpa o cache de permissoes no banco
			XcpPermissaoAcaoCache.getInstance().clear(this.operadorSel.getOperador());

			HttpServletRequest req = getRequest();
			Sessoes sessao = new Sessoes();
			sessao.setInicio(new Date());
			sessao.setIpv4(req.getRemoteAddr());
			sessao.setOperador(this.operadorSel.getOperador());
			sessao.setPorta(req.getLocalPort());
			sessao.setServidor(req.getLocalAddr());
			sessao.setTipo(Sessoes.TIPO_1_ATIVO);
			// salvar variáveis na sessão
			sessao = this.getSession().registraLogin(this.operadorSel, emp, listaEmpresas, sessao);

			//Grava o acesso
			LoginLogoutVO vo = new LoginLogoutVO(sessao.getSessao());
			vo.setDesRemoteAdress(req.getRemoteAddr());
			vo.setCodEmpresa(this.codEmpresa);
			vo.setOperador(this.operadorSel.getOperador());
			vo.setTipOperacao(Acessos.TIP_ACESSO_1_LOGIN);
			vo.setDesIpServidorRequest(req.getLocalAddr());
			vo.setNumPortaServidorRequest(req.getLocalPort());

			XcpSessionBacking xcpSession = this.getSession();
			xcpSession.setLoginLogoutVO(vo);

			this.operadoresSession.gravaLoginLogout(ejbVars, vo);

			return true;

		} catch (MensagemException e) {
			addFacesMessage(this.getTraducao(e.getCodMensagem(), e.getPars()));
			return false;
		}
	}

	public boolean isRenderedLoginCert() {
		HttpServletRequest request = getRequest();
		if (request != null) {
			return request.isSecure();
		}
		return false;
	}

	private boolean isValidPassword(String storedPass) {
		String typedPass = Util.getMD5(this.desSenha);

		if (typedPass.equals(storedPass)) {
			return true;
		}

		String typedPass2 = Util.getMD5Correto(this.desSenha);

		if (typedPass2.equals(storedPass)) {
			return true;
		}

		//		StringBuilder sb = new StringBuilder();
		//		sb.append("LOGIN_ N_VAL U: ");
		//		sb.append(this.desUsuario);
		//		sb.append(" C1: ");
		//		sb.append(typedPass);
		//		sb.append(" C2: ");
		//		sb.append(typedPass2);
		//		sb.append(" A: ");
		//		sb.append(storedPass);
		//		logger.warn(sb.toString());
		return false;
	}

	public String actionEsqueciSenha() {
		XcpCaptchaBacking back = getBacking(XcpCaptchaBacking.class);
		back.actionTrocarCaptcha("login");
		return "/public/xcp/XcpEsqueciSenha.xhtml";
	}

	private void loginInvalido() {

		this.registraLogin(XcpLogLogin.SITUACAO_1_FALHA);

		this.setDesUsuario(null);
		this.setDesSenha(null);
		this.setDesCaptcha(null);

		if (this.getTipoAcesso().equals(Menus.PERMISSAO_2_PORTAL)) {
			XcpCaptchaBacking back = getBacking(XcpCaptchaBacking.class);
			back.actionTrocarCaptcha("login");
		}

		addFacesMessage(this.getTraducao("msg_login_invalido"));
	}

	public String logarInternal(boolean validate) {

		if (!XcpMonitoraCache.getInstance().isAtivo()) {
			addFacesMessage(this.getTraducao("msg_esocial_desativado"));
			return null;
		}

		try {
			Integer tipoAcesso = this.getTipoAcesso();

			if (validate) {
				if (Util.isEmpty(this.getDesUsuario()) || Util.isEmpty(this.getDesSenha())) {
					this.loginInvalido();
					return null;
				}

				XcpCaptchaBacking back = getBacking(XcpCaptchaBacking.class);
				if (this.getTipoAcesso().equals(Menus.PERMISSAO_2_PORTAL) && back.getTipoCaptcha().equals(XcpCaptchaBacking.CAPTCHA_0_SIMPLECAPTCHA)) {

					if (!back.isCorrect("login", this.getDesCaptcha())) {
						this.loginInvalido();
						return null;
					}
				}
			}

			XcpEjbVars ejbVars = validate ? this.getEjbVars() : null;

			this.operadorSel = this.operadoresSession.buscaOperador(ejbVars, this.desUsuario, tipoAcesso);
			if (this.operadorSel == null) {
				this.loginInvalido();
				return null;
			}

			Date dtaAtu = Util.trunc(new Date());
			if (this.operadorSel.getDtaIniBloqueio() != null && this.operadorSel.getDtaFimBloqueio() != null) {
				if (Util.dataDentroPeriodo(dtaAtu, this.operadorSel.getDtaIniBloqueio(), this.operadorSel.getDtaFimBloqueio())) {
					addFacesMessage(this.getTraducao("msg_usu_bloq_temp"));
					this.registraLogin(XcpLogLogin.SITUACAO_1_FALHA);
					return null;
				}
			} else {
				if (this.operadorSel.getDtaIniBloqueio() != null) {
					if (dtaAtu.after(this.operadorSel.getDtaIniBloqueio())) {
						addFacesMessage(this.getTraducao("msg_usu_bloq_temp"));
						this.registraLogin(XcpLogLogin.SITUACAO_1_FALHA);
						return null;
					}
				}

				if (this.operadorSel.getDtaFimBloqueio() != null) {
					if (dtaAtu.before(this.operadorSel.getDtaFimBloqueio())) {
						addFacesMessage(this.getTraducao("msg_usu_bloq_temp"));
						this.registraLogin(XcpLogLogin.SITUACAO_1_FALHA);
						return null;
					}
				}
			}

			boolean isLoginNormal = false;
			if (validate) {
				if (Constants.INT_1.equals(this.operadorSel.getCertificado())) {
					addFacesMessage(this.getTraducao("msg_login_por_certificado"));
					return null;
				} else if (Constants.INT_1.equals(this.operadorSel.getLdap())) {
					if (!this.authenticateLDAP(this.desUsuario, this.desSenha)) {
						this.loginInvalido();
						return null;
					}
				} else {
					isLoginNormal = true;
					if (!this.isValidPassword(this.operadorSel.getSenha())) {
						this.loginInvalido();
						return null;
					}
				}
			}

			if (!this.loginProceed(ejbVars, tipoAcesso)) {
				this.registraLogin(XcpLogLogin.SITUACAO_1_FALHA);
				return null;
			}

			if (this.getTipoAcesso().equals(Menus.PERMISSAO_2_PORTAL)) {
				if (this.cache.hasToClear(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_XCP_TRADUCAO)) {
					XcpTraducaoCache.limpar();
					this.cache.markCacheOk(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_XCP_TRADUCAO);
				}
				//Conforme solicitacao de Maicon, aumentado para 30 minutos
				((HttpSession) getExternalContext().getSession(false)).setMaxInactiveInterval(30 * 60);
			} else {
				Long tempoSessao = this.getParametroLong("Sistema", 5);
				if (tempoSessao == null) {
					//Se o parametro não foi definido, o tempo padrão é 30 
					((HttpSession) getExternalContext().getSession(false)).setMaxInactiveInterval(30 * 60);
				} else {
					((HttpSession) getExternalContext().getSession(false)).setMaxInactiveInterval(tempoSessao.intValue() * 60);
				}
			}

			if (validate) {
				if (isLoginNormal) {
					try {

						boolean altSenha = INT_1.equals(this.operadorSel.getTrocaSenha());

						//					Com a alteracao para login por senha gerada, nao mais precisa fazer esses tratamentos
						//					if (this.operadorSel.getCpf() != null) {
						//						String cpf = String.format("%011d", this.operadorSel.getCpf());
						//						altSenha = Util.getMD5(cpf).equals(desSenhaHash);
						//					} else if (this.operadorSel.getMatricula() != null) {
						//						FuncionariosPK pk = new FuncionariosPK();
						//						pk.setEmpresa(this.codEmpresa);
						//						pk.setMatricula(this.operadorSel.getMatricula());
						//						Funcionarios func = this.xcpQuerySession.find(this.getEjbVars(), Funcionarios.class, pk);
						//						String cpf = String.format("%011d", func.getCpf());
						//						altSenha = Util.getMD5(cpf).equals(desSenhaHash);
						//					} else {
						//						altSenha = Util.getMD5(Converter.toString(this.operadorSel.getOperador())).equals(desSenhaHash);
						//					}

						if (!altSenha) {
							Date renovacao = this.operadorSel.getRenovacao();
							if (renovacao != null) {
								int diff = DataUtil.difDays(new Date(), renovacao);
								if (diff <= 0) {
									altSenha = true;
									//TODO Cadastrar mensagem "Sua senha expirou, informe uma nova senha"
									addFacesMessage(this.getTraducao("msg_senha_expirada"));
								} else {
									RegraSenha regra = this.xcpQuerySession.find(this.getEjbVars(), RegraSenha.class, this.codEmpresa);
									if (regra != null) {
										Integer aviso = regra.getAviso();
										if (aviso != null) {
											// Obtém a quantidade de dias para expiração da senha
											if (diff <= aviso) {
												//TODO cadastrar mensagem "Senha expirará em X dias"
												addFacesMessage(this.getTraducao("msg_senha_expirara_dias", diff));
											}
										}
									}
								}
							}
						}

						if (altSenha) {
							this.putFlash(PAR_FROM_LOGIN, true);
							return "/secure/AlteraSenhaForm.xhtml";
						}
					} catch (Exception e) {
						logger.error(e);
					}
				}

				this.registraLogin(XcpLogLogin.SITUACAO_2_SUCESSO);

				//TODO cadastrar mensagem "Logado com sucesso"

				addFacesMessage(this.getTraducao("msg_usuario_logado_sucesso"), MSG_INFO);
			}
			return PG_HOME;
		} catch (Exception e) {
			logger.error(e);
			this.addFacesMessage(e);
		}

		return null;
	}

	public String logar() {
		return this.logarInternal(true);
	}

	public synchronized void montaMenu(XcpEjbVars ejbVars, Integer tipoAcesso, Long seqUsuXcpDemandas) {
		this.list = new ArrayList<MenuVO>();

		List<PlainMenuVO> listplain = new ArrayList<PlainMenuVO>();
		List<Menus> listaMenus = this.operadoresSession.buscaMenus(ejbVars, tipoAcesso);
		if (listaMenus != null && !listaMenus.isEmpty()) {
			logger.info("Foram encontrados [" + listaMenus.size() + "] menus na base...");
			for (Menus menu : listaMenus) {

				List<AcessofuncView> listaSubMenus = this.operadoresSession.buscaSubmenus(ejbVars, this.codEmpresa, this.operadorSel.getOperador(), menu.getMenu());

				listplain.add(new PlainMenuVO(menu.getDescricao(), listaSubMenus));

				if (listaSubMenus == null || listaSubMenus.isEmpty()) {
					continue;
				}

				MenuVO mvo = new MenuVO();
				mvo.setDesLabel(menu.getDescricao());
				MenuModel mModel = new DefaultMenuModel();
				mvo.setSubmenu(mModel);
				this.list.add(mvo);
				for (AcessofuncView row : listaSubMenus) {

					String tela = row.getComponente();
					//Se nao tem componente é uma execucao de objeto
					if (Util.isEmpty(tela)) {
						tela = PG_EXECUCAO_OBJETO_COMPONENTE;
					}

					MenuAcessoVO vo = new MenuAcessoVO();
					vo.setDescricao(row.getDescricao());
					vo.setRotina(row.getRotina());
					vo.setComponente(tela);
					vo.setCodObjeto(row.getCodObjeto());
					vo.setCodPaginaXcp(row.getCodPaginaXcp());
					this.getSession().addMenuAcesso(vo);

					DefaultMenuItem item = new DefaultMenuItem(row.getDescricao());
					item.setImmediate(true);
					item.setParam("codigoRotina", vo.getRotina());
					item.setParam("iconeRotina", (row.getIcone() == null ? "" : row.getIcone()));

					String codObjeto = "";//Só passa o objeto se for a tela de execucao 
					if (tela.equals(PG_EXECUCAO_OBJETO_COMPONENTE) && row.getCodObjeto() != null) {
						codObjeto = row.getCodObjeto();
					}

					if (row.getIdConsulta() != null) {
						item.setCommand("#{xcpLayoutAppBacking.actionLinkTransparencia(" + row.getIdConsulta() + ")}");
					} else if (row.getCodPaginaXcp() != null) {
						if (Rotinas.TIP_ROTINA_4_PG_FLOW_SISTEMA_DEMANDAS.equals(row.getTipRotina())) {
							if (seqUsuXcpDemandas != null) {
								item.setCommand("#{xcpLayoutAppBacking.actionFlowfastDemandas(" + row.getCodPaginaXcp() + ")}");
							} else {
								continue;
							}
						} else {
							if (this.getTipoAcesso().equals(Menus.PERMISSAO_2_PORTAL)) {
								item.setParam("flow", 1);
								item.setParam("page", row.getCodPaginaXcp());
								item.setOnclick(String.format("XcpMenu.openFlow('%s');", row.getCodPaginaXcp()));
							} else {
								item.setCommand("#{xcpLayoutAppBacking.actionFlowfast(" + row.getCodPaginaXcp() + ")}");
							}
						}
					} else if (tela.endsWith(".html")) {
						tela = tela.replaceAll(".html", "");
						item.setOnclick(String.format("XcpMenu.openHelp('%s','%s');", this.getRequestContextPath(), Converter.toHex(tela)));
					} else {
						item.setOnclick(String.format("return XcpMenu.openPage('%s', '%s', '%s');return false;", tela, codObjeto, row.getDescricao()));
					}
					mModel.addElement(item);
				}
			}

			this.getSession().setListplain(listplain);
		}

		if (Menus.PERMISSAO_2_PORTAL.equals(tipoAcesso)) {
			this.setActiveMenuIdx(0);
		}
	}

	public String logout() throws Exception {
		BigDecimal b = null;
		if (Menus.PERMISSAO_2_PORTAL.equals(this.getTipoAcesso())) {
			b = this.getParametroGlobalBigDecimal("Azure", 11);
		} else {
			b = this.getParametroGlobalBigDecimal("Azure", 1);
		}

		boolean azure = BigDecimal.ONE.equals(b);

		HttpSession session = (HttpSession) getExternalContext().getSession(false);
		session.invalidate();

		if (azure) {
			return PG_SESSAO_ENCERRADA_REDIRECT;
		}

		//Precisa do redirect para evitar problema de apertar F5 logo apos o logout.
		if (Menus.PERMISSAO_2_PORTAL.equals(this.getTipoAcesso())) {
			return PG_PORTAL_REDIRECT;
		} else {
			return PG_LOGIN_REDIRECT;
		}
	}

	public String actionEjetarTela() {
		RequestContext requestContext = getRequestContext();
		FacesContext facesContext = getFacesContext();

		String ejectId = UUID.randomUUID().toString();
		requestContext.addCallbackParam("pageName", facesContext.getViewRoot().getViewId());
		requestContext.addCallbackParam("ejectId", ejectId);

		facesContext.getExternalContext().getSessionMap().put(PARAM_XCP_EJECT_ID + ":" + ejectId, new HashMap(facesContext.getViewRoot().getViewMap()));

		return PG_HOME;
	}

	private boolean authenticateLDAP(String desUsuario, String desSenha) throws Exception {
		InitialLdapContext ctx = null;
		try {
			if (this.operadorSel.getCodldap() == null) {
				//TODO Cadastrar mensagem "Dados ldap nao cadastrados para o operador"
				addFacesMessage(this.getTraducao("msg_dados_ldap_nao_cadastrados"));
				return false;
			}

			Ldap ldap = this.xcpQuerySession.find(this.getEjbVars(), Ldap.class, this.operadorSel.getCodldap());
			if (ldap == null) {
				//TODO Cadastrar mensagem "Dados ldap nao cadastrados para o operador"
				addFacesMessage(this.getTraducao("msg_dados_ldap_nao_cadastrados"));
				return false;
			}

			//ldap://andreo.xcape.com.br:9090/    
			String host = ldap.getHost();
			//uid={usuario_ldap},ou=xcape,dc=maxcrc,dc=com    
			String userCtx = ldap.getConexao();

			if (!Util.isEmpty(userCtx)) {
				userCtx = userCtx.replace("{usuario_ldap}", desUsuario);
			} else {
				userCtx = desUsuario;
			}

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, host);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, userCtx);
			env.put("com.sun.jndi.ldap.connect.timeout", "15000");
			env.put("com.sun.jndi.ldap.read.timeout", "15000");
			env.put(Context.SECURITY_CREDENTIALS, desSenha);

			ctx = new InitialLdapContext(env, null);
		} catch (NamingException e) {

			if (e instanceof AuthenticationException) {
				return false;
			}

			if (e.getCause() != null && e.getCause() instanceof AuthenticationException) {
				return false;
			}

			throw e;
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception e) {
					//ignora
				}
			}
		}
		return true;
	}

	public String getDesUsuario() {
		return this.desUsuario;
	}

	public void setDesUsuario(String desUsuario) {
		this.desUsuario = desUsuario;
	}

	public String getDesSenha() {
		return this.desSenha;
	}

	public void setDesSenha(String desSenha) {
		this.desSenha = desSenha;
	}

	public Operadores getOperadorSel() {
		return this.operadorSel;
	}

	public void setOperadorSel(Operadores operadorSel) {
		this.operadorSel = operadorSel;
	}

	public List<MenuVO> getList() {
		return this.list;
	}

	public void setList(List<MenuVO> list) {
		this.list = list;
	}

	public Integer getActiveMenuIdx() {
		if (this.activeMenuIdx == null) {
			this.activeMenuIdx = -1;
		}
		return this.activeMenuIdx;
	}

	public void setActiveMenuIdx(Integer activeMenuIdx) {
		this.activeMenuIdx = activeMenuIdx;
	}

	public String getDesNomeCabecalho() {
		return this.desNomeCabecalho;
	}

	public void setDesNomeCabecalho(String desNomeCabecalho) {
		this.desNomeCabecalho = desNomeCabecalho;
	}

	public List<PesquisaTelaVO> actionPesqTela(String query) {
		this.setListPesq(new ArrayList<PesquisaTelaVO>());

		List<MenuVO> listMenu = this.getList();
		for (MenuVO menuVO : listMenu) {

			List<MenuElement> elements = menuVO.getSubmenu().getElements();
			for (MenuElement menuElement : elements) {
				DefaultMenuItem item = (DefaultMenuItem) menuElement;

				String desRotina = Converter.toString(item.getValue());
				if (this.matchWildcard(desRotina, query)) {
					PesquisaTelaVO vo = new PesquisaTelaVO();
					vo.setDesModulo(menuVO.getDesLabel());
					vo.setDesRotina(desRotina);
					vo.setOnclick(item.getOnclick());
					vo.setCodRotina(Converter.toInteger(item.getParams().get("codigoRotina").get(0)));
					this.getListPesq().add(vo);
				}
			}
		}

		return this.getListPesq();
	}

	public void selectTela(SelectEvent event) {
		PesquisaTelaVO vo = (PesquisaTelaVO) event.getObject();
		if (vo != null) {
			if (vo.getOnclick() != null) {
				String onclick = vo.getOnclick();
				int inicio = onclick.indexOf("XcpMenu.");
				int fim = onclick.indexOf("');");
				String script = onclick.substring(inicio, fim + 3);
				StringBuilder sb = new StringBuilder();
				sb.append("$('#formMenu\\\\:txtPesTela_input').val('');");
				sb.append(script);
				getRequestContext().execute(sb.toString());
			} else if (vo.getCodRotina() != null) {
				Rotinas r = this.xcpQuerySession.find(this.getEjbVars(), Rotinas.class, vo.getCodRotina());
				if (Rotinas.TIP_ROTINA_3_PAGINA_XCP.equals(r.getTipRotina())) {
					XcpLayoutAppBacking backing = getBacking(XcpLayoutAppBacking.class);
					try {
						backing.actionFlowfast(Converter.toInteger(r.getCodPaginaXcp()));
					} catch (Exception e) {
						logger.error(e);
					}
				} else if (Rotinas.TIP_ROTINA_6_TRANSPARENCIA.equals(r.getTipRotina())) {
					XcpLayoutAppBacking backing = getBacking(XcpLayoutAppBacking.class);
					try {
						backing.actionLinkTransparencia(Converter.toInteger(r.getIdConsulta()));
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}
	}

	public List<PesquisaTelaVO> getListPesq() {
		return this.listPesq;
	}

	public void setListPesq(List<PesquisaTelaVO> listPesq) {
		this.listPesq = listPesq;
	}

	public javax.faces.convert.Converter getPesqConverter() {
		return new javax.faces.convert.Converter() {

			@Override
			public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
				if (value == null) {
					return null;
				}

				PesquisaTelaVO vo = (PesquisaTelaVO) value;
				return com.adm.util.Converter.toString(vo.getCodRotina());
			}

			@Override
			public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
				List<PesquisaTelaVO> listPesq = XcpLoginBacking.this.getListPesq();

				for (PesquisaTelaVO vo : listPesq) {
					if (vo.getCodRotina().equals(com.adm.util.Converter.toInteger(value))) {
						return vo;
					}
				}

				return null;
			}
		};
	}

	private boolean matchWildcard(String value, String filter) {

		if (value == null || filter == null) {
			return false;
		}

		value = Formatter.removeAccents(value.toUpperCase());
		filter = Formatter.removeAccents(filter.toUpperCase());

		if (value.contains(filter)) {
			return true;
		}

		if (filter.contains("%")) {
			String[] split = filter.split("[%]");

			if (!filter.startsWith("%")) {
				if (!value.startsWith(split[0])) {
					return false;
				}
			}

			int fromIndex = 0;
			for (String part : split) {
				int indexOf = value.indexOf(part, fromIndex);
				if (indexOf < 0) {
					return false;
				}

				fromIndex = indexOf;
			}
			return true;
		}
		return false;
	}

	public String getDesCaptcha() {
		return this.desCaptcha;
	}

	public void setDesCaptcha(String desCaptcha) {
		this.desCaptcha = desCaptcha;
	}

	public Boolean getRenderedEsqueciSenha() {
		if (this.renderedEsqueciSenha == null) {
			try {
				Long indRenderiza = this.getParametroGlobalLong("Esqueci senha", 3);
				if (indRenderiza == null) {
					this.renderedEsqueciSenha = true;
				} else {
					this.renderedEsqueciSenha = LONG_1.equals(indRenderiza);
				}
			} catch (Exception e) {
				this.renderedEsqueciSenha = true;
			}
		}
		return this.renderedEsqueciSenha;
	}

	public void setRenderedEsqueciSenha(Boolean renderedEsqueciSenha) {
		this.renderedEsqueciSenha = renderedEsqueciSenha;
	}

	public Boolean getRenderedCaptcha() {
		try {
			String ipsLiberadosSemCaptcha = this.getParametroGlobalString("Captcha", 2);
			if (ipsLiberadosSemCaptcha == null) {
				this.renderedCaptcha = true;
			} else {
				String ipAcesso = IpAddressUtil.getRequestIpAddress(getRequest());
				this.renderedCaptcha = !ipsLiberadosSemCaptcha.contains(ipAcesso);
			}
		} catch (Exception e) {
			this.renderedCaptcha = true;
		}
		return this.renderedCaptcha;
	}

	public void setRenderedCaptcha(Boolean renderedCaptcha) {
		this.renderedCaptcha = renderedCaptcha;
	}

	private void registraLogin(Integer situacao) {
		XcpLogLogin log = new XcpLogLogin();
		log.setNome(this.getDesUsuario());
		log.setPermissao(this.getTipoAcesso());
		log.setSituacao(situacao);
		log.setDatahora(new Date());
		log.setIp(IpAddressUtil.getRequestIpAddress(getRequest()));
		this.persist.update(this.getEjbVars(), log);

		if (this.operadorSel != null) {
			if (XcpLogLogin.SITUACAO_1_FALHA.equals(situacao)) {

				Long qtdTentativaMax = 0L;

				try {
					qtdTentativaMax = Util.nvl(this.getParametroGlobalLong("Seguranca", 1), 0L);
				} catch (Exception e) {
					//ignora
				}

				Integer qtd = Util.nvl(this.operadorSel.getQtdtentativas(), 0) + 1;

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("qtd", qtd);
				map.put("operador", this.operadorSel.getOperador());
				this.persist.executeNativeQuery(this.getEjbVars(), "update operadores set qtdtentativas = :qtd where operador = :operador", map);

				if (qtdTentativaMax > 0 && qtd >= qtdTentativaMax) {

					Date dtaIniBloqueio = this.operadorSel.getDtaIniBloqueio();
					Date dtaFimBloqueio = this.operadorSel.getDtaFimBloqueio();

					Date hj = Util.trunc(new Date());

					boolean bloqueado = false;

					if (dtaIniBloqueio != null && dtaFimBloqueio != null) {
						if (hj.compareTo(dtaIniBloqueio) >= 0 && hj.compareTo(dtaFimBloqueio) <= 0) {
							bloqueado = true;
						}
					} else if (dtaIniBloqueio != null) {
						if (hj.compareTo(dtaIniBloqueio) >= 0) {
							bloqueado = true;
						}
					} else if (dtaFimBloqueio != null) {
						if (hj.compareTo(dtaFimBloqueio) <= 0) {
							bloqueado = true;
						}
					}

					if (!bloqueado) {
						this.operadorSel.setDtaIniBloqueio(hj);
						this.operadorSel.setDtaFimBloqueio(hj);

						if (this.operadorSel.getMatricula() != null && this.operadorSel.getEmpresa() != null) {

							Funcionarios func = this.xcpQuerySession.find(this.getEjbVars(), Funcionarios.class, new FuncionariosPK(
									this.operadorSel.getEmpresa(),
									this.operadorSel.getMatricula()));

							if (!Util.isEmpty(func.getEmail())) {
								try {

									//enviar email
									Long codServidorEmail = this.getParametroGlobalLong("Seguranca", 2);
									if (codServidorEmail != null) {
										StringBuilder sb = new StringBuilder();
										sb.append("O usuário ");
										sb.append(this.operadorSel.getNome());
										sb.append(" foi temporariamente bloqueado devido a excesso de tentativas de login. ");
										sb.append(" Solicite ao administrador o desbloqueio da conta. ");

										XcpSendEmailVO mailVo = new XcpSendEmailVO(codServidorEmail, "Usuário bloqueado", sb.toString());
										mailVo.addDestinatario(func.getEmail());
										this.emailSession.sendMailAsync(this.getEjbVars(), mailVo);
									}
								} catch (Exception e) {
									//ignora falhas de envio de email para persistir o bloqueio
								}
							}
						}

					}

				}
				this.operadorSel = this.persist.update(this.getEjbVars(), this.operadorSel);
			} else {
				if (Util.nvl(this.operadorSel.getQtdtentativas(), 0) > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("qtd", 0);
					map.put("operador", this.operadorSel.getOperador());
					this.persist.executeNativeQuery(this.getEjbVars(), "update operadores set qtdtentativas = :qtd where operador = :operador", map);
				}
			}

		}
	}

	public String getComplErrAzure() {
		return this.complErrAzure;
	}

	public void setComplErrAzure(String complErrAzure) {
		this.complErrAzure = complErrAzure;
	}
}
