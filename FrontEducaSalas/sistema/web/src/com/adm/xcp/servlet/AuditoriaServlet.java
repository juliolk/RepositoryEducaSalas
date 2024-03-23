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

import com.adm.ejb.entity.Auditoria;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpSessionBacking;

@WebServlet("/audit")
public class AuditoriaServlet extends HttpServlet {

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

			Long operador = null;
			Long numLog = null;
			String campo = null;
			for (String string : split) {
				String[] arr = string.split("=");
				if (arr[0].equals("operador")) {
					operador = Converter.toLong(arr[1]);
				} else if (arr[0].equals("numLog")) {
					numLog = Converter.toLong(arr[1]);
				} else if (arr[0].equals("campo")) {
					campo = arr[1];
				}
			}
			
			if (!operador.equals(back.getUsuario().getOperador())) {
				out.println("Sem permissao de acesso");
				return;
			}

			MontaQuery mq = new MontaQuery("Select e from Auditoria e");
			mq.addWhere("numlog", "=", numLog);

			//resp.setContentType("image/jpeg");

			List list = this.session.executeQueryList(null, mq);

			InputStream is = null;
			StringBuilder sbFilename = new StringBuilder();
			sbFilename.append("audit-");
			String campoTabela = "";
			if (!Util.isEmpty(list)) {
				Auditoria p = (Auditoria) list.get(0);
				campoTabela = p.getCampo().toLowerCase();
				sbFilename.append(p.getCampo().replace('.', '-').toLowerCase());
				if (campo.equals("anterior_blob") && p.getAnteriorBlob() != null) {
					is = new ByteArrayInputStream(p.getAnteriorBlob());
					sbFilename.append("-anterior");
				}
				else if (campo.equals("atual_blob") && p.getAtualBlob() != null) {
					is = new ByteArrayInputStream(p.getAtualBlob());
					sbFilename.append("-atual");
				}
				else if (campo.equals("anterior_clob") && p.getAnteriorClob() != null) {
					is = new ByteArrayInputStream(p.getAnteriorClob().getBytes());
					sbFilename.append("-anterior");
				}
				else if (campo.equals("atual_clob") && p.getAtualClob() != null) {
					is = new ByteArrayInputStream(p.getAtualClob().getBytes());
					sbFilename.append("-atual");
				}
				
			}
			
	        Detector detector = new DefaultDetector();
	        TikaInputStream tis = TikaInputStream.get(is);
			MediaType mediaType = detector.detect(tis, new Metadata());
			
			TikaConfig config = TikaConfig.getDefaultConfig();
			MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());

			// Tratamento para arquivos jasper (jrxml) na xcp_objeto_modelo
			if (mimeType.getExtension().equals(".xml") && campoTabela.equals("xcp_objeto_modelo.obj_fonte")) {
				sbFilename.append(".jrxml");
			}
			else {
				sbFilename.append(mimeType.getExtension());
			}
			resp.setContentType(mimeType.getName());
			resp.addHeader("content-disposition", "attachment; filename=" + sbFilename.toString());

			if (is == null) {
				is = this.getServletContext().getResourceAsStream("/resources/img/semImagem.jpg");
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
