package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.session.local.XcpSession;
import com.adm.util.Converter;
import com.adm.xcp.backing.XcpSessionBacking;
import com.adm.xcp.servlet.exception.XcpHelpException;

@WebServlet(urlPatterns = { "/help/*" })
public class XcpHelpServlet extends HttpServlet {

	@EJB
	private XcpSession session;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		try {

			String helpPath = null;
			Transformer transformer = null;

			HttpSession session = request.getSession(true);

			try {
				XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);
				if (back == null || back.getUsuario() == null || back.getEmpresa() == null || back.getEmpresa().getEmpresa() == null) {
					out.print("Sem permissão de acesso");
					return;
				}

				XcpParametrosSistema xcpPar = this.session.findXcpParametrosSistema(null, back.getEmpresa().getEmpresa(), "Sistema", 3);

				if (xcpPar != null) {
					helpPath = xcpPar.getDesParametro();
				}
				if (helpPath == null) {
					throw new XcpHelpException("Caminho dos helps não cadastrado nos parâmetros do sistema");
				}

				TransformerFactory tFactory = TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", this.getClass().getClassLoader());
				transformer = tFactory.newTransformer(new StreamSource(new File(helpPath + "/xslt/help.xsl")));

				String help;
				Pattern pattern = Pattern.compile("/help/h/(\\w+)");
				Matcher matcher = pattern.matcher(request.getRequestURI());
				if (matcher.find()) {
					help = Converter.fromHex(matcher.group(1));
				} else {

					pattern = Pattern.compile("/help/f/(\\w+)");
					matcher = pattern.matcher(request.getRequestURI());
					if (matcher.find()) {
						String file = Converter.fromHex(matcher.group(1));
						InputStream is = new BufferedInputStream(new FileInputStream(new File(helpPath, file)));

						try (BufferedInputStream input = new BufferedInputStream(is, 10240); BufferedOutputStream output = new BufferedOutputStream(out, 10240)) {

							byte[] buffer = new byte[10240];
							int length;
							while ((length = input.read(buffer)) > 0) {
								output.write(buffer, 0, length);
							}
						}
						return;
					} else {
						pattern = Pattern.compile("/help/(\\w+)");
						matcher = pattern.matcher(request.getRequestURI());
						if (matcher.find()) {
							help = matcher.group(1);
						} else {
							throw new XcpHelpException("Informação do help não recebida");
						}
					}
				}

				transformer.clearParameters();
				transformer.setParameter("contexto", request.getContextPath());

				StreamSource source = new StreamSource(getHelpFile(helpPath, help));

				transformer.transform(source, new StreamResult(out));

			} catch (XcpHelpException e) {
				if (transformer != null) {
					transformer.setParameter("contexto", request.getContextPath());
					transformer.setParameter("erro", e.getMessage());
					transformer.transform(new StreamSource(getHelpFile(helpPath, "error")), new StreamResult(out));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace(new PrintStream(out));
		}
	}

	public static File getHelpFile(String helpPath, String help) throws XcpHelpException {
		File f = new File(helpPath, help + ".xml");
		if (!f.canRead()) {
			throw new XcpHelpException("Help não encontrado: " + f.getAbsolutePath());
		}
		return f;
	}

}
