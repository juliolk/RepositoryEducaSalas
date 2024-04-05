package com.adm.xcp.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.session.XcpServerCacheSessionBean;
import com.adm.ejb.session.local.XcpSession;
import com.adm.util.version.Version;

@WebServlet(urlPatterns = { "/public/version" })
public class VersionServlet extends HttpServlet {

	@EJB
	private XcpSession session;

	@EJB
	private XcpServerCacheSessionBean xcpServer;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			PrintWriter writer = response.getWriter();
			Version instance = Version.getInstance();

			String anyVersion = this.xcpServer.getScriptVersion(null, XcpServerCacheSessionBean.CTX_ANY);
			String earVersion = this.xcpServer.getScriptVersion(null, XcpServerCacheSessionBean.CTX_EAR);

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			StringBuilder sb = new StringBuilder();
			sb.append("<head>");
			sb.append("Informações da geração");
			sb.append("</head>");
			sb.append("<body>");
			sb.append("<table style=\"width:500px\">");

			sb.append("<tr>");
			sb.append("<td>Data</td>");
			sb.append("<td>");
			sb.append("<b>");
			sb.append(sdf.format(instance.getBuildDate()));
			sb.append("</b>");
			sb.append("</td>");
			sb.append("</tr>");

			sb.append("<tr>");
			sb.append("<td>Usuario</td>");
			sb.append("<td>");
			sb.append(instance.getBuildUser());
			sb.append("</td>");
			sb.append("</tr>");

			sb.append("<tr>");
			sb.append("<td>OS</td>");
			sb.append("<td>");
			sb.append(instance.getBuildOS());
			sb.append("</td>");
			sb.append("</tr>");

			sb.append("<tr>");
			sb.append("<td>Versao script avulso</td>");
			sb.append("<td>");
			sb.append(anyVersion);
			sb.append("</td>");
			sb.append("</tr>");

			sb.append("<tr>");
			sb.append("<td>Versao script EAR</td>");
			sb.append("<td>");
			sb.append(earVersion);
			sb.append("</td>");
			sb.append("</tr>");

			sb.append("</table>");
			sb.append("</body>");
			sb.append("</html>");
			writer.print(sb.toString());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
