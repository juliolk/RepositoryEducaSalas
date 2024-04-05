package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.entity.NoticiasPortal;
import com.adm.util.Converter;
import com.adm.xcp.util.XcpPortalCache;

@WebServlet("/noticiaportal")
public class NoticiaPortalServlet extends HttpServlet implements com.adm.util.Constants {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int cacheAge = 60 * 60 * 24;
			long expiry = new Date().getTime() + cacheAge * 1000;

			resp.setDateHeader("Expires", expiry);
			resp.setHeader("Cache-Control", "max-age=" + cacheAge);

			String pars = req.getParameter("pars");

			String fromHex = Converter.fromHex(pars);

			Date publicacao = null;

			publicacao = Converter.toDateTime(fromHex, true);
			
			NoticiasPortal noticia = null;
			for (NoticiasPortal n : XcpPortalCache.getInstance().getListaNoticias()) {
				if (n.getPublicacao().compareTo(publicacao) == 0) {
					noticia = n;
					break;
				}
			}
			if (noticia != null) {
				byte[] bytes = noticia.getImagem();
				if (bytes != null) {
					resp.getOutputStream().write(bytes);
					resp.flushBuffer();
					return;
				}
			}

			InputStream is = this.getServletContext().getResourceAsStream("/resources/img/semImagem.jpg");

			try (BufferedInputStream input = new BufferedInputStream(is, 10240);
					BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), 10240)) {

				byte[] buffer = new byte[10240];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
