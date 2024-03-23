package com.adm.xcp.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.adm.ejb.entity.Acessos;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.Operadoresperfis;
import com.adm.ejb.entity.PermissaoAcao;
import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.entity.XcpAzureReq;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.session.XcpKeysSingleton;
import com.adm.ejb.session.local.XcpSession;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.util.XcpMonitoraCache;
import com.adm.ejb.vo.ChaveVO;
import com.adm.ejb.vo.LoginLogoutVO;
import com.adm.util.EJBLookup;
import com.adm.util.Formatter;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpApplicationBacking;
import com.adm.xcp.backing.XcpAvisosDashboardBacking;
import com.adm.xcp.backing.XcpLoginBacking;
import com.adm.xcp.backing.XcpSessionBacking;
import com.adm.xcp.backing.XcpViewUtilBacking;
import com.adm.xcp.util.XCapeExceptionHandler;
import com.adm.xcp.util.XcpPermissaoAcaoCache;
import com.adm.xcp.util.XcpTraducaoCache;
import com.adm.xcp.util.XcpViewConstants;
import com.adm.xcp.vos.AvisoDashboardVO;
import com.adm.xcp.vos.MenuAcessoVO;

public class XcpPhaseListener implements PhaseListener, XcpViewConstants {

	private static final Logger logger = Logger.getLogger(XcpPhaseListener.class);

	@Inject
	private XcpKeysSingleton singleton;

	@Override
	public void afterPhase(PhaseEvent event) {
		FacesContext ctx = event.getFacesContext();

		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			ExternalContext exCtx = ctx.getExternalContext();
			if (exCtx.getRequestParameterMap().containsKey(PARAM_XCP_EJECT_ID)) {
				String ejectId = exCtx.getRequestParameterMap().get(PARAM_XCP_EJECT_ID);
				if (exCtx.getSessionMap().containsKey(PARAM_XCP_EJECT_ID + ":" + ejectId)) {
					Map viewStateMap = (Map) exCtx.getSessionMap().remove(PARAM_XCP_EJECT_ID + ":" + ejectId);
					ctx.getViewRoot().getViewMap().putAll(viewStateMap);
				}
			}
		}

		if (event.getPhaseId() == PhaseId.RESTORE_VIEW || event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			ExternalContext exCtx = ctx.getExternalContext();
			HttpServletRequest req = (HttpServletRequest) exCtx.getRequest();

			XcpSessionBacking xcpSessionBacking = XcpViewUtilBacking.getBacking(XcpSessionBacking.class);
			XcpApplicationBacking xcpApplicationBacking = XcpViewUtilBacking.getBacking(XcpApplicationBacking.class);

			Operadores usuario = null;

			usuario = xcpSessionBacking.getUsuario();

			String chave = exCtx.getRequestParameterMap().get("xkey");
			if (chave != null) {
				ChaveVO chaveVO = this.singleton.get(null, chave);
				if (chaveVO != null && usuario == null) {
					XcpLoginBacking back = XcpViewUtilBacking.getBacking(XcpLoginBacking.class);
					back.setDesUsuario(chaveVO.getDesUsuario());
					back.logarInternal(false);
					return;
				}
			}

			String viewId = ctx.getViewRoot().getViewId();
			String tipoAcesso = System.getProperty("com.xcape.TIPO_ACESSO");

			/**
			 * permite acesso a qualquer conteudo statico
			 */
			if (viewId.contains("/public/")) {

				if (viewId.endsWith("XcpLogin.xhtml")) {

					// Se esta tentando acessar a pagina de login, e já está logado manda para o HOME.
					if (usuario != null) {
						ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_HOME);
					} else {

						if (Objects.equals("GET", ((HttpServletRequest) ctx.getExternalContext().getRequest()).getMethod())) {
							//Forca a ida para o login mesmo em ambiente azure
							String force = ((HttpServletRequest) exCtx.getRequest()).getParameter("force");
							if (Objects.equals("true", force)) {
								return;
							}

							boolean azure = false;
							try {
								XcpSession session = (XcpSession) EJBLookup.getInstance().get(XcpSession.JNDI_NAME);
								XcpParametrosSistema xcpAzure = null;
								if ("2".equals(tipoAcesso)) {
									xcpAzure = session.findXcpParametrosSistema(null, null, "Azure", 11);
								} else {
									xcpAzure = session.findXcpParametrosSistema(null, null, "Azure", 1);
								}

								if (xcpAzure != null) {
									azure = Objects.equals(xcpAzure.getVlrParametro(), BigDecimal.ONE);
								}
							} catch (Exception e) {
								logger.error(e);
							}

							if (azure) {
								try {
									this.sendToAzure((HttpServletResponse) exCtx.getResponse(), tipoAcesso);
								} catch (Exception e) {
									exCtx.getSessionMap().put(XCapeExceptionHandler.ERROR_STACK, Formatter.getStackTrace(e));
									ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XCapeExceptionHandler.PG_ERRO);
									logger.error(e.getMessage(), e);
								}
							}
						}
					}
				}

