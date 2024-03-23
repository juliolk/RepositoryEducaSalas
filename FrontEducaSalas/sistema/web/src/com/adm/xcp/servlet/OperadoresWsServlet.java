package com.adm.xcp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.entity.Operadores;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.gui.vo.operws.OperadorReq;
import com.adm.gui.vo.operws.OperadorResp;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = { OperadoresWsServlet.INCLUI, OperadoresWsServlet.ALTERA })
public class OperadoresWsServlet extends HttpServlet {

	public static final String INCLUI = "/operador/inclui";
	public static final String ALTERA = "/operador/altera";

	private static final String OK = "OK";
	private static final String NOK = "NOK";

	@EJB
	private XcpQuerySession query;

	@EJB
	private XcpPersistSession persist;

	@EJB
	private OperadoresSession operadoresSession;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			String body = this.getBody(req);

			if (req.getRequestURI().endsWith(INCLUI)) {
				this.inclui(body, resp);
			} else if (req.getRequestURI().endsWith(ALTERA)) {
				this.altera(body, resp);
			}

			resp.setContentType("application/json");

		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().write(e.getMessage());
			e.printStackTrace(resp.getWriter());
		}

	}

	private void inclui(String body, HttpServletResponse resp) throws IOException {
		OperadorResp respvo = new OperadorResp();
		try {
			OperadorReq reqvo = this.getGson().fromJson(body, OperadorReq.class);

			if (reqvo.getCodUsuario() == null) {
				respvo.setStatus(NOK);
				respvo.setMsg("Campo codigo do usuario obrigatorio");
				return;
			}

			if (Util.isEmpty(reqvo.getLogin())) {
				respvo.setStatus(NOK);
				respvo.setMsg("Campo Login obrigatorio");
				return;
			}

			if (reqvo.getCpf() == null) {
				respvo.setStatus(NOK);
				respvo.setMsg("Campo CPF obrigatorio");
				return;
			}

			if (!this.isValidoNome(reqvo.getLogin(), null)) {
				respvo.setStatus(NOK);
				respvo.setMsg("Login ja existente");
				return;
			}

			this.operadoresSession.criaUsuIntegracao(null, reqvo.getCodUsuario(), reqvo.getLogin(), reqvo.getCpf());

			respvo.setStatus(OK);

		} catch (Exception e) {
			e.printStackTrace();
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		} finally {

			if (NOK.equals(respvo.getStatus())) {
				resp.setStatus(500);
			}

			resp.getWriter().write(this.getGson().toJson(respvo));
		}

	}

	private void altera(String body, HttpServletResponse resp) throws IOException {
		OperadorResp respvo = new OperadorResp();

		try {
			OperadorReq reqvo = this.getGson().fromJson(body, OperadorReq.class);
			if (reqvo.getCodUsuario() == null) {
				respvo.setStatus(NOK);
				respvo.setMsg("Codigo do usuario obrigatorio");
				return;
			}

			MontaQuery q = new MontaQuery("Select e from Operadores e");
			q.addWhere("codUsuarioIntegracao", "=", reqvo.getCodUsuario());
			List<Operadores> list = this.query.executeQueryList(null, q);
			if (Util.isEmpty(list)) {
				respvo.setStatus(NOK);
				respvo.setMsg("Usuario nao encontrado pelo seu codigo de usuario");
				return;
			}

			for (Operadores oper : list) {
				if (reqvo.getLogin() != null) {
					oper.setNome(reqvo.getLogin());
				}

				if (reqvo.getAtivo() != null) {
					oper.setSituacao(reqvo.getAtivo() ? Operadores.SITUACAO_1_ATIVO : Operadores.SITUACAO_0_INATIVO);
				}

				this.persist.update(null, oper);
			}

			respvo.setStatus(OK);

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		} finally {

			if (NOK.equals(respvo.getStatus())) {
				resp.setStatus(500);
			}

			resp.getWriter().write(this.getGson().toJson(respvo));
		}

	}

	private boolean isValidoNome(String nome, Long operador) {
		MontaQuery q = new MontaQuery("select 1 from Operadores e");
		q.addWhere("nome", "=", nome);
		if (operador != null) {
			q.addWhere("operador", "<>", operador);
		}
		q.addWhere("permissao", "=", Operadores.PERMISSAO_1_SISTEMA);
		if (this.query.existe(null, q)) {
			return false;
		}

		return true;
	}

	private Gson getGson() throws IOException {
		return new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
	}

	private String getBody(HttpServletRequest req) throws IOException {
		try (ServletInputStream inputStream = req.getInputStream(); ByteArrayOutputStream result = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[1024];
			for (int length; (length = inputStream.read(buffer)) != -1;) {
				result.write(buffer, 0, length);
			}
			return result.toString();
		}
	}

}
