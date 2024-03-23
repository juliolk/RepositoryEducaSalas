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

import com.adm.ejb.entity.extend.DocumentosPortalManut;
import com.adm.util.Converter;
import com.adm.xcp.util.XcpPortalCache;

@WebServlet("/docportal")
public class DocPortalServlet extends HttpServlet implements com.adm.util.Constants {

	public static final Integer PARAM_TIPO_IMAGEM = 1;
	public static final Integer PARAM_TIPO_ARQUIVO = 2;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			String pars = req.getParameter("pars");

			String fromHex = Converter.fromHex(pars);
			String[] split = fromHex.split("[|]");

			Date publicacao = null;
			Integer tipo = null;

			if (split.length == 2) {
				publicacao = Converter.toDateTime(split[0], true);
				tipo = Converter.toInteger(split[1]);
			}
			DocumentosPortalManut doc = null;
			for (DocumentosPortalManut d : XcpPortalCache.getInstance().getListaDocumentos()) {
				if (d.getPublicacao().compareTo(publicacao) == 0) {
					doc = d;
					break;
				}
			}
			if (doc != null) {
				byte[] bytes = null;
				if (PARAM_TIPO_IMAGEM.equals(tipo)) {
					bytes = doc.getImagem();
				} else if (PARAM_TIPO_ARQUIVO.equals(tipo)) {
					bytes = doc.getArquivo();
					resp.setContentType("application/x-download");

					StringBuilder sb = new StringBuilder();
					sb.append("attachment;");

					String desArquivo = doc.getDesArquivo();
					if (desArquivo != null) {
						//Remove aspas duplas
						desArquivo = desArquivo.replace("\"", " ");
						sb.append(" filename=\"");
						sb.append(desArquivo);
						sb.append("\"");
					}

					resp.setHeader("Content-Disposition", sb.toString());
				}
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
