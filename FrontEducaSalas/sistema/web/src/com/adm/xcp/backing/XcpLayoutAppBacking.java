package com.adm.xcp.backing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.adm.ejb.entity.Favoritos;
import com.adm.ejb.entity.Menus;
import com.adm.ejb.entity.PortaltranspConsulta;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.entity.pk.FavoritosPK;
import com.adm.ejb.session.XcpIntegracaoSingleton;
import com.adm.ejb.util.SSLUtilities;
import com.adm.ejb.view.AcessofuncView;
import com.adm.ejb.vo.ChaveVO;
import com.adm.ejb.vo.xcape.Behavior;
import com.adm.ejb.vo.xcape.Integracao;
import com.adm.ejb.vo.xcape.Item;
import com.adm.ejb.vo.xcape.Menu;
import com.adm.gui.vo.PlainMenuVO;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.PBE;
import com.adm.util.Util;
import com.adm.util.exceptions.MensagemException;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.servlet.XcpIntegracaoCallBackServlet;
import com.adm.xcp.vos.AvisoDashboardVO;
import com.adm.xcp.vos.MenuAcessoVO;
import com.google.gson.Gson;

@ManagedBean
@RequestScoped
public class XcpLayoutAppBacking extends XcpAbstractBacking {

	XcpAvisosDashboardBacking avisosDashboard;

	private static final String IMAGE_FAV = "/resources/img/favorite.png";
	private static final String IMAGE_NOT_FAV = "/resources/img/add_fav.png";

	private String desImageFavorito;
	private Integer qtdMensagensNaoLidas;
	private Map<Integer, AvisoDashboardVO> listaAvisosDashboard;

	private String desParLnk1;
	private String desParLnk2;

	private Boolean pontoRemotoHabilitado;

	public boolean isRenderedFavorito() {
		return this.getRotina() != null && this.getRotina().getRotina() != null && this.getSession().getUsuario() != null && this.getCodEmpresa() != null;
	}

	public String getDesImageFavActive() {
		return IMAGE_FAV;
	}

	public String getDesImageFavorito() {
		if (this.desImageFavorito == null) {
			this.desImageFavorito = IMAGE_NOT_FAV;

			MenuAcessoVO menuAcessoVO = this.getSession().getMenuAcesso(getPageName(), this.getCodObjeto());
			if (menuAcessoVO != null) {
				FavoritosPK pk = new FavoritosPK();
				pk.setOperador(this.getSession().getUsuario().getOperador());
				pk.setRotina(menuAcessoVO.getRotina());
				Favoritos fav = this.xcpQuerySession.find(this.getEjbVars(), Favoritos.class, pk);
				if (fav != null) {
					this.desImageFavorito = IMAGE_FAV;
				}
			}
		}

		return this.desImageFavorito;
	}

	public void actionCreateFav() {
		MenuAcessoVO menuAcessoVO = this.getSession().getMenuAcesso(getPageName(), this.getCodObjeto());
		if (menuAcessoVO != null) {
			this.xcpSession.createFav(this.getEjbVars(), this.getSession().getUsuario().getOperador(), menuAcessoVO.getRotina());
			//Forca a recarga dos favoritos na proxima solicitacao
			this.getSession().setMenuFav(null);
		}
	}

	public boolean isPopupTemplate() {
		String template = "manut";
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (externalContext.getRequestMap().containsKey(PARAM_XCP_TEMPLATE_NAME_CURRENT)) {
			template = (String) externalContext.getRequestMap().get(PARAM_XCP_TEMPLATE_NAME_CURRENT);
		}
		return Objects.equals(template, "popup");
	}

