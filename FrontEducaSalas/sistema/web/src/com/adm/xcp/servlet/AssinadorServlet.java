package com.adm.xcp.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.adm.ejb.entity.Assinproc;
import com.adm.ejb.entity.extend.AssinetapaManut;
import com.adm.ejb.session.XcpAssinaturaDigitalSessionBean;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.xcp.backing.XcpFuncionarioBacking;

@WebServlet(AssinadorServlet.CONTEXT)
public class AssinadorServlet extends HttpServlet {

	public static final String CONTEXT = "/assinar";

	private static final Logger logger = Logger.getLogger(XcpFuncionarioBacking.class.getName());

	@EJB
	private XcpQuerySession session;

	@EJB
	private XcpAssinaturaDigitalSessionBean assinaturaSession;

	@EJB
	private XcpPersistSession persistSession;

	private static final String ACTION_1_REQ_FILE = "action_1_request";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			logger.info("Recebido requisicao de assinatura... ");

			String body = this.getBody(req);

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(body));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inStream);

			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression exp = xpath.compile("//*[local-name()='etapa']/text()");
			String etapa = (String) exp.evaluate(doc, XPathConstants.STRING);

			exp = xpath.compile("//*[local-name()='check']/text()");
			String check = (String) exp.evaluate(doc, XPathConstants.STRING);

			Long seqEtapa = Converter.toLong(etapa);

			//Verifica o hash da solicitacao para evitar tentativas sequenciais de download
			AssinetapaManut entity = this.session.find(null, AssinetapaManut.class, seqEtapa);
			if (entity == null || !entity.getUuid().equals(check)) {
				logger.info("Hash do arquivo recebido nao corresponde ao hash da base. Invalidando requisicao! ");
				ServletOutputStream out = resp.getOutputStream();
				out.print(this.createXmlErro("Documento nao existente"));
				return;
			}

			//Se ja foi assinado, nao pode reassinar. alem de ser mais um mecanismo de seguranca
			if (entity.getDataassin() != null) {
				logger.info("Documento ja assinado. Invalidando requisicao!");
				ServletOutputStream out = resp.getOutputStream();
				out.print(this.createXmlErro("Documento ja assinado"));
				return;
			}

			Assinproc proc = this.session.find(null, Assinproc.class, entity.getSeqproc());
			if (!Objects.equals(proc.getSeqetapa(), entity.getSeqetapa())) {
				logger.info("Ordem de assinatura invalida! Invalidando requisicao!");
				ServletOutputStream out = resp.getOutputStream();
				out.print(this.createXmlErro("Ordem de assinatura invalida!"));
				return;
			}

			if (proc.getDtcancelamento() != null) {
				logger.info("Processo de assinatura cancelado. Assinatura negada!");
				ServletOutputStream out = resp.getOutputStream();
				out.print(this.createXmlErro("Processo de assinatura cancelado. Assinatura negada!"));
				return;
			}

			//			TODO: HABILITAR ALGUMA SEGURANCA. POR ENQUANTO DESLIGADO O DE CPF PARA AGILIZAR A ENTREGA
			//			exp = xpath.compile("//*[local-name()='cpf']");
			//			NodeList nodos = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
			//			/*
			//			 * Testa se algum dos CPF que o assinador tem, fecha com o cpf do
			//			 * funcionario que deveria assinar
			//			 */
			//			boolean valido = false;
			//			for (int i = 0; i < nodos.getLength(); i++) {
			//				Node ele = nodos.item(i);
			//				String cpf = ele.getTextContent();
			//
			//				if (Objects.equals(Converter.toLong(cpf), entity.getMatriculaFk().getCpf())) {
			//					valido = true;
			//				}
			//			}
			//
			//			if (!valido) {
			//				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			//				return;
			//			}

			String xaction = req.getHeader("xaction");
			if (ACTION_1_REQ_FILE.equals(xaction)) {
				logger.info("Realizando o download para assinatura... ");
				/*
				 * Faz o download
				 */
				String xml = this.assinaturaSession.montaXmlDownload(null, entity);
				ServletOutputStream out = resp.getOutputStream();
				out.print(xml);
			} else {
				logger.info("Realizando o upload do documento assinado... ");
				/*
				 * Atualiza o pdf assinado
				 */
				exp = xpath.compile("//*[local-name()='file']/text()");
				String hex = (String) exp.evaluate(doc, XPathConstants.STRING);
				byte[] bytesass = DatatypeConverter.parseBase64Binary(hex);

				//				AssinetapaBlob blob = this.session.find(null, AssinetapaBlob.class, seqEtapa);
				//				blob.setDocAssinado(bytesass);
				//				blob.setDataassin(new Date());
				//				blob = this.persistSession.update(null, blob);
				//
				//				proc.setSeqetapa(seqEtapa);
				//				this.persistSession.update(null, proc);

				this.assinaturaSession.atualizaDocAssinado(null, bytesass, seqEtapa);
			}

			logger.info("Concluindo.");

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	private String getBody(HttpServletRequest req) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = req.getReader()) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private String createXmlErro(String mensagem) {
		StringBuilder sb = new StringBuilder();
		sb.append("<mensagem>");
		sb.append("<texto>");
		sb.append(mensagem);
		sb.append("</texto>");
		sb.append("</mensagem>");
		return sb.toString();
	}
}
