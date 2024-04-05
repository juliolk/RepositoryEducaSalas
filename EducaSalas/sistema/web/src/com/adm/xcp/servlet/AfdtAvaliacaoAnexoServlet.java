package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.adm.ejb.entity.AfdtAvaliacaoAnexoBlob;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpSessionBacking;

@WebServlet("/afdtavaliacaoanexo")
public class AfdtAvaliacaoAnexoServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	@EJB
	private XcpQuerySession session;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ServletOutputStream out = resp.getOutputStream();
			HttpSession session = req.getSession(true);
			XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);

			if (back.getUsuario() == null) {
				out.println("Sem permissao de acesso");
				return;
			}

			String pars = req.getParameter("pars");

			String fromHex = Converter.fromHex(pars);

			Long seq = Converter.toLong(fromHex);					
			

			MontaQuery mq = new MontaQuery("Select e from AfdtAvaliacaoAnexoBlob e");
			mq.addWhere("seq", "=", seq);

			List list = this.session.executeQueryList(null, mq);

			InputStream is = null;
			if (!Util.isEmpty(list)) {
				AfdtAvaliacaoAnexoBlob p = (AfdtAvaliacaoAnexoBlob) list.get(0);
				if (p.getArquivo() != null) {
					is = new ByteArrayInputStream(p.getArquivo());
				}
			}

			if (is == null) {
				is = getServletContext().getResourceAsStream("/resources/img/semImagem.jpg");
			}

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
