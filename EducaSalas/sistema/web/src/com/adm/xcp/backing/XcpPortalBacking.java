package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlInputText;

import com.adm.ejb.entity.Menus;
import com.adm.ejb.entity.NoticiasPortal;
import com.adm.ejb.entity.TipoDocumentoPortal;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.entity.extend.DocumentosPortalManut;
import com.adm.ejb.session.XcpServerCacheSessionBean;
import com.adm.util.Converter;
import com.adm.util.Util;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.servlet.DocPortalServlet;
import com.adm.xcp.util.XcpPortalCache;
import com.adm.xcp.util.XcpViewConstants;

@ManagedBean
@ViewScoped
public class XcpPortalBacking extends XcpAbstractBacking implements Serializable {

	private DocumentosPortalManut docDownload;

	private XcpPortalCache cache;

	@EJB
	private XcpServerCacheSessionBean xcpServSession;

	public XcpPortalBacking() {
		this.cache = XcpPortalCache.getInstance();
	}

	private XcpPortalCache getCache() {
		return this.cache;
	}

	public void preRenderView() {

		if (this.isPaginaInicialBloqueada() && !this.getSession().isLogged()) {
			this.navigateTo(XcpViewConstants.PG_HOME_REDIRECT);
		}

		if (Menus.PERMISSAO_1_SISTEMA.equals(this.getTipoAcesso())) {
			this.navigateTo(XcpViewConstants.PG_HOME_REDIRECT);
		} else {
			if (this.xcpServSession.hasToClear(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_DOCUMENTOS_PORTAL)) {
				XcpPortalCache.limpar();
				this.cache = XcpPortalCache.getInstance();
				this.xcpServSession.markCacheOk(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_DOCUMENTOS_PORTAL);
			}
		}
	}

	private Boolean isPaginaInicialBloqueada() {
		XcpParametrosSistema paramSistema;
		try {
			paramSistema = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), 1, "Portal Servidor", 15);
		} catch (ParametroNaoEncontradoException e) {
			return false;
		}
		if (paramSistema != null && paramSistema.getVlrParametro() != null && paramSistema.getVlrParametro().equals(BIG_1)) {
			return true;
		}
		return false;
	}

	public List<NoticiasPortal> getListaNoticias() {
		return this.getCache().getListaNoticias();
	}

	public List<DocumentosPortalManut> getListaDocumentos() {
		return this.getCache().getListaDocumentos();
	}

	public List<TipoDocumentoPortal> getListaTipoDocumento() {
		return this.getCache().getListaTipoDocumento();
	}

	public String getDesPathDocumento(DocumentosPortalManut doc, Integer tipo) {
		return String.format("/docportal?pars=%s", Converter.toHex(String.format("%s|%s", Converter.toStringDateTime(doc.getPublicacao(), true), tipo)));
	}

	public String getDesPathNoticia(NoticiasPortal noticia) {
		return String.format("/noticiaportal?pars=%s", Converter.toHex(String.format("%s", Converter.toStringDateTime(noticia.getPublicacao(), true))));
	}

	public void actionIniciaDownload(DocumentosPortalManut doc) {
		this.docDownload = doc;
		getBacking(XcpCaptchaBacking.class).actionTrocarCaptcha("download_portal");

		((HtmlInputText) this.findComponent("txtCaptcha")).setSubmittedValue("");
		((HtmlInputText) this.findComponent("txtCaptcha")).setValue("");

		getRequestContext().execute("PF('dlgCaptchaDownPortal_w').show();");
	}

	public void actionDownload() {
		if (this.docDownload != null) {
			String arg = String.format(
					"PF('dlgCaptchaDownPortal_w').hide();window.open('%s%s','_self');",
					this.getContexto(),
					this.getDesPathDocumento(this.docDownload, DocPortalServlet.PARAM_TIPO_ARQUIVO));
			getRequestContext().execute(arg);
		}
	}

	public boolean rendererDoc(DocumentosPortalManut documento, TipoDocumentoPortal tipoDoc) {
		if (documento.getTipoDocumentoPortalFk() == null) {
			return (tipoDoc.getCodigo() == 0); //Tipo não informado
		} else {
			return (documento.getTipoDocumentoPortalFk().getCodigo() == tipoDoc.getCodigo());
		}
	}

	public List<List<DocumentosPortalManut>> getRowsDoc(TipoDocumentoPortal tipoDoc) {
		List<List<DocumentosPortalManut>> rowsDoc = new ArrayList<List<DocumentosPortalManut>>();
		List<DocumentosPortalManut> listaDocumentos = this.getCache().getListaDocumentos();
		List<DocumentosPortalManut> rowDoc = new ArrayList<DocumentosPortalManut>();
		if (!Util.isEmpty(listaDocumentos)) {
			for (DocumentosPortalManut doc : listaDocumentos) {
				if (this.rendererDoc(doc, tipoDoc)) {
					rowDoc.add(doc);
					if (rowDoc.size() == 3) {
						rowsDoc.add(rowDoc);
						rowDoc = new ArrayList<DocumentosPortalManut>();
					}
				}
			}
			if (rowDoc.size() > 0) {
				rowsDoc.add(rowDoc);
			}
		}
		return rowsDoc;
	}

}
