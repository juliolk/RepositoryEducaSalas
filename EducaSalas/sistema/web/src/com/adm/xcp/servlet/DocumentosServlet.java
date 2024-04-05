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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;

import com.adm.ejb.entity.extend.DocumentosBlob;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpSessionBacking;

@WebServlet("/documentos")
public class DocumentosServlet extends HttpServlet {

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
			String[] split = fromHex.split("[|]");

			Long registro = null;
			String detect = null;
			for (String string : split) {
				String[] arr = string.split("=");
				if (arr[0].equals("registro")) {
					registro = Converter.toLong(arr[1]);
				} else if (arr[0].equals("detect")) {
					detect = arr[1];
				}
			}

			MontaQuery mq = new MontaQuery("Select e from DocumentosBlob e");
			mq.addWhere("registro", "=", registro);

			List list = this.session.executeQueryList(null, mq);

			InputStream is = null;
			if (!Util.isEmpty(list)) {
				DocumentosBlob p = (DocumentosBlob) list.get(0);
				if (p.getDocumento() != null) {
					is = new ByteArrayInputStream(p.getDocumento());
				}
			}

			if (is == null) {
				out.print("Documento nao encontrado!");
				resp.setContentType("text/plain");
				return;
			}

			if ("true".equals(detect)) {
				Detector detector = new DefaultDetector();
				TikaInputStream tis = TikaInputStream.get(is);
				MediaType mediaType = detector.detect(tis, new Metadata());
				TikaConfig config = TikaConfig.getDefaultConfig();
				MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
				resp.setContentType(mimeType.getName());
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
