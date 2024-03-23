package com.adm.xcp.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.XcpAzureReq;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.session.local.XcpSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.Formatter;
import com.adm.util.Util;
import com.adm.xcp.vos.AzureVO;

@WebServlet("/azure")
public class AzureServlet extends HttpServlet {

	public static final String AZURE_VO = "azure.vo";

	@EJB
	private XcpSession session;

	@EJB
	private XcpQuerySession xcpQuerySession;

	@EJB
	private XcpPersistSession xcpPersistSession;

	private static Logger logger = Logger.getLogger(AzureServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("doGet()");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		XcpAzureReq xcpreq = null;
		try {
			//Pega a resposta no atributo
			String body = req.getParameter("SAMLResponse");
			if (Util.isEmpty(body)) {
				logger.error("msg_azure_response");
				this.returnFailure(req, "msg_azure_response", null);
				return;
			}

			//Transforma de base64 para string plana
			Document doc = null;
			String xml = null;
			try {
				xml = new String(DatatypeConverter.parseBase64Binary(body));
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(new InputSource(new StringReader(xml)));
			} catch (Exception e) {
				logger.error("msg_azure_xml " + xml);
				this.returnFailure(req, "msg_azure_xml", xml);
				return;
			}

			//Grava a requisicao no banco
			xcpreq = new XcpAzureReq();
			xcpreq.setIndErro(0);
			xcpreq.setDesMsg(xml);
			xcpreq.setDthMsg(new Date());
			xcpreq.setTipSentido(XcpAzureReq.TIP_SENTIDO_2_AZURE_SIST);
			xcpreq = this.xcpPersistSession.update(null, xcpreq);

			//Usa a biblioteca opensaml para parsear a resposta. 
			//Nao precisaria, mas usa-se a mesma para validar a assinatura digital logo a frente
			Element element = doc.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			Response samlresp = (Response) unmarshaller.unmarshall(element);

			/*
			 * VER OS TIPOS DE REQUEST QUE PODEM CHEGAR. IGNORAR OS FAILURE E
			 * SOMENTE PERMITIR OS SUCCESS??
			 */

			boolean updatedb = false;
			if (!Util.isEmpty(samlresp.getID())) {
				xcpreq.setDesId(samlresp.getID());
				updatedb = true;
			}

			if (!Util.isEmpty(samlresp.getInResponseTo())) {
				StringBuilder sb = new StringBuilder();
				sb.append("select seq from xcp_azure_req ");
				sb.append(" where des_id = :desId ");

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("desId", samlresp.getInResponseTo());
				List l = this.xcpQuerySession.executeNativeQueryList(null, sb.toString(), map);
				if (!Util.isEmpty(l)) {
					xcpreq.setSeqResp(Converter.toLong(l.get(0)));
					updatedb = true;
				}
			}

			//Se tem os campos acima no xml (deveria ter sempre) atualiza no banco 
			if (updatedb) {
				xcpreq = this.xcpPersistSession.update(null, xcpreq);
			}

			//Verifica se tem que validar a assinatura digital
			XcpParametrosSistema xcpAssinatura = this.session.findXcpParametrosSistema(null, null, "Azure", 8);
			boolean assinatura = false;
			if (xcpAssinatura != null) {
				assinatura = Objects.equals(xcpAssinatura.getVlrParametro(), BigDecimal.ONE);
			}

			if (assinatura) {
				XcpParametrosSistema xcpPathCert = this.session.findXcpParametrosSistema(null, null, "Azure", 9);
				String caminho = null;
				if (xcpPathCert != null) {
					caminho = xcpPathCert.getDesParametro();
				}

				//Se nao tem nada no parametro dispara erro
				if (Util.isEmpty(caminho)) {
					logger.error("msg_azure_certpath");
					this.returnFailure(req, "msg_azure_certpath", null);
					return;
				}

				//Se nao existe o certificado de validacao, dispara erro
				File f = new File(caminho);
				if (!f.exists() || !f.canRead()) {
					logger.error("msg_azure_certpath");
					this.returnFailure(req, "msg_azure_certpath", null);
					return;
				}

				//Procura a assinatura dentro da primeira assertion que tiver. O Azure somente assina 
				//essa parte do xml (para outros provedores,necessario analizar o xml e a especificacao)
				for (Assertion a : samlresp.getAssertions()) {
					if (a.getSignature() != null) {
						CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
						InputStream inputStream = new FileInputStream(f);
						X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(inputStream);
						// Crie a credencial com o certificado
						BasicX509Credential credential = new BasicX509Credential();
						credential.setEntityCertificate(caCert);

						try {
							new SignatureValidator(credential).validate(a.getSignature());
						} catch (ValidationException e) {
							logger.error("msg_invalid_cert");
							this.returnFailure(req, "msg_invalid_cert", xml);
							return;
						}

						break;
					}
				}
			}

			//Pega a url de destino que o azure tinha
			String desUrl = samlresp.getDestination();
			if (Util.isEmpty(desUrl)) {
				logger.error("msg_azure_url_orig " + xml);
				this.returnFailure(req, "msg_azure_url_orig", xml);
				return;
			}

			//Pega os parametros que indicam qual url pertence a qual dominio
			String desUrlSist = null;
			String desUrlPortal = null;
			try {
				XcpParametrosSistema xcpDesUrlSist = this.session.findXcpParametrosSistema(null, null, "Azure", 6);
				XcpParametrosSistema xcpDesUrlPortal = this.session.findXcpParametrosSistema(null, null, "Azure", 7);

				desUrlSist = xcpDesUrlSist.getDesParametro();
				desUrlPortal = xcpDesUrlPortal.getDesParametro();

			} catch (Exception e) {
				logger.error("msg_azure_pars67");
				this.returnFailure(req, "msg_azure_pars67", null);
				return;
			}

			//Descobre se eh portal ou sistema
			Integer tipPermissao = null;
			if (desUrl.contains(desUrlSist)) {
				tipPermissao = Operadores.PERMISSAO_1_SISTEMA;
			} else if (desUrl.contains(desUrlPortal)) {
				tipPermissao = Operadores.PERMISSAO_2_PORTAL;
			}

			if (tipPermissao == null) {
				logger.error("msg_azure_orig " + xml);
				this.returnFailure(req, "msg_azure_orig", xml);
				return;
			}

			//Procura o email
			String desEmail = null;
			if (!Util.isEmpty(samlresp.getAssertions())) {
				for (Assertion assertion : samlresp.getAssertions()) {
					if (!Util.isEmpty(assertion.getAttributeStatements())) {
						for (AttributeStatement attstat : assertion.getAttributeStatements()) {
							if (!Util.isEmpty(attstat.getAttributes())) {
								for (Attribute att : attstat.getAttributes()) {
									if (att.getName().toLowerCase().contains("emailaddress")) {
										desEmail = att.getAttributeValues().get(0).getDOM().getTextContent();
										break;
									}
								}
							}
						}
					}
				}
			}

			if (Util.isEmpty(desEmail)) {
				logger.error("msg_azure_email " + xml);
				this.returnFailure(req, "msg_azure_email", xml);
				return;
			}

			desEmail = desEmail.toLowerCase();

			//Com o email e o tipo de sistema descobre o operador
			StringBuilder sb = new StringBuilder();
			sb.append(" Select e from Operadores e, Funcionarios f  ");
			sb.append("  where e.empresa = f.empresa                ");
			sb.append("   and e.matricula = f.matricula             ");
			sb.append("   and lower(f.email) = :desEmail                   ");
			sb.append("   and e.permissao = :tipPermissao			  ");
			sb.append("   and e.situacao = 1								  ");
			sb.append("   order by f.admissao 							  ");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("desEmail", desEmail);
			map.put("tipPermissao", tipPermissao);

			List<Operadores> listOper = this.xcpQuerySession.executeQueryList(null, sb.toString(), map);

			if (Util.isEmpty(listOper)) {
				logger.error("msg_azure_oper_n_encontrado");
				this.returnFailure(req, "msg_azure_oper_n_encontrado", xml);
				return;
			}

			Operadores oper = listOper.get(0);

			HttpSession session = req.getSession(true);
			session.setAttribute(AZURE_VO, new AzureVO(oper));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (xcpreq != null) {
				xcpreq.setIndErro(1);
				xcpreq.setDesErro(Formatter.getStackTrace(e));
				xcpreq = this.xcpPersistSession.update(null, xcpreq);
			}

		} finally {
			resp.sendRedirect(req.getContextPath() + "/public/xcp/XcpLoginAzure.xhtml");
		}
	}

	private void returnFailure(HttpServletRequest req, String message, String xml) {
		HttpSession session = req.getSession(true);
		session.setAttribute(AZURE_VO, new AzureVO(message, xml));
	}

}
