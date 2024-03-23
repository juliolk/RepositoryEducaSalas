package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.adm.ejb.entity.Empresas;
import com.adm.ejb.entity.Menus;
import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.XcpTraducao;
import com.adm.ejb.session.local.XcpSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.MensagemVO;
import com.adm.util.Constants;
import com.adm.util.Util;
import com.adm.util.exceptions.MensagemException;
import com.adm.util.exceptions.SemEmpresaException;
import com.adm.util.vo.XcpEjbVars;
import com.adm.xcp.util.IpAddressUtil;
import com.adm.xcp.util.XCapeExceptionHandler;
import com.adm.xcp.util.XcpTraducaoCache;
import com.adm.xcp.util.XcpViewConstants;
import com.adm.xcp.vos.MenuAcessoVO;

public abstract class XcpAbstractBackingBase implements XcpViewConstants, Constants {
	private static Logger log = Logger.getLogger(XcpAbstractBackingBase.class);

	public static final FacesMessage.Severity MSG_ERROR = FacesMessage.SEVERITY_ERROR;
	public static final FacesMessage.Severity MSG_FATAL = FacesMessage.SEVERITY_FATAL;
	public static final FacesMessage.Severity MSG_INFO = FacesMessage.SEVERITY_INFO;
	public static final FacesMessage.Severity MSG_WARN = FacesMessage.SEVERITY_WARN;

	private ArrayList<SelectItem> itensIndSimNao;
	private ArrayList<SelectItem> itensIndSimNaoNull;
	//MAP para guardar os filtros de tela.
	private Map<String, String> filterMap;

	public Map<String, String> getFilterMap() {
		return this.filterMap;
	}

	public void setFilterMap(Map<String, String> filterMap) {
		this.filterMap = filterMap;
	}

	//TODO Pegar o idioma de algum lugar
	public static final Integer COD_IDIOMA = 1;

	@EJB
	protected XcpQuerySession xcpQuerySession;

	@EJB
	protected XcpSession xcpSession;

	public String getTraducao(MensagemVO mensagem) {
		if (mensagem.getArgs() != null) {
			return String.format(this.getTraducao(mensagem.getDesChave()), (Object[]) mensagem.getArgs());
		}
		return this.getTraducao(mensagem.getDesChave());
	}

	public String getTraducao(String key, Object... args) {
		return String.format(this.getTraducao(key), args);
	}

	public String getTraducao(String key) {
		return this.getTraducao(key, this.getSession().getRotina().getRotina());
	}

	public String getTraducao(String key, Integer numRotina) {
		XcpTraducaoCache cache = XcpTraducaoCache.getInstance();
		if (!cache.isCarregado()) {
			this.montaCacheTraducao();
			cache = XcpTraducaoCache.getInstance();
		}

		return cache.getKey(key, COD_IDIOMA, numRotina);
	}

	/**
	 * Retorna o código da empresa que o usuário está utilizando no momento
	 * 
	 * @return código empresa
	 */
	public Integer getCodEmpresa() {
		Empresas vo = this.getSession().getEmpresa();
		if (vo == null || vo.getEmpresa() == null) {
			//TODO Cadastrar mensagem "Usuario sem empresa"
			String mensagemRetorno = this.getTraducao("msg_usuario_sem_empresa");
			throw new SemEmpresaException(mensagemRetorno);
		}

		return vo.getEmpresa();
	}

	private void montaCacheTraducao() {
		List<XcpTraducao> traducoes = this.xcpSession.findTraducao(this.getEjbVars());
		XcpTraducaoCache.limpar();
		XcpTraducaoCache cache = XcpTraducaoCache.getInstance();
		for (XcpTraducao admTraducao : traducoes) {
			cache.putKey(admTraducao);
		}
		cache.setListaIdiomas(this.xcpSession.findIdiomas(this.getEjbVars()));
		if (!cache.getListaIdiomas().isEmpty()) {
			cache.setIdiomaDefault(cache.getListaIdiomas().get(0));
		}
		cache.setCarregado(true);
	}

	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	public static void validationFailed(String msg) {
		addFacesMessage(msg);
		FacesContext.getCurrentInstance().validationFailed();
	}

