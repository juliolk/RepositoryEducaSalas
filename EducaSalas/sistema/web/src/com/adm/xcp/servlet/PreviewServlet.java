package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Constants;
import com.adm.xcp.backing.XcpSessionBacking;

@SuppressWarnings("serial")
@WebServlet("/prev")
public class PreviewServlet extends HttpServlet {

	@EJB
	private XcpQuerySession session;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ServletOutputStream out = resp.getOutputStream();
		HttpSession session = req.getSession(true);
		XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);

		if (back.getUsuario() == null) {
			out.println("Sem permissao de acesso");
			return;
		}

		Object seqproc = req.getParameter("seqproc");
		Path p = Paths.get(Constants.PATH_TEMPORARY_FILES).resolve(seqproc + ".pdf");
		if (p.toFile().exists()) {
			InputStream is = new BufferedInputStream(new FileInputStream(p.toFile()));
			try (BufferedInputStream input = new BufferedInputStream(is, 10240);
					BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), 10240)) {

				byte[] buffer = new byte[10240];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
			}
		}
	}
}
