package com.adm.xcp.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.adm.ejb.entity.NoticiasPortal;
import com.adm.ejb.entity.TipoDocumentoPortal;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.entity.extend.DocumentosPortalManut;
import com.adm.ejb.entity.extend.PerguntasFrequentesManut;
import com.adm.ejb.entity.extend.PerguntasTopicosManut;
import com.adm.ejb.session.local.XcpSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.EJBLookup;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.vo.XcpEjbVars;

public class XcpPortalCache {
	private static XcpPortalCache instance;

	@EJB
	protected XcpSession xcpSession;

	@EJB
	protected XcpQuerySession xcpQuerySession;

	private List<NoticiasPortal> listaNoticias;
	private List<DocumentosPortalManut> listaDocumentos;
	private List<TipoDocumentoPortal> listaTipoDocumento;
	private List<PerguntasTopicosManut> listaPerguntasTopicos;
	private List<PerguntasFrequentesManut> listaPerguntasFrequentes;
	private boolean carregado = false;

	private XcpPortalCache() throws Exception {
		this.xcpQuerySession = (XcpQuerySession) EJBLookup.getInstance().get("XcpQuerySessionBean");
		this.xcpSession = (XcpSession) EJBLookup.getInstance().get("XcpSessionBean");
	}

	public static XcpPortalCache getInstance() {
		if (instance == null) {
			try {
				instance = new XcpPortalCache();
				instance.carregar();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public void carregar() {
		if (!this.isCarregado()) {

			XcpEjbVars ejbVars = null;

			{
				MontaQuery qt = new MontaQuery(PerguntasTopicosManut.class, "e.topico");
				this.setListaPerguntasTopicos(this.xcpQuerySession.executeQueryList(ejbVars, qt));
			}
			{
				MontaQuery qp = new MontaQuery(PerguntasFrequentesManut.class, "e.topico, e.pergunta");
				qp.addWhere("(e.validade is null or e.validade >= :dta)");
				qp.putParameter("dta", new Date());
				this.setListaPerguntasFrequentes(this.xcpQuerySession.executeQueryList(ejbVars, qp));
			}
			{
				MontaQuery qn = new MontaQuery(NoticiasPortal.class, "e.publicacao desc");
				qn.addWhere("(e.validade is null or e.validade >= :dta)");
				qn.putParameter("dta", new Date());
				this.setListaNoticias(this.xcpQuerySession.executeQueryList(ejbVars, qn));
				
				this.compactarImagensNoticias();
			}
			{
				MontaQuery qtd = new MontaQuery(TipoDocumentoPortal.class, "e.descricao desc");
				this.setListaTipoDocumento(this.xcpQuerySession.executeQueryList(ejbVars, qtd));
				
			}
			{
				Map<Integer,String> tipoDocMap = new HashMap<Integer,String>();
				List<TipoDocumentoPortal> tipoDocList = new ArrayList<TipoDocumentoPortal>();
				MontaQuery qd = new MontaQuery(DocumentosPortalManut.class, "e.tipoDocumentoPortal,e.ordem");
				qd.addWhere("(e.validade is null or e.validade >= :dta)");
				qd.putParameter("dta", new Date());
				List<DocumentosPortalManut> docList = this.xcpQuerySession.executeQueryList(ejbVars, qd);
				for(DocumentosPortalManut doc:docList) {
					TipoDocumentoPortal tipoDoc = doc.getTipoDocumentoPortalFk();
					if(tipoDoc == null) {
						tipoDoc = new TipoDocumentoPortal();
						tipoDoc.setCodigo(0);
						tipoDoc.setDescricao("Tipo não informado");
					}
					if(!tipoDocMap.containsKey(tipoDoc.getCodigo())){						
						if(tipoDoc.getCodigo() == 0) {
							tipoDocList.add(0,tipoDoc);
						} else {
							tipoDocList.add(tipoDoc);
						}	
						tipoDocMap.put(tipoDoc.getCodigo(),tipoDoc.getDescricao());
					}
				}	
				this.setListaTipoDocumento(tipoDocList);
				this.setListaDocumentos(docList);								
			}
			this.setCarregado(true);
			this.otimizarImagemFundo();
		}
	}

	private void otimizarImagemFundo() {
		String caminhoImagemFundo = "";
		try {
			XcpParametrosSistema parametro = this.xcpSession.findXcpParametrosSistema(null, 1, "Portal Servidor", 2);
			caminhoImagemFundo = parametro.getDesParametro();

			this.compactarImagem(caminhoImagemFundo, 1024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void compactarImagem(String caminhoImagemOrigem, Integer largura) {
		if (Util.isEmpty(caminhoImagemOrigem)) {
			return;
		}
		try {
			String caminhoImagemFundo = FacesContext.getCurrentInstance().getExternalContext().getResource("/resources/img/bg_customizado.jpg").getPath();
			File input = new File(caminhoImagemOrigem);
			BufferedImage image = ImageIO.read(input);

			BufferedImage scaledBufferedImage = this.prepararImagemReduzida(image, largura);

			File compressedImageFile = new File(caminhoImagemFundo);
			OutputStream os = new FileOutputStream(compressedImageFile);
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);

			ImageWriter writer = this.getImageWriterByFormatName("jpg");
			writer.setOutput(ios);
			writer.write(null, new IIOImage(scaledBufferedImage, null, null), this.getWriteParam(writer));

			os.close();
			ios.close();
			writer.dispose();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void compactarImagensNoticias() {
		int largura = 640;
		for (NoticiasPortal noticia : this.listaNoticias) {
			try {
				if (noticia.getImagem() == null) {
					continue;
				}

				BufferedImage image = ImageIO.read(new ByteArrayInputStream(noticia.getImagem()));

				BufferedImage scaledBufferedImage = this.prepararImagemReduzida(image, largura);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

				ImageWriter writer = this.getImageWriterByFormatName("jpg");
				writer.setOutput(ios);
				writer.write(null, new IIOImage(scaledBufferedImage, null, null), this.getWriteParam(writer));

				baos.flush();
				noticia.setImagem(baos.toByteArray());

				ios.close();
				baos.close();
				writer.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ImageWriter getImageWriterByFormatName(String formatName) {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
		return writers.next();
	}

	private ImageWriteParam getWriteParam(ImageWriter writer) {
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(0.85f);
		return param;
	}

	private BufferedImage prepararImagemReduzida(BufferedImage imagem, int largura) {
		Image scaledImage = imagem.getScaledInstance(largura, -1, Image.SCALE_SMOOTH);
		BufferedImage scaledBufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D bGr = scaledBufferedImage.createGraphics();
		bGr.drawImage(scaledImage, 0, 0, null);
		bGr.dispose();
		return scaledBufferedImage;
	}

	public boolean isCarregado() {
		return this.carregado;
	}

	public void setCarregado(boolean carregado) {
		this.carregado = carregado;
	}

	public static void limpar() {
		instance = null;
	}

	public List<NoticiasPortal> getListaNoticias() {
		return this.listaNoticias;
	}

	public void setListaNoticias(List<NoticiasPortal> listaNoticias) {
		this.listaNoticias = listaNoticias;
	}

	public List<PerguntasTopicosManut> getListaPerguntasTopicos() {
		return this.listaPerguntasTopicos;
	}

	public void setListaPerguntasTopicos(List<PerguntasTopicosManut> listaPerguntasTopicos) {
		this.listaPerguntasTopicos = listaPerguntasTopicos;
	}

	public List<PerguntasFrequentesManut> getListaPerguntasFrequentes() {
		return this.listaPerguntasFrequentes;
	}

	public void setListaPerguntasFrequentes(List<PerguntasFrequentesManut> listaPerguntasFrequentes) {
		this.listaPerguntasFrequentes = listaPerguntasFrequentes;
	}

	public List<DocumentosPortalManut> getListaDocumentos() {
		return this.listaDocumentos;
	}

	public void setListaDocumentos(List<DocumentosPortalManut> listaDocumentos) {
		this.listaDocumentos = listaDocumentos;
	}

	public List<TipoDocumentoPortal> getListaTipoDocumento() {
		return listaTipoDocumento;
	}

	public void setListaTipoDocumento(List<TipoDocumentoPortal> listaTipoDocumento) {
		this.listaTipoDocumento = listaTipoDocumento;
	}
	
	
}