	private String getImagemPastaSistema(String imagem) throws ParametroNaoEncontradoException {
		XcpParametrosSistema parSitema1 = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), 1, "Sistema", 1);
		if (parSitema1 == null || Util.isEmpty(parSitema1.getDesParametro())) {
			return "/resources/img/" + imagem;
		}
		StringBuilder logo = new StringBuilder();
		logo.append(parSitema1.getDesParametro());
		logo.append(imagem);

		String path = logo.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("/img?pars=");
		sb.append(Converter.toHex(path));
		return sb.toString();
	}

	public String getDesImagem() throws ParametroNaoEncontradoException {
		return this.getImagemPastaSistema("logo_prefeitura.png");
	}

	public String getDesImagemCarteiraFuncional() throws ParametroNaoEncontradoException {
		XcpParametrosSistema parSitema1 = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), 1, "Carteira funcional", 1);
		if (parSitema1 != null && !Util.isEmpty(parSitema1.getDesParametro())) {
			StringBuilder sb = new StringBuilder();
			sb.append("/img?pars=");
			sb.append(Converter.toHex(parSitema1.getDesParametro()));
			return sb.toString();
		}
		return this.getDesImagemPortal();
	}

	public String getDesImagemPortal() throws ParametroNaoEncontradoException {
		XcpParametrosSistema parSitema24 = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), 1, "Portal Servidor", 24);
		if (parSitema24 != null && !Util.isEmpty(parSitema24.getDesParametro())) {
			StringBuilder sb = new StringBuilder();
			sb.append("/img?pars=");
			sb.append(Converter.toHex(parSitema24.getDesParametro()));
			return sb.toString();
		}
		return this.getImagemPastaSistema("logo_portal.png");
	}

	private String getDesParametroSistema(Integer codEmpresa, String codParametro, Integer numParametro) {
		try {
			XcpParametrosSistema parametroSistema = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), codEmpresa, codParametro, numParametro);
			if (parametroSistema == null || Util.isEmpty(parametroSistema.getDesParametro())) {
				return "";
			}
			return parametroSistema.getDesParametro();
		} catch (ParametroNaoEncontradoException e) {
			return "";
		}
	}

	private String getResourcePath(String resource) {
		if (resource.isEmpty()) {
			return "";
		}
		StringBuilder caminho = new StringBuilder();
		caminho.append(resource);
		String path = caminho.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("/resource?pars=");
		sb.append(Converter.toHex(path));
		return sb.toString();
	}

	public String getDesInstituicao() {
		return this.getDesParametroSistema(1, "Portal Servidor", 1);
	}

	public String getCaminhoLogoTelaLogin() {
		return this.getResourcePath(this.getDesParametroSistema(1, "Portal Servidor", 22));
	}

	public String getCaminhoImagemFundoPortal() {
		return this.getResourcePath(this.getDesParametroSistema(1, "Portal Servidor", 2));
	}

	public String getCaminhoCSSCustomizadoPortal() {
		return this.getResourcePath(this.getDesParametroSistema(1, "Portal Servidor", 3));
	}

	public String getCSSCustomizadoPortal() {
		String customCSS = this.getDesParametroSistema(1, "Portal Servidor", 3);
		if (customCSS.isEmpty()) {
			return "";
		}
		return customCSS;
	}

	public int getQtdMensagensNaoLidas() {
		if (this.getSession().getUsuario() != null) {
			if (this.qtdMensagensNaoLidas == null) {
				MontaQuery q = new MontaQuery("SELECT count(e) FROM Mensagens e ");
				q.addWhere("operador", "=", this.getSession().getUsuario().getOperador());
				q.addWhere("e.dtvisualizacao is null");
				this.qtdMensagensNaoLidas = Converter.toInteger(this.xcpQuerySession.executeQuerySingle(this.getEjbVars(), q));
				return this.qtdMensagensNaoLidas;
			} else {
				return this.qtdMensagensNaoLidas;
			}
		}
		return 0;
	}

	public boolean hasAvisoRotina(Integer codigoRotina) throws Exception {
		return this.getListaAvisosDashboard().get(codigoRotina) != null;
	}

	public AvisoDashboardVO getAvisoRotina(Integer codigoRotina) throws Exception {
		return this.getListaAvisosDashboard().get(codigoRotina);
	}

	public Map<Integer, AvisoDashboardVO> getListaAvisosDashboard() throws Exception {
		if (this.listaAvisosDashboard == null) {
			this.avisosDashboard = getBacking(XcpAvisosDashboardBacking.class);
			this.listaAvisosDashboard = this.avisosDashboard.getListaAvisosDashboard();
		}
		return this.listaAvisosDashboard;
	}

	public StreamedContent getDownloadDocumento(Integer help) throws Exception {
		//		Integer help = this.getRotina().getHelp();

		String par = this.getParametroString("Sistema", 3);
		if (par == null || help == null) {
			addFacesMessage(this.getTraducao("msg_help_nao_encontrado"), MSG_INFO);
			return null;
		}

		File file = Paths.get(par).resolve(help + ".pdf").toFile();
		if (!file.exists()) {
			addFacesMessage(this.getTraducao("msg_help_nao_encontrado"), MSG_INFO);
			return null;
		}

		DefaultStreamedContent content = new DefaultStreamedContent(new FileInputStream(file));
		content.setName(help + ".pdf");
		return content;
	}

	public String getVerificaMensagens() throws Exception {
		if (!this.getSession().isExibiuMsg()) {
			int qtd = this.getQtdMensagensNaoLidas();
			if (qtd > 0) {
				XcpMensagensBacking backing = getBacking(XcpMensagensBacking.class);
				backing.actionAbrir();
			}
			this.getSession().setExibiuMsg(true);
		}
		return null;
	}

	public String getDesParLnk1() {
		if (this.desParLnk1 == null) {
			this.desParLnk1 = this.getDesParametroSistema(1, "Portal Servidor", 20);
		}
		return this.desParLnk1;
	}

	public void setDesParLnk1(String desParLnk1) {
		this.desParLnk1 = desParLnk1;
	}

	public String getDesParLnk2() {
		if (this.desParLnk2 == null) {
			this.desParLnk2 = this.getDesParametroSistema(1, "Portal Servidor", 21);
		}
		return this.desParLnk2;
	}

	public void setDesParLnk2(String desParLnk2) {
		this.desParLnk2 = desParLnk2;
	}

	public Boolean getPontoRemotoHabilitado() {
		if (this.pontoRemotoHabilitado == null) {
			try {
				this.pontoRemotoHabilitado = false;
				XcpParametrosSistema codigoParametro = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), 1, "Portal Servidor", 27);
				if (codigoParametro != null && codigoParametro.getVlrParametro() != null) {
					this.pontoRemotoHabilitado = true;
				}
			} catch (ParametroNaoEncontradoException e) {
				e.printStackTrace();
			}
		}
		return this.pontoRemotoHabilitado;
	}

	@EJB
	private XcpIntegracaoSingleton xcpIntegracaoSingleton;

	public void actionFlowfast(Integer codPaginaXcp) throws Exception {
		String desUrlBaseCallback = null;
		String desUrlParCallback = null;
		if (Menus.PERMISSAO_1_SISTEMA.equals(this.getTipoAcesso())) {
			desUrlParCallback = this.getParametroGlobalString("Flowfast", 4);
		} else {
			desUrlParCallback = this.getParametroGlobalString("Flowfast", 7);
		}

		if (!Util.isEmpty(desUrlParCallback)) {
			desUrlBaseCallback = desUrlParCallback;
		} else {
			String desRoot = XcpViewUtilBacking.getInstance().getRootPath();
			if (!desRoot.endsWith("/")) {
				desRoot += "/";
			}
			desRoot += this.getContexto();
			desUrlBaseCallback = desRoot;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(desUrlBaseCallback);
		sb.append(XcpIntegracaoCallBackServlet.URL);

		Behavior b = new Behavior();
		b.setCallbackurl(sb.toString());
		b.setIdHome("952");
		b.setIdLogout(XcpIntegracaoCallBackServlet.LOGOUT);
		//b.setCtx(ctx);

		Menu m = new Menu();
		List<PlainMenuVO> listplain = this.getSession().getListplain();
		for (PlainMenuVO e : listplain) {
			Item item = new Item();
			item.setDesc(e.getDescricao());

			List<AcessofuncView> listRotinas = e.getListRotinas();
			for (AcessofuncView r : listRotinas) {
				Item folha = new Item();
				folha.setDesc(r.getDescricao());
				folha.setId(Converter.toString(r.getRotina()));
				item.getItens().add(folha);
			}
			m.getItens().add(item);
		}
		b.setMenu(m);

		ChaveVO chave = this.getSession().getChaveADM();
		b.setKey(chave.getDesChave());

		String desUrl = this.getParametroGlobalString("Flowfast", 1);
		if (Util.isEmpty(desUrl)) {
			throw new MensagemException("msg_parametro_flowfast", 1);
		}

		if (!desUrl.endsWith("/")) {
			desUrl += "/";
		}

		String desChave = this.getParametroGlobalString("Flowfast", 3);

		Integracao integ = new Integracao();
		integ.setCodPagina(codPaginaXcp);
		integ.setBehavior(b);
		integ.setDesChave(desChave);
		integ.setDesUsuario(this.getSession().getUsuario().getNome());

		String tipoAcesso = System.getProperty("com.xcape.TIPO_ACESSO");
		if ("2".equals(tipoAcesso)) {
			integ.setCodTipoUsuario(this.getParametroGlobalLong("Flowfast", 6));
			integ.setDesUsuario(Converter.toString(this.getSession().getUsuario().getMatricula()));
		} else {
			integ.setCodTipoUsuario(this.getParametroGlobalLong("Flowfast", 5));
		}

		Gson gson = new Gson();
		String json = gson.toJson(integ);

		sb = new StringBuilder();
		sb.append(desUrl);
		sb.append("xcp/integracao");

		try {
			String desUrlChamar = this.post(sb.toString(), json);
			getRequestContext().execute(String.format("javascript:window.open('%s','_blank')", desUrlChamar));
		} catch (Exception e) {
			this.addFacesMessage(e);
		}

	}

	public void actionFlowfastDemandas(Integer codPaginaXcp) throws Exception {
		try {

			String xml = "<exec>{body}<hash>{hash}</hash></exec>";
			String body = "<e><page>{page}</page><user>{user}</user><token>{token}</token><pars></pars></e>";

			body = body.replace("{page}", Converter.toString(codPaginaXcp));
			body = body.replace("{user}", Converter.toString(this.getSession().getUsuario().getSeqUsuXcpDemandas()));

			body = body.replace("{token}", this.getParametroGlobalString("Flowfast", 9));

			String hash = Util.getMD5Correto(body);
			xml = xml.replace("{body}", body);
			xml = xml.replace("{hash}", hash);

			URL urlServ = new URL(this.getParametroGlobalString("Flowfast", 8) + "/exec-token");
			HttpURLConnection con = (HttpURLConnection) urlServ.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			try (OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8")) {
				wr.write(xml);
				wr.flush();
			}

			int responseCode = con.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			String desResposta = null;
			if (con.getErrorStream() != null) {
				desResposta = this.makeRead(con.getErrorStream());
				addFacesMessage(desResposta);
			} else {
				desResposta = this.makeRead(con.getInputStream());
				System.out.println("URL demandas: " + desResposta);
				getRequestContext().execute(String.format("javascript:window.open('%s','_blank')", desResposta));
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.addFacesMessage(e);
		}
	}

	private String post(String url, String content) throws Exception {
		HttpURLConnection conn = null;
		try {
			URL urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("POST");
			if (conn instanceof HttpsURLConnection) {
				SSLContext sslCtx = SSLContext.getInstance("SSLv3");
				sslCtx.init(null, SSLUtilities.getBypassedTrustManagers(), new java.security.SecureRandom());
				((HttpsURLConnection) conn).setSSLSocketFactory(sslCtx.getSocketFactory());
				((HttpsURLConnection) conn).setHostnameVerifier(SSLUtilities.getBypassedHostnameVerifier());
			}
			conn.setReadTimeout(90000);
			conn.setConnectTimeout(90000);
			conn.setRequestProperty("Content-Length", String.valueOf(content.getBytes().length));
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8;");

			try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
				wr.write(content);
				wr.flush();
			}

			return this.makeRead(conn.getInputStream());
		} catch (IOException e) {
			if (conn != null) {

				throw new IOException(this.makeRead(conn.getErrorStream()), e);
			}

			throw e;
		}
	}

	private String makeRead(InputStream is) throws Exception {
		if (is == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (InputStreamReader ir = new InputStreamReader(is, Charset.forName("UTF-8")); BufferedReader rd = new BufferedReader(ir)) {
			String line = null;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
		}

		return sb.toString();
	}

	public void actionOpenDrive() {
		getRequestContext().execute(String.format("window.open('%s');", "https://drive.google.com/file/d/14KbqcFbntfsERZ861UvxM6RDe-5SdhGQ/view"));
	}

	public void actionLinkTransparencia(Integer idConsulta) throws Exception {

		PortaltranspConsulta e = this.xcpQuerySession.find(this.getEjbVars(), PortaltranspConsulta.class, idConsulta);

		String desUrl = this.getParametroString("Transparencia", 10);

		if (!desUrl.endsWith("/")) {
			desUrl += "/";
		}

		String oper = PBE.encPBE(Converter.toString(this.getSession().getUsuario().getOperador()));
		oper = URLEncoder.encode(oper, "UTF-8");

		String emp = PBE.encPBE(Converter.toString(this.getCodEmpresa()));
		emp = URLEncoder.encode(emp, "UTF-8");

		StringBuilder sb = new StringBuilder();
		sb.append(desUrl);
		sb.append(e.getIdentificador());
		sb.append("?o=");
		sb.append(oper);
		sb.append("&e=");
		sb.append(emp);

		getRequestContext().execute(String.format("javascript:window.open('%s','_blank')", sb.toString()));

	}
}
