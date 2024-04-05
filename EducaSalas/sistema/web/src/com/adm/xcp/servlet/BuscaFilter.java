package com.adm.xcp.servlet;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.util.Util;

@WebFilter({ "/busca/*", "/probatorio/*", "/funcionario/*", "/transparencia/*", "/operador/*" })
public class BuscaFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpreq = (HttpServletRequest) req;
		HttpServletResponse httpresp = (HttpServletResponse) resp;
		String auth = httpreq.getHeader("Authorization");
		if (Util.isEmpty(auth)) {
			httpresp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String token = auth.substring("Bearer".length()).trim();
		if (Util.isEmpty(token)) {
			httpresp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		SecretKey key = Keys.hmacShaKeyFor(TokenServlet.SECRET_KEY.getBytes(StandardCharsets.UTF_8));

		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		} catch (Exception e) {
			httpresp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {
		// ignora
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// ignora
	}

}
