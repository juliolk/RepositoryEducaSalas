package com.adm.xcp.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.session.XcpIntegracaoSingleton;
import com.adm.ejb.session.XcpKeysSingleton;
import com.adm.ejb.session.local.XcpSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.ChaveVO;
import com.adm.ejb.vo.xcape.Callback;
import com.adm.util.Converter;
import com.adm.util.Util;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.util.XcpViewConstants;
import com.google.gson.Gson;

@WebServlet(urlPatterns = { XcpIntegracaoCallBackServlet.URL })
public class XcpIntegracaoCallBackServlet extends HttpServlet {

	public static final String URL = "/integracaocallback";
	public static final String LOGOUT = "LOGOUT";
	private static final String PG_LOGOUT = "public/xcp/XcpLogout.xhtml";

	@EJB
	private XcpIntegracaoSingleton xcpIntegracaoSingleton;

	@EJB
	private XcpKeysSingleton xcpKeysSingleton;

	@EJB
	private XcpQuerySession xcpQuerySession;

	@EJB
	private XcpSession session;

	private static final Logger logger = Logger.getLogger(XcpIntegracaoCallBackServlet.class.getName());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String body = this.getBody(req);

		if (Util.isEmpty(body)) {
			return;
		}

		Gson gson = new Gson();
		Callback callback = gson.fromJson(body, Callback.class);

		ChaveVO chaveVO = this.xcpIntegracaoSingleton.get(null, callback.getKey());
		if (chaveVO == null) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (Util.isEmpty(callback.getId())) {
			resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			return;
		}

		StringBuilder sburl = new StringBuilder();

		if ("LOGOUT".equals(callback.getId())) {
			String desUrlBase = this.getDesUrlBase(req);
			sburl.append(desUrlBase);
			sburl.append(PG_LOGOUT);
		} else {

			Rotinas rotina = this.xcpQuerySession.find(null, Rotinas.class, Converter.toInteger(callback.getId()));

			if (rotina == null) {
				resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
				return;
			}

			if (Rotinas.TIP_ROTINA_3_PAGINA_XCP.equals(rotina.getTipRotina())) {
				sburl.append("XPAGE=" + rotina.getCodPaginaXcp());
			} else {

				logger.info("PASSEI AQUI 1...");
				//
				//			HttpSession session = req.getSession(true);
				//			XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);
				//			if (back == null || back.getUsuario() == null || back.getEmpresa() == null || back.getEmpresa().getEmpresa() == null) {
				//				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				//				return;
				//			}

				String desUrl = this.getDesUrlBase(req);

				logger.info("URL: " + desUrl);

				sburl.append(desUrl);
				sburl.append("secure/");

				if (Rotinas.TIP_ROTINA_1_PAGINA.equals(rotina.getTipRotina())) {
					sburl.append(rotina.getComponente());
				} else if (Rotinas.TIP_ROTINA_2_OBJETO.equals(rotina.getTipRotina())) {
					sburl.append(XcpViewConstants.PG_EXECUCAO_OBJETO_COMPONENTE);
					sburl.append("?com.xcape.codObjeto=");
					sburl.append(rotina.getCodObjeto());
				}

				String desChaveTmp = UUID.randomUUID().toString();

				ChaveVO vo = new ChaveVO();
				vo.setDesUsuario(chaveVO.getDesUsuario());
				vo.setDtaCheck(new Date());
				vo.setDesChave(desChaveTmp);
				this.xcpKeysSingleton.put(null, vo);

				sburl.append("?xkey=");
				sburl.append(vo.getDesChave());
			}
		}

		resp.getWriter().write(sburl.toString());
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	private String getBody(HttpServletRequest req) throws IOException {
		req.setCharacterEncoding("UTF-8");
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = req.getReader()) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private String getDesUrlBase(HttpServletRequest req) {
		XcpParametrosSistema xcpPar = null;
		try {
			String tipoAcesso = System.getProperty("com.xcape.TIPO_ACESSO");
			if ("2".equals(tipoAcesso)) {
				xcpPar = this.session.findXcpParametrosSistema(null, 1, "Portal Servidor", 10);
			} else {
				xcpPar = this.session.findXcpParametrosSistema(null, 1, "Sistema", 16);
			}
		} catch (ParametroNaoEncontradoException e) {
			logger.info("NAO ACHOU PARAMETRO: ");
			//ignora
		}

		String desUrl = null;
		if (xcpPar != null) {
			if (!Util.isEmpty(xcpPar.getDesParametro())) {
				desUrl = xcpPar.getDesParametro();
			}
		}

		//Se nao vier dos parametros, tenta responder com a url da chamada
		//(pode ter problemas caso as aplicacoes estejam na mesma maquina [url interna x externa])
		if (Util.isEmpty(desUrl)) {
			desUrl = req.getRequestURL().toString().replace(URL, "");
		}

		if (!Util.isEmpty(desUrl)) {
			if (!desUrl.endsWith("/")) {
				desUrl += "/";
			}
		}

		return desUrl;
	}

}
