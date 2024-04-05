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

import com.adm.ejb.entity.Documentospessoais;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.remote.FuncionariosSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

@WebServlet("/fotovalidador")
public class FotoPublicaValidadorServlet extends HttpServlet {

	@EJB
	private XcpQuerySession session;
	
	@EJB
	private FuncionariosSession funcionariosSession;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ServletOutputStream out = resp.getOutputStream();
			String pars = req.getParameter("pars");

			String fromHex = Converter.fromHex(pars);
			String[] split = fromHex.split("[|]");
			Integer empresa;
			Long matricula;
			String hash;
			
			if(!split[0].equals("vcf")) {
				out.println("Tipo Invalido");
				return;
			}

			empresa = Integer.valueOf(split[1]);
			matricula = Long.valueOf(split[2]);
			hash = split[3];
			
			FuncionariosPK pk = new FuncionariosPK(empresa, matricula);
			if(!this.funcionariosSession.validaHashFuncionario(null, pk, hash)) {
				out.println("Hash Invalido");
				return;
			}

			MontaQuery mq = new MontaQuery("Select e from Documentospessoais e");
			mq.addWhere("empresa", "=", empresa);
			mq.addWhere("matricula", "=", matricula);
			mq.addWhere("tipdocumento", "=", Documentospessoais.TIPDOCUMENTO_1_FOTO);

			//resp.setContentType("image/jpeg");

			List list = this.session.executeQueryList(null, mq);

			InputStream is = null;
			if (!Util.isEmpty(list)) {
				Documentospessoais p = (Documentospessoais) list.get(0);
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
