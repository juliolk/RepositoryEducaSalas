package com.adm.xcp.backing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.XcpObjetoModelo;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.util.Converter;
import com.adm.util.DBConnect;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.vos.XcpImportarRelatorioVO;

@ManagedBean
@ViewScoped
public class XcpImportarRelatorioBacking extends XcpAbstractBacking implements XcpEjbConstants {

	private boolean semModelo = true;
	private boolean semFonte = true;
	private List<XcpImportarRelatorioVO> lista;
	private List<XcpImportarRelatorioVO> listaFiltrados;
	private List<XcpImportarRelatorioVO> listaSel;
	private List<String> listerr;
	private String desOrigem;
	private String desDestino;

	@EJB
	private XcpPersistSession persist;

	public List<XcpImportarRelatorioVO> getLista() {
		if (this.lista == null) {
			this.lista = new ArrayList<XcpImportarRelatorioVO>();
		}
		return this.lista;
	}

	public void actionPesquisar() {
		this.setLista(null);
		this.setListaFiltrados(null);
		this.setListaSel(null);

		StringBuilder sb = new StringBuilder();
		sb.append("  SELECT o.cod_objeto, m.des_nome, m.seq_modelo                  ");
		sb.append("    FROM xcp_objeto o                                            ");
		sb.append("    LEFT JOIN xcp_objeto_modelo m ON o.cod_objeto = m.cod_objeto ");
		sb.append("    and m.tipo = 1																 ");
		sb.append("   WHERE o.tip_objeto = 1                                        ");
		if (this.isSemFonte()) {
			sb.append("    AND m.obj_fonte is null                                   ");
		}

		if (this.isSemModelo()) {
			sb.append("    AND m.des_modelo is null                                  ");
		}

		List<XcpImportarRelatorioVO> newlist = new ArrayList<XcpImportarRelatorioVO>();
		List<Object[]> list = this.xcpQuerySession.executeNativeQueryList(this.getEjbVars(), sb.toString(), new HashMap<String, Object>());
		for (Object[] arr : list) {
			XcpImportarRelatorioVO vo = new XcpImportarRelatorioVO();
			vo.setCodObjeto(Converter.toString(arr[0]));
			vo.setCodModelo(Converter.toString(arr[1]));
			vo.setSeqModelo(Converter.toLong(arr[2]));
			newlist.add(vo);
		}

		this.setLista(newlist);
	}

	public String getDesDestino() {
		if (this.desDestino == null) {
			try {
				this.desDestino = this.getParametroString("Sistema", 4);
			} catch (ParametroNaoEncontradoException e) {
				this.desDestino = "";
			}
		}
		return this.desDestino;
	}