				return;
			}

			String codObjeto = null;
			boolean telaLogin = false;
			Sessoes sessao = xcpSessionBacking.getSessao();
			if (usuario == null) {
				//Não logado
				if (viewId.contains("/secure/")) {
					telaLogin = true;
				}
			} else {
				if (!xcpApplicationBacking.getUsuariosLogados().contains(sessao)) {
					//Sessao finalizada
					XcpViewUtilBacking.addFacesMessage(xcpSessionBacking.getTraducao("msg_sessao_finalizada"), XcpViewUtilBacking.MSG_WARN);
					req.getSession().invalidate();
					telaLogin = true;
				} else if (viewId.contains("/secure/")) {

					if (!XcpMonitoraCache.getInstance().isAtivo()) {
						XcpViewUtilBacking.addFacesMessage(xcpSessionBacking.getTraducao("msg_esocial_desativado"), XcpViewUtilBacking.MSG_ERROR);
						telaLogin = true;
					}

					codObjeto = XcpViewUtilBacking.getInstance().getCodObjeto();

					if (!viewId.equals(PG_HOME) && !xcpSessionBacking.isAcessivel(viewId, codObjeto)) {
						//Acesso negado
						XcpViewUtilBacking.addFacesMessage(xcpSessionBacking.getTraducao("msg_acesso_negado"), XcpViewUtilBacking.MSG_WARN);
						telaLogin = true;
					} else {
						/*
						 * Acesso do portal bloqueia acesso a rotinas caso a
						 * atualizacao cadastral nao tenha sido feita
						 */

						if ("2".equals(tipoAcesso)) {
							if (!XcpViewConstants.PG_HOME.equals(viewId) && !XcpViewConstants.PG_SOLICITACOES_PORTAL.equals(viewId)) {
								try {
									XcpAvisosDashboardBacking xcpAvisoBacking = XcpViewUtilBacking.getBacking(XcpAvisosDashboardBacking.class);
									AvisoDashboardVO avisoRotina = xcpAvisoBacking.getAvisoRotina(XcpAvisosDashboardBacking.ROTINA_ATUALIZACAO_CADASTRAL);
									if (avisoRotina != null) {
										XcpViewUtilBacking.addFacesMessage(xcpSessionBacking.getTraducao("msg_bloqueado_atu_cad"), XcpViewUtilBacking.MSG_WARN);
										ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_HOME);
										return;
									}
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
							}

							if (!XcpViewConstants.PG_HOME.equals(viewId) && !XcpViewConstants.PG_ESPELHO_PONTO_WEB.equals(viewId)) {
								try {
									XcpAvisosDashboardBacking xcpAvisoBacking = XcpViewUtilBacking.getBacking(XcpAvisosDashboardBacking.class);
									AvisoDashboardVO avisoRotina = xcpAvisoBacking.getAvisoRotina(XcpAvisosDashboardBacking.ROTINA_ESPELHO_PONTO);
									if (avisoRotina != null) {
										XcpViewUtilBacking.addFacesMessage(avisoRotina.getMsgListener(), XcpViewUtilBacking.MSG_WARN);
										ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_HOME);
										return;
									}
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
							}

						}

						if (!XcpViewConstants.PG_HOME.equals(viewId)) {
							try {
								MenuAcessoVO menuAcessoVO = xcpSessionBacking.getMenuAcesso(viewId, codObjeto);
								XcpPermissaoAcaoCache instance = XcpPermissaoAcaoCache.getInstance();
								List<PermissaoAcao> list = instance.get(xcpSessionBacking.getCodEmpresa(), usuario.getOperador(), menuAcessoVO.getRotina());
								if (list == null) {
									XcpViewUtilBacking util = (XcpViewUtilBacking) XcpViewUtilBacking.getELResolverObject("viewUtil");
									util.montaAcessos(xcpSessionBacking.getCodEmpresa(), menuAcessoVO.getRotina(), usuario.getOperador());
									list = instance.get(xcpSessionBacking.getCodEmpresa(), usuario.getOperador(), menuAcessoVO.getRotina());
								}
								if (!Util.isEmpty(list)) {
									for (PermissaoAcao permissaoAcao : list) {

										boolean continua = true;
										if (permissaoAcao.getPerfil() != null) {
											continua = false;
											List<Operadoresperfis> listp = xcpSessionBacking.getListaPerfisOperador();
											for (Operadoresperfis p : listp) {
												if (Objects.equals(permissaoAcao.getPerfil(), p.getPerfil())) {
													continua = true;
												}
											}
										}

										if (continua) {
											if (PermissaoAcao.TIP_BLOQ_2_TELA.equals(permissaoAcao.getTipBloq())
													&& PermissaoAcao.SITUACAO_0_BLOQUEADO.equals(permissaoAcao.getSituacao())) {
												Date dtaIni = Util.trunc(permissaoAcao.getDtaIni() != null ? permissaoAcao.getDtaIni() : new Date());
												Date dtaFim = Util.trunc(permissaoAcao.getDtaFim() != null ? permissaoAcao.getDtaFim() : new Date());
												if (Util.dataDentroPeriodoSemDataFim(Util.trunc(new Date()), dtaIni, dtaFim)) {
													String desMsg = Util.nvl(permissaoAcao.getMsg(), xcpSessionBacking.getTraducao("msg_bloqueado_temp"));
													XcpViewUtilBacking.addFacesMessage(desMsg, XcpViewUtilBacking.MSG_WARN);
													ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_HOME);
													return;
												}
											}
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								logger.error(e.getMessage(), e);
							}
						}

					}
				}
			}

			if (telaLogin) {
				req.getSession().invalidate();

				boolean azure = false;
				try {
					XcpSession session = (XcpSession) EJBLookup.getInstance().get(XcpSession.JNDI_NAME);

					XcpParametrosSistema xcpAzure = null;
					if ("2".equals(tipoAcesso)) {
						xcpAzure = session.findXcpParametrosSistema(null, null, "Azure", 11);
					} else {
						xcpAzure = session.findXcpParametrosSistema(null, null, "Azure", 1);
					}

					if (xcpAzure != null) {
						azure = Objects.equals(xcpAzure.getVlrParametro(), BigDecimal.ONE);
					}
				} catch (Exception e) {
					logger.error(e);
				}

				if (azure) {
					try {
						this.sendToAzure((HttpServletResponse) exCtx.getResponse(), tipoAcesso);
					} catch (Exception e) {
						exCtx.getSessionMap().put(XCapeExceptionHandler.ERROR_STACK, Formatter.getStackTrace(e));
						ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XCapeExceptionHandler.PG_ERRO);
						logger.error(e.getMessage(), e);
					}
				} else {
					ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_LOGIN);
				}

			} else {
				if (usuario != null) {
					if (!viewId.equals(xcpSessionBacking.getCurrentViewId())) {
						try {
							MenuAcessoVO menuAcessoVO = xcpSessionBacking.getMenuAcesso(viewId, codObjeto);

							OperadoresSession session = (OperadoresSession) EJBLookup.getInstance().get(OperadoresSession.JNDI_NAME);

							LoginLogoutVO vo = new LoginLogoutVO(xcpSessionBacking.getEjbVars().getSessao());
							vo.setDesRemoteAdress(req.getRemoteAddr());
							vo.setCodEmpresa(xcpSessionBacking.getCodEmpresa());
							vo.setOperador(usuario.getOperador());
							vo.setTipOperacao(Acessos.TIP_ACESSO_3_TELA);
							vo.setDesIpServidorRequest(req.getLocalAddr());
							vo.setNumPortaServidorRequest(req.getLocalPort());
							if (menuAcessoVO != null) {
								vo.setNumRotina(menuAcessoVO.getRotina());
							}
							session.gravaLoginLogout(xcpSessionBacking.getEjbVars(), vo);
							xcpSessionBacking.setCurrentViewId(viewId);
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}
			}
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		FacesContext ctx = event.getFacesContext();
		ExternalContext exCtx = ctx.getExternalContext();
		if (!exCtx.getRequestMap().containsKey(PARAM_XCP_TEMPLATE_NAME)) {
			String templateName;
			if (exCtx.getRequestParameterMap().containsKey(PARAM_XCP_TEMPLATE_NAME)) {
				templateName = exCtx.getRequestParameterMap().get(PARAM_XCP_TEMPLATE_NAME);
			} else if (exCtx.getFlash().containsKey(PARAM_XCP_TEMPLATE_NAME)) {
				templateName = (String) exCtx.getFlash().get(PARAM_XCP_TEMPLATE_NAME);
			} else {
				templateName = "manut";
			}
			exCtx.getRequestMap().put(PARAM_XCP_TEMPLATE_NAME, templateName);
		}
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			if (ctx.getExternalContext().getRequestServletPath().contains("/secure/XcpTraducaoSmart.xhtml")) {
				ctx.getExternalContext().getFlash().put("chaves_traducao", XcpTraducaoCache.getInstance().getChavesAcessadasSmart());
			}
		}
	}

	private void sendToAzure(HttpServletResponse resp, String tipoAcesso) throws Exception {

		XcpSession session = (XcpSession) EJBLookup.getInstance().get(XcpSession.JNDI_NAME);

		XcpParametrosSistema xcpParUrl = null;
		XcpParametrosSistema xcpParId = null;

		if ("2".equals(tipoAcesso)) {
			xcpParUrl = session.findXcpParametrosSistema(null, null, "Azure", 3);
			xcpParId = session.findXcpParametrosSistema(null, null, "Azure", 5);
		} else {
			xcpParUrl = session.findXcpParametrosSistema(null, null, "Azure", 2);
			xcpParId = session.findXcpParametrosSistema(null, null, "Azure", 4);
		}

		String desUrl = xcpParUrl.getDesParametro();
		String desIdApp = xcpParId.getDesParametro();
		String desIdReq = "_" + UUID.randomUUID().toString();
		Date dthReq = new Date();
		String xml = this.getXmlAzure(desIdApp, desIdReq, dthReq);

		XcpPersistSession persist = (XcpPersistSession) EJBLookup.getInstance().get(XcpPersistSession.JNDI_NAME);

		XcpAzureReq xcpreq = new XcpAzureReq();
		xcpreq.setIndErro(0);
		xcpreq.setDesMsg(xml);
		xcpreq.setDthMsg(dthReq);
		xcpreq.setTipSentido(XcpAzureReq.TIP_SENTIDO_1_SIST_AZURE);
		xcpreq.setDesId(desIdReq);
		xcpreq = persist.update(null, xcpreq);

		HashMap parameters = new HashMap<String, String>();
		parameters.put("SAMLRequest", xml);
		try {
			sendRedirect(resp, desUrl, parameters);
		} catch (Exception e) {
			xcpreq.setIndErro(1);
			xcpreq.setDesErro(Formatter.getStackTrace(e));
			persist.update(null, xcpreq);
		}
	}

	private String getXmlAzure(String desIdApp, String desIdReq, Date dthReq) throws Exception {

		String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dthReq) + "Z";

		StringBuilder sb = new StringBuilder();
		sb.append("<samlp:AuthnRequest ");
		sb.append(" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ");
		sb.append(" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" ");
		sb.append(" ID=\"");
		sb.append(desIdReq);
		sb.append("\" ");
		sb.append(" IssueInstant=\"");
		sb.append(date);
		sb.append("\"");
		sb.append(" Version=\"2.0\" ");
		sb.append(" AssertionConsumerServiceIndex=\"0\" >");
		sb.append("<saml:Issuer>");
		sb.append(desIdApp);
		sb.append("</saml:Issuer>");
		sb.append("<samlp:NameIDPolicy Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\"/> ");
		sb.append("</samlp:AuthnRequest> ");

		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
		deflaterStream.write(sb.toString().getBytes(Charset.forName("UTF-8")));
		deflaterStream.finish();
		return DatatypeConverter.printBase64Binary(bytesOut.toByteArray());

	}

	private static String sendRedirect(HttpServletResponse response, String location, Map<String, String> parameters) throws IOException {
		String target = location;

		if (!parameters.isEmpty()) {
			boolean first = !location.contains("?");
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				if (first) {
					target += "?";
					first = false;
				} else {
					target += "&";
				}
				target += parameter.getKey();
				if (!parameter.getValue().isEmpty()) {
					target += "=" + urlEncoder(parameter.getValue());
				}
			}
		}

		response.sendRedirect(target);

		return target;
	}

	public static String urlEncoder(String input) {
		if (input != null) {
			try {
				return URLEncoder.encode(input, "UTF-8");
			} catch (UnsupportedEncodingException e) {

				throw new IllegalArgumentException();
			}
		} else {
			return null;
		}
	}

}
