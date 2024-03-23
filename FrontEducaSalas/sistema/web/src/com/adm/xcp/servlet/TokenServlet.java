package com.adm.xcp.servlet;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.entity.Operadores;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.gui.vo.TokenVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.google.gson.Gson;

@WebServlet("/token")
public class TokenServlet extends HttpServlet {

	public static final String SECRET_KEY = "mYq3t6w9z$C&E)H@McQfTjWnZr4u7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+MbPe";

	@EJB
	private XcpQuerySession xcpQuerySession;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String body = this.getBody(req);
		if (Util.isEmpty(body)) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Gson gson = new Gson();
		TokenVO tokenreq = null;

		try {
			tokenreq = gson.fromJson(body, TokenVO.class);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (Util.isEmpty(tokenreq.getUsuario()) || Util.isEmpty(tokenreq.getSenha())) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		MontaQuery q = new MontaQuery("Select e from Operadores e");
		if (tokenreq.getEmpresa() != null) {
			q.addWhere("empresa", "=", tokenreq.getEmpresa());
		} else {
			q.addWhere("empresa", "=", 1);
		}
		q.addWhere("nome", "like", tokenreq.getUsuario());
		q.addWhere("indUsuIntegracao", "=", 1);

		List list = this.xcpQuerySession.executeQueryList(null, q);
		if (Util.isEmpty(list)) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Operadores oper = (Operadores) list.get(0);

		if (!this.isValidPassword(tokenreq.getSenha(), oper.getSenha())) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		Date dataexp = cal.getTime();
		String jwtToken = Jwts.builder().setSubject(tokenreq.getUsuario()).setIssuedAt(new Date()).setExpiration(dataexp).signWith(key).compact();
		resp.setContentType("application/json");
		resp.getOutputStream().print(jwtToken);
	}

	private String getBody(HttpServletRequest req) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = req.getReader()) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private boolean isValidPassword(String senha, String md5banco) {
		String typedPass = Util.getMD5(senha);

		if (typedPass.equals(md5banco)) {
			return true;
		}

		typedPass = Util.getMD5Correto(senha);

		if (typedPass.equals(md5banco)) {
			return true;
		}

		return false;
	}
}