	public static RequestContext getRequestContext() {
		return RequestContext.getCurrentInstance();
	}

	public static ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) getExternalContext().getRequest();
	}

	public String getRequestContextPath() {
		return getExternalContext().getRequestContextPath();
	}

	public static String getPageName() {
		return getFacesContext().getViewRoot().getViewId();
	}

	public static String getSimplePageName() {
		String pageName = getPageName();
		if (pageName == null) {
			return null;
		}

		try {
			return pageName.substring(pageName.lastIndexOf('/') + 1, pageName.length());
		} catch (Exception e) {
			//ignora caso a formacao do nome esteja diferente.
		}

		return pageName;
	}

	public String getContexto() {
		return getContextoStatic();
	}

	public static String getContextoStatic() {
		return getExternalContext().getRequestContextPath();
	}

	public static Object getELResolverObject(String nome) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		Object obj = app.getELResolver().getValue(context.getELContext(), null, nome);
		return obj;
	}

	public static <T extends XcpAbstractBacking> T getBacking(Class<T> backClass) {
		String backName = backClass.getSimpleName();
		backName = backName.substring(0, 1).toLowerCase() + backName.substring(1);
		return (T) getELResolverObject(backName);
	}

	public void addFacesMessage(Throwable e) {
		Throwable exTratada = XCapeExceptionHandler.trataException(e);

		if (exTratada instanceof MensagemException) {
			String msgEx = ((MensagemException) exTratada).getCodMensagem();
			if (msgEx != null) {
				if (msgEx.startsWith("msg_ora_02292")) {
					addFacesMessage(this.getTraducao("msg_ora_02292", ((MensagemException) exTratada).getPars()) + "\nNome da vinculação: "
							+ msgEx.replace("msg_ora_02292", ""));
				} else {
					addFacesMessage(this.getTraducao(msgEx, ((MensagemException) exTratada).getPars()));
				}
			} else {
				log.error(getPageName(), exTratada);
				addFacesMessage(exTratada.getMessage());
			}
		} else {
			/**
			 * Feito assim temporariamente para conseguir recuperar as mensagens
			 * do banco de dados nas telas quando ocorre exception. Melhorar
			 * para buscar recursivo.
			 */
			StringBuilder sb = new StringBuilder();
			sb.append(exTratada.getMessage());
			sb.append("\n");
			if (exTratada.getCause() != null) {
				exTratada = exTratada.getCause();
				sb.append(exTratada.getMessage());
				sb.append("\n");
				if (exTratada.getCause() != null) {
					exTratada = exTratada.getCause();
					sb.append(exTratada.getMessage());
				}
			}
			log.error(getPageName(), exTratada);
			addFacesMessage(sb.toString());
		}
	}

	public static void addFacesMessage(String msg) {
		FacesMessage message = new FacesMessage(MSG_ERROR, msg, null);
		getFacesContext().addMessage(null, message);
	}

	public static void addFacesMessage(String msg, Severity severity) {
		FacesMessage message = new FacesMessage(severity, msg, null);
		getFacesContext().addMessage(null, message);
	}

	public UIComponent findComponent(String id) {
		if (id == null) {
			return null;
		}
		UIComponent result = null;
		UIComponent root = FacesContext.getCurrentInstance().getViewRoot();
		if (root != null) {
			UIComponent form = root.findComponent("form");
			if (form != null) {
				result = form.findComponent(id);
				if (result != null) {
					return result;
				}
			}
			result = this.findComponent(root, id);
		}
		return result;
	}

	private UIComponent findComponent(UIComponent root, String id) {
		UIComponent result = null;
		if (root.getId().equals(id)) {
			return root;
		}

		for (UIComponent child : root.getChildren()) {
			if (child != null && child.getId() != null) {
				if (child.getId().equals(id)) {
					result = child;
					break;
				}
				result = child.findComponent(id);
				if (result != null) {
					break;
				}
				result = this.findComponent(child, id);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	public Collection<SelectItem> getItens(String campo, Object... valores) {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();

		itens.add(new SelectItem("", this.getTraducao("opt_selecione")));

		for (Object obj : valores) {
			String label = this.getTraducao("opt_" + campo + "_" + obj);
			itens.add(new SelectItem(obj, label));
		}

		return itens;
	}

	public Collection<SelectItem> getItensSemNull(String campo, Object... valores) {
		Collection<SelectItem> itens = new ArrayList<SelectItem>();

		for (Object obj : valores) {
			String label = this.getTraducao("opt_" + campo + "_" + obj);
			itens.add(new SelectItem(obj, label));
		}

		return itens;
	}

	public List<SelectItem> getItensIndSimNao() {
		if (this.itensIndSimNao == null) {
			this.itensIndSimNao = new ArrayList<SelectItem>();
			this.itensIndSimNao.add(new SelectItem(1, this.getTraducao("opt_sim")));
			this.itensIndSimNao.add(new SelectItem(0, this.getTraducao("opt_nao")));
		}
		return this.itensIndSimNao;
	}

	public List<SelectItem> getItensIndSimNaoNull() {
		if (this.itensIndSimNaoNull == null) {
			this.itensIndSimNaoNull = new ArrayList<SelectItem>();
			this.itensIndSimNaoNull.add(new SelectItem("", this.getTraducao("opt_selecione")));
			this.itensIndSimNaoNull.add(new SelectItem(1, this.getTraducao("opt_sim")));
			this.itensIndSimNaoNull.add(new SelectItem(0, this.getTraducao("opt_nao")));
		}
		return this.itensIndSimNaoNull;
	}

	public void putFlash(String key, Object value) {
		getExternalContext().getFlash().putNow(key, value);
	}

	public Object getFlash(String key) {
		return getExternalContext().getFlash().get(key);
	}

	public void navigateTo(String name) {
		getFacesContext().getApplication().getNavigationHandler().handleNavigation(getFacesContext(), null, name);
	}

	public boolean isIntervaloValido(Date dtaInicio, Date dtaTermino, String message) {
		if (dtaInicio != null && dtaTermino != null) {
			if (dtaInicio.after(dtaTermino)) {
				addFacesMessage(this.getTraducao(message), MSG_INFO);
				return false;
			}
		}
		return true;
	}

	public boolean isIntervaloValido(Long inicio, Long termino, String message) {
		if (inicio != null && termino != null) {
			if (inicio.compareTo(termino) > 0) {
				addFacesMessage(this.getTraducao(message), MSG_INFO);
				return false;
			}
		}
		return true;
	}

	public XcpSessionBacking getSession() {
		return getSessionStatic();
	}

	public static XcpSessionBacking getSessionStatic() {
		return getBacking(XcpSessionBacking.class);
	}

	public Rotinas getRotina() {
		//Busca a rotona no XcpViewScopedBacking para faser uma especie de "cache" das rotinas para não ficar acessando o banco em todo o request
		return getBacking(XcpViewScopedBacking.class).getRotina();
	}

	public String getCodObjeto() {
		if (getPageName().equals(PG_EXECUCAO_OBJETO)) {
			if (!getBacking(XcpViewScopedBacking.class).isConstructXcpExecObjBacking()) {
				XcpExecObjBacking backing = XcpViewUtilBacking.getBacking(XcpExecObjBacking.class);
				if (backing.getHelper().getExecObjVO() != null && backing.getHelper().getExecObjVO().getXcpObjeto() != null) {
					return backing.getHelper().getExecObjVO().getXcpObjeto().getCodObjeto();
				}
			}
		} else {
			return this.getRotina().getCodObjeto();
		}
		return null;
	}

	public Map<String, Object> getSystemVars() {
		Map<String, Object> p = new HashMap<String, Object>();
		XcpSessionBacking session = this.getSession();
		Integer empresa = null;
		String empresaNome = null;

		if (session.getEmpresa() != null) {
			empresa = session.getEmpresa().getEmpresa();
			empresaNome = session.getEmpresa().getNome();
		}

		Long operador = null;
		String operadorNome = null;
		Long codconsig = null;
		if (session.getUsuario() != null) {
			operador = session.getUsuario().getOperador();
			operadorNome = session.getUsuario().getNome();
			codconsig = session.getUsuario().getCodconsig();
		}

		p.put(XcpEjbVars.EMPRESA, empresa);
		p.put(XcpEjbVars.EMPRESA_NOME, empresaNome);
		p.put(XcpEjbVars.OPERADOR, operador);
		p.put(XcpEjbVars.OPERADOR_NOME, operadorNome);
		p.put(XcpEjbVars.CODCONSIG, codconsig);

		MenuAcessoVO menuAcesso = this.getSession().getMenuAcesso(getPageName(), this.getCodObjeto());
		if (menuAcesso != null) {
			p.put(XcpEjbVars.ROTINA, menuAcesso.getRotina());
		} else {
			p.put(XcpEjbVars.ROTINA, this.getRotina().getRotina());
		}
		p.put(XcpEjbVars.CLASS_NAME, this.getClass().getSimpleName());
		p.put(XcpEjbVars.PAGE_NAME, getPageName());
		p.put(XcpEjbVars.SIMPLE_PAGE_NAME, getSimplePageName());
		p.put(XcpEjbVars.COD_OBJETO, this.getCodObjeto());
		p.put(XcpEjbVars.IP_ACESSO, IpAddressUtil.getRequestIpAddress(getRequest()));
		p.put(XcpEjbVars.IP_SERVIDOR, getRequest().getLocalAddr());
		p.put(XcpEjbVars.NUM_PORTA, getRequest().getLocalPort());
		// no Login necessita da tradução mas ainda não efetuou o login
		if (this.getSession().getSessao() == null) {
			p.put(XcpEjbVars.SESSAO, -1L);
		} else {
			p.put(XcpEjbVars.SESSAO, this.getSession().getSessao().getSessao());
		}

		p.put(XcpEjbVars.RESTRICAO_MOSTRA_DISP, this.getSession().getMostraFuncDisp());
		p.put(XcpEjbVars.RESTRICAO_ORGAO, this.getSession().getRestricaoOrgao());
		p.put(XcpEjbVars.RESTRICAO_DIVISAO, this.getSession().getRestricaoDivisao());
		p.put(XcpEjbVars.RESTRICAO_SETOR, this.getSession().getRestricaoSetor());
		p.put(XcpEjbVars.RESTRICAO_VINCULO, this.getSession().getRestricaoVinculo());

		p.put(XcpEjbVars.RESTRICAO_PUBLICACAO, this.getSession().getRestricaoPublicacoes());
		p.put(XcpEjbVars.RESTRICAO_HISTORICO, this.getSession().getRestricaoHistorico());
		p.put(XcpEjbVars.RESTRICAO_PONTO, this.getSession().getRestricaoPonto());
		p.put(XcpEjbVars.RESTRICAO_AFASTAMENTO, this.getSession().getRestricaoAfast());
		p.put(XcpEjbVars.RESTRICAO_AVERBACOES, this.getSession().getRestricaoAverb());
		p.put(XcpEjbVars.RESTRICAO_AUTORIZACOES, this.getSession().getRestricaoAut());
		p.put(XcpEjbVars.RESTRICAO_PARAMETRO, this.getSession().getRestricaoPar());

		p.put(XcpEjbVars.RESTRICAO_FERIAS, this.getSession().getRestricaoFerias());
		p.put(XcpEjbVars.RESTRICAO_LICENCA, this.getSession().getRestricaoLicenca());
		p.put(XcpEjbVars.RESTRICAO_COMISSAO, this.getSession().getRestricaoComissao());
		p.put(XcpEjbVars.RESTRICAO_ASSIST, this.getSession().getRestricaoAssist());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_SETOR, this.getSession().getRestricaoTransfSetor());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_FUNCAO, this.getSession().getRestricaoTransfFuncao());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_FG, this.getSession().getRestricaoTransfFg());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_HORARIO, this.getSession().getRestricaoTransfHorario());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_CENTRO, this.getSession().getRestricaoTransfCentro());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_CARGOS, this.getSession().getRestricaoTransfCargos());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_PADROES, this.getSession().getRestricaoTransfPadroes());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_NIVEIS, this.getSession().getRestricaoTransfNiveis());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_NOMES, this.getSession().getRestricaoTransfNomes());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_AMP, this.getSession().getRestricaoTransfAmp());
		p.put(XcpEjbVars.RESTRICAO_TRANSF_SUBS, this.getSession().getRestricaoTransfSubs());

		p.put(XcpEjbVars.RESTRICAO_ORGAO_LOV, null);
		p.put(XcpEjbVars.SEM_RESTRICAO_ORGAO, null);
		p.put(XcpEjbVars.SEM_RESTRICAO_DIVISAO, null);
		p.put(XcpEjbVars.RESTRICAO_DIVISAO_LOV, null);
		p.put(XcpEjbVars.SEM_RESTRICAO_SETOR, null);
		p.put(XcpEjbVars.RESTRICAO_SETOR_LOV, null);
		p.put(XcpEjbVars.SEM_RESTRICAO_VINCULO, null);
		p.put(XcpEjbVars.RESTRICAO_VINCULO_LOV, null);
		if (this.getSession().getRestricoes() != null) {
			p.put(XcpEjbVars.RESTRICAO_ORGAO_LOV, this.getSession().getRestricoes().getOrgaos());
			p.put(XcpEjbVars.RESTRICAO_DIVISAO_LOV, this.getSession().getRestricoes().getDivisao());
			p.put(XcpEjbVars.RESTRICAO_SETOR_LOV, this.getSession().getRestricoes().getSetor());
			p.put(XcpEjbVars.RESTRICAO_VINCULO_LOV, this.getSession().getRestricoes().getVinculo());
			if (Util.isEmpty(this.getSession().getRestricoes().getOrgaos())) {
				p.put(XcpEjbVars.SEM_RESTRICAO_ORGAO, "SIM");
				p.put(XcpEjbVars.RESTRICAO_ORGAO_LOV, null);
			}
			if (Util.isEmpty(this.getSession().getRestricoes().getDivisao())) {
				p.put(XcpEjbVars.SEM_RESTRICAO_DIVISAO, "SIM");
				p.put(XcpEjbVars.RESTRICAO_DIVISAO_LOV, null);
			}
			if (Util.isEmpty(this.getSession().getRestricoes().getSetor())) {
				p.put(XcpEjbVars.SEM_RESTRICAO_SETOR, "SIM");
				p.put(XcpEjbVars.RESTRICAO_SETOR_LOV, null);
			}
			if (Util.isEmpty(this.getSession().getRestricoes().getVinculo())) {
				p.put(XcpEjbVars.SEM_RESTRICAO_VINCULO, "SIM");
				p.put(XcpEjbVars.RESTRICAO_VINCULO_LOV, null);
			}
		}
		return p;
	}

	public XcpEjbVars getEjbVars() {
		return new XcpEjbVars(this.getSystemVars());
	}

	/**
	 * @return Tipo de aplicação acessada: 1-Sistema e 2-Portal
	 */
	public Integer getTipoAcesso() {
		return getTipoAcessoStatic();
	}

	/**
	 * @return Tipo de aplicação acessada: 1-Sistema e 2-Portal
	 */
	public static Integer getTipoAcessoStatic() {
		String tipoAcesso = System.getProperty("com.xcape.TIPO_ACESSO");
		if ("2".equals(tipoAcesso)) {
			return Menus.PERMISSAO_2_PORTAL;
		} else {
			return Menus.PERMISSAO_1_SISTEMA;
		}
	}

	/**
	 * @return Indica se está em cliente ou em desenvolvimento: 1-
	 */
	public static Integer getCodClienteStatic() {
		String prop = System.getProperty("com.xcape.COD_CLIENTE");
		Integer codCliente = null;
		try {
			codCliente = new Integer(prop);
		} catch (Exception e) {
			codCliente = null;
		}
		return codCliente;
	}

	/**
	 * @return Indica se está em cliente ou em desenvolvimento.
	 */
	public static Boolean isClienteStatic() {
		if (getCodClienteStatic() == null) {
			return false;
		}
		return true;
	}

}