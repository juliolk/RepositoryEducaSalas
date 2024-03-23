package com.adm.xcp.servlet;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.ejb.EJB;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.xcp.backing.XcpSessionBacking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;


@SuppressWarnings("serial")
@WebServlet("/qrcode")
public class QRCodeServlet extends HttpServlet {
	
	@EJB
	private XcpQuerySession session;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			int cacheAge = 60 * 60 * 24;
			long expiry = new Date().getTime() + cacheAge * 1000;

			resp.setDateHeader("Expires", expiry);
			resp.setHeader("Cache-Control", "max-age=" + cacheAge);

			String pars = req.getParameter("pars");
			String fromHex = Converter.fromHex(pars);
			String[] split = fromHex.split("[|]");
			String conteudo = split[0].replaceFirst("conteudo=", "");
			Integer largura = Integer.valueOf(split[1].replaceFirst("largura=", ""));
			Integer altura = Integer.valueOf(split[2].replaceFirst("altura=", ""));
			
			
			// Validações para geração de QRCode
			//-----------------------------------
			
			// Precisa ter uma sessão de usuário (verificar possíveis exceções futuras)
			ServletOutputStream out = resp.getOutputStream();
			HttpSession session = req.getSession(true);
			XcpSessionBacking back = (XcpSessionBacking) session.getAttribute(XcpSessionBacking.ID);
			if (back.getUsuario() == null) {
				out.println("Sem permissao de acesso");
				return;
			}
			
			BufferedImage qrcode = 
				MatrixToImageWriter
				.toBufferedImage(new QRCodeWriter()
					.encode(conteudo, BarcodeFormat.QR_CODE, largura, altura));

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(qrcode, "png", os);                          
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			
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
