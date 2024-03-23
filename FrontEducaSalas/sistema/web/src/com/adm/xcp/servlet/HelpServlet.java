package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.session.local.XcpSession;
import com.adm.util.Converter;
import com.adm.xcp.backing.XcpSessionBacking;

@WebServlet("/help")
public class HelpServlet extends HttpServlet {

	@EJB
	private XcpSession session;

	private String replaceUnwantedChars(String string) {
		if (string == null) {
			return null;
		}
		//espaco
		string = string.replace("%20", " ");
		//tentativa de invasao
		string = string.replace("/..", "");

		return string;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ServletOutputStream out = resp.getOutputStream();
			HttpSession session = req.getSession(true);
			XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);

			if (back == null || back.getUsuario() == null || back.getEmpresa() == null || back.getEmpresa().getEmpresa() == null) {
				out.println("Sem permissao de acesso");
				return;
			}

			XcpParametrosSistema xcpPar = this.session.findXcpParametrosSistema(null, back.getEmpresa().getEmpresa(), "Sistema", 3);
			String desPath = null;
			if (xcpPar != null) {
				desPath = xcpPar.getDesParametro();
			}
			//desPath = "/servers/helps";
			if (desPath == null) {
				out.println("Caminho dos helps nao cadastrado nos parametros do sistema");
				return;
			}

			String img = req.getParameter("img");
			if (img != null) {
				img = Converter.fromHex(img);
				img = this.replaceUnwantedChars(img);
				Path path = Paths.get(desPath, img);
				if (!path.toFile().exists()) {
					return;
				}

				resp.setContentType("image/jpeg");
				try (FileInputStream fis = new FileInputStream(path.toFile());
						BufferedInputStream input = new BufferedInputStream(fis, 10240);
						BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), 10240)) {

					byte[] buffer = new byte[10240];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				}

				return;

			}

			String nomeHelp = null;
			String nomeHelpHex = req.getParameter("nomeHelpH");
			if (nomeHelpHex != null) {
				nomeHelp = Converter.fromHex(nomeHelpHex);
			} else {
				nomeHelp = req.getParameter("nomeHelp");
			}

			nomeHelp = this.replaceUnwantedChars(nomeHelp);
			String numHelp = req.getParameter("numHelp");

			if (numHelp == null && nomeHelp == null) {
				out.println("Informacao do help nao recebida");
				return;
			}

			String help = numHelp == null ? nomeHelp : numHelp;

			Path path = Paths.get(desPath, help + ".html");

			if (!path.toFile().exists()) {
				out.println("Help " + help + ".html nao existe");
				return;
			}

			List<String> readAllLines = Files.readAllLines(path, Charset.forName("ISO-8859-1"));

			Pattern p = Pattern.compile("src=\"(.*?)\"");
			Pattern phtml = Pattern.compile("href=\"(.*?).html\"");

			for (String s : readAllLines) {
				// trata as imagens
				Matcher m = p.matcher(s);
				while (m.find()) {
					String srcVal = m.group(1);

					if (srcVal.startsWith(".")) {
						srcVal = srcVal.substring(1, srcVal.length());
					}

					if (!srcVal.startsWith("/")) {
						srcVal = "/" + srcVal;
					}

					StringBuilder sb = new StringBuilder();
					sb.append("src= \"");
					sb.append(req.getRequestURL().toString());
					sb.append("?img=");
					sb.append(Converter.toHex(srcVal));
					sb.append("\"");
					s = m.replaceFirst(sb.toString());
				}

				// trata os links
				m = phtml.matcher(s);
				while (m.find()) {
					String srcVal = m.group(1);

					if (srcVal.startsWith(".")) {
						srcVal = srcVal.substring(1, srcVal.length());
					}

					if (!srcVal.startsWith("/")) {
						srcVal = "/" + srcVal;
					}

					StringBuilder sb = new StringBuilder();
					sb.append("href=\"");
					sb.append(req.getRequestURL().toString());
					sb.append("?nomeHelpH=");
					sb.append(Converter.toHex(srcVal));
					sb.append("\"");
					s = m.replaceFirst(sb.toString());
				}

				out.println(s);
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}
}
