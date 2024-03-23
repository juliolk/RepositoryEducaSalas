package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;

@WebServlet("/img")
public class ImgServlet extends HttpServlet {

	@EJB
	private XcpQuerySession session;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			int cacheAge = 60 * 60 * 24;
			long expiry = new Date().getTime() + cacheAge * 1000;

			resp.setDateHeader("Expires", expiry);
			resp.setHeader("Cache-Control", "max-age=" + cacheAge);

			//			ServletOutputStream out = resp.getOutputStream();
			//			HttpSession session = req.getSession(true);

			String pars = req.getParameter("pars");
			String fromHex = Converter.fromHex(pars);
			String[] split = fromHex.split("[|]");
			String caminho = split[0];

			InputStream is = new BufferedInputStream(new FileInputStream(caminho));

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