	public void actionImportar() throws Exception {

		this.listerr = new ArrayList<String>();

		File fori = new File(this.getDesOrigem());
		if (!fori.exists()) {
			//TODO: Caminho de origem nao existe no servidor
			addFacesMessage(this.getTraducao("msg_caminho_origem"));
			return;
		}

		String dirRel = this.getDesDestino();
		if (Util.isEmpty(dirRel)) {
			addFacesMessage(this.getTraducao("msg_parametro_relatorio_invalido"));
			return;
		}

		if (!new File(dirRel).canWrite()) {
			addFacesMessage(this.getTraducao("msg_nao_pode_gravar_destino"));
			return;
		}

		List<XcpImportarRelatorioVO> listaSel = this.getListaSel();
		if (Util.isEmpty(listaSel)) {
			listaSel = this.getLista();
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();

		Pattern pattern = Pattern.compile("\"(.*?)\"");

		for (XcpImportarRelatorioVO vo : listaSel) {
			Path pathDir = fori.toPath().resolve(vo.getCodObjeto());
			Path path = null;

			if (Util.isEmpty(vo.getCodModelo())) {
				path = pathDir.resolve(vo.getCodObjeto() + ".jasper");
			} else {
				path = pathDir.resolve(vo.getCodModelo() + ".jasper");
			}

			File fprinc = path.toFile();
			if (!fprinc.exists()) {
				this.listerr.add("[principal] cod_objeto = " + vo.getCodObjeto() + " Arquivo " + fprinc.getAbsolutePath()
						+ " nao existe para ser lido! ignorando linha selecionada");
				continue;
			}

			Path pathsrc = null;
			if (Util.isEmpty(vo.getCodModelo())) {
				pathsrc = pathDir.resolve(vo.getCodObjeto() + ".jrxml");
			} else {
				pathsrc = pathDir.resolve(vo.getCodModelo() + ".jrxml");
			}

			File fprincsrc = pathsrc.toFile();
			if (!fprincsrc.exists()) {
				this.listerr.add("[principal] cod_objeto = " + vo.getCodObjeto() + " Arquivo " + fprincsrc.getAbsolutePath()
						+ " nao existe para ser lido! ignorando linha selecionada");
				continue;
			}

			/*
			 * Abre o relatorio a procura de subrelatorios
			 */
			JasperReport report = (JasperReport) JRLoader.loadObject(fprinc);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JRXmlWriter.writeReport(report, baos, "UTF-8");

			NodeList nl = (NodeList) xPath.evaluate(
					"//*[local-name()='subreportExpression']",
					new InputSource(new StringReader(new String(baos.toByteArray()))),
					XPathConstants.NODESET);

			int length = nl.getLength();
			for (int i = 0; i < length; i++) {

				String text = nl.item(i).getTextContent();

				Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {

					String desJasper = matcher.group(1);

					File fsub = pathDir.resolve(desJasper).toFile();
					if (!fsub.exists()) {
						this.listerr.add("[subrelatorio] cod_objeto =" + vo.getCodObjeto() + " Arquivo " + fsub.getAbsolutePath()
								+ " nao existe para ser lido! ignorando linha selecionada");
						continue;
					}

					String desModelo = desJasper.replace(".jasper", "");

					File fsubsrc = pathDir.resolve(desModelo + ".jrxml").toFile();
					if (!fsubsrc.exists()) {
						this.listerr.add("[subrelatorio] cod_objeto =" + vo.getCodObjeto() + " Arquivo " + fsubsrc.getAbsolutePath()
								+ " nao existe para ser lido! ignorando linha selecionada");
						continue;
					}

					MontaQuery q = new MontaQuery("Select e from XcpObjetoModelo e ");
					q.addWhere("codObjeto", "=", vo.getCodObjeto());
					q.addWhere("desNome", "=", desModelo);

					XcpObjetoModelo mod = null;

					List ls = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
					if (Util.isEmpty(ls)) {
						mod = new XcpObjetoModelo();
						mod.setTipo(XcpObjetoModelo.TIP_MODELO_2_SUBRELATORIO);
						mod.setCodObjeto(vo.getCodObjeto());
						mod.setDesModelo(desModelo);
						mod.setDesNome(desModelo);
						mod.setIndCabRod(INT_0);
						mod.setIndPaisagem(INT_0);
						mod.setNumOrdem(1L);
						mod = this.persist.update(this.getEjbVars(), mod);
					} else {
						mod = (XcpObjetoModelo) ls.get(0);
					}

					try (FileInputStream fis = new FileInputStream(fsubsrc);
							Connection conn = DBConnect.getConnection(this.getEjbVars());
							PreparedStatement pstmt = conn.prepareStatement("UPDATE xcp_objeto_modelo set obj_fonte = ? where seq_modelo = ?");) {
						pstmt.setBinaryStream(1, fis);
						pstmt.setLong(2, mod.getSeqModelo());
						pstmt.execute();
					}

					Files.createDirectories(Paths.get(dirRel).resolve(vo.getCodObjeto()));
					Files.copy(fsub.toPath(), Paths.get(dirRel).resolve(vo.getCodObjeto()).resolve(desJasper), StandardCopyOption.REPLACE_EXISTING);
				}
			}

			XcpObjetoModelo mod = new XcpObjetoModelo();

			//se deu tudo certo na gravacao dos subrelatorio agora pode criar o principal.
			if (Util.isEmpty(vo.getCodModelo())) {
				String desNome = null;
				String desModelo = null;
				if (Util.isEmpty(vo.getCodModelo())) {
					desNome = vo.getCodObjeto();
					desModelo = "Geral";
				} else {
					desNome = vo.getCodModelo();
					desModelo = vo.getCodModelo();
				}

				mod.setTipo(XcpObjetoModelo.TIP_MODELO_1_PRINCIPAL);
				mod.setCodObjeto(vo.getCodObjeto());
				mod.setDesModelo(desModelo);
				mod.setDesNome(desNome);
				mod.setIndCabRod(INT_0);
				mod.setIndPaisagem(INT_0);
				mod.setNumOrdem(1L);
				mod = this.persist.update(this.getEjbVars(), mod);
			} else {
				mod.setSeqModelo(vo.getSeqModelo());
			}

			try (FileInputStream fis = new FileInputStream(fprincsrc);
					Connection conn = DBConnect.getConnection(this.getEjbVars());
					PreparedStatement pstmt = conn.prepareStatement("UPDATE xcp_objeto_modelo set obj_fonte = ? where seq_modelo = ?");) {
				pstmt.setBinaryStream(1, fis);
				pstmt.setLong(2, mod.getSeqModelo());
				pstmt.execute();
			}

			Files.createDirectories(Paths.get(dirRel).resolve(vo.getCodObjeto()));
			Files.copy(fprinc.toPath(), Paths.get(dirRel).resolve(vo.getCodObjeto()).resolve(fprinc.getName()), StandardCopyOption.REPLACE_EXISTING);

		}

		if (this.listerr.size() > 0) {
			getRequestContext().execute("PF('w_msg').show()");
		} else {
			addFacesMessage(this.getTraducao("msg_processo_executado_sucesso"), MSG_INFO);
		}

		this.actionPesquisar();
	}

	public String getErros() {
		StringBuilder sb = new StringBuilder();
		if (!Util.isEmpty(this.listerr)) {
			for (String err : this.listerr) {
				sb.append(err);
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	public boolean isSemModelo() {
		return this.semModelo;
	}

	public void setSemModelo(boolean semModelo) {
		this.semModelo = semModelo;
	}

	public boolean isSemFonte() {
		return this.semFonte;
	}

	public void setSemFonte(boolean semFonte) {
		this.semFonte = semFonte;
	}

	public void setLista(List<XcpImportarRelatorioVO> lista) {
		this.lista = lista;
	}

	public List<XcpImportarRelatorioVO> getListaFiltrados() {
		return this.listaFiltrados;
	}

	public void setListaFiltrados(List<XcpImportarRelatorioVO> listaFiltrados) {
		this.listaFiltrados = listaFiltrados;
	}

	public List<XcpImportarRelatorioVO> getListaSel() {
		return this.listaSel;
	}

	public void setListaSel(List<XcpImportarRelatorioVO> listaSel) {
		this.listaSel = listaSel;
	}

	public String getDesOrigem() {
		return this.desOrigem;
	}

	public void setDesOrigem(String desOrigem) {
		this.desOrigem = desOrigem;
	}

}