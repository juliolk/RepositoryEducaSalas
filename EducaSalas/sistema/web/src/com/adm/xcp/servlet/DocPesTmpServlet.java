package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;

import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Constants;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpSessionBacking;

@WebServlet("/docpestmp")
public class DocPesTmpServlet extends HttpServlet {

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

			String id = req.getParameter("id");

			if (Util.isEmpty(id)) {
				resp.setContentType("image/jpeg");
				try (BufferedInputStream input = new BufferedInputStream(this.getServletContext().getResourceAsStream("/resources/img/semImagem.jpg"), 10240);
						BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), 10240)) {
					byte[] buffer = new byte[10240];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				}
				return;
			}

			Path p = Paths.get(Constants.PATH_TEMPORARY_FILES).resolve(id + ".tmp");

			try (FileInputStream input = new FileInputStream(p.toFile())) {
				Detector detector = new DefaultDetector();
				TikaInputStream tis = TikaInputStream.get(input);
				MediaType mediaType = detector.detect(tis, new Metadata());
				TikaConfig config = TikaConfig.getDefaultConfig();
				MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
				resp.setContentType(mimeType.getName());
			}

			try (FileInputStream input = new FileInputStream(p.toFile()); BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), 10240)) {
				byte[] buffer = new byte[10240];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}
}
