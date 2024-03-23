package com.adm.xcp.backing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.DRMargin;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.expression.Expressions;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRCompiler;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.XcpObjetoModelo;
import com.adm.ejb.entity.XcpTipoLista;
import com.adm.ejb.entity.custom.XcpObjetosPars;
import com.adm.ejb.entity.extend.XcpObjetoManut;
import com.adm.ejb.entity.extend.XcpObjetoModeloManut;
import com.adm.ejb.session.remote.XcpExecObjSession;
import com.adm.ejb.vo.XcpExecObjConsultaColunaVO;
import com.adm.ejb.vo.XcpExecObjVO;
import com.adm.util.Converter;
import com.adm.util.DBConnect;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.XcpUtil;
import com.adm.util.ejb.XcapeEntity;

@ManagedBean
@ViewScoped
public class XcpObjetoModeloBacking extends XcpManutencaoBacking<XcpObjetoBacking> implements Serializable, XcpEjbConstants {

	@EJB
	private XcpExecObjSession xcpExecObjSession;

	private Path pathRel;
	private StreamedContent file;

	private static final Logger logger = Logger.getLogger(XcpObjetoModeloBacking.class.getName());

	public XcpObjetoModeloBacking() {
		this.setTipomManut(TIPO_MANUT.DETALHE);
		this.setMestreBackingClass(XcpObjetoBacking.class);
	}

	@PostConstruct
	public synchronized void postConstruct() {
		try {
			String dirRel = this.getParametroString("Sistema", 4);
			this.pathRel = Paths.get(dirRel);
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	@Override
	public XcpObjetoModeloManut getEntity() {
		XcpObjetoModeloManut entity = (XcpObjetoModeloManut) super.getEntity();
		if (entity == null) {
			entity = new XcpObjetoModeloManut();
			entity.setNumOrdem(0L);
			entity.setIndPaisagem(INT_0);
			entity.setTipo(XcpObjetoModelo.TIP_MODELO_1_PRINCIPAL);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		//Busca a entidade PAI e se não for nova busca os detalhes
		XcpObjetoManut entityMestre = this.getMestreBacking().getEntity();
		if (!entityMestre.isNovo()) {
			MontaQuery q = new MontaQuery(XcpObjetoModeloManut.class, "numOrdem,desModelo");
			q.addWhere("codObjeto", "=", entityMestre.getCodObjeto());
			return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo()) {
			//Seta a chave do PAI
			XcpObjetoManut entityMestre = this.getMestreBacking().getEntity();
			this.getEntity().setCodObjeto(entityMestre.getCodObjeto());
		}

		this.setPesquisaNoVoltar(true);
		return super.gravar();
	}

	@Override
	public boolean remover() throws Exception {
		MontaQuery delete = new MontaQuery("update XcpExecucao e set e.seqModelo = null");
		delete.addWhere("seqModelo", "=", this.getEntity().getSeqModelo());
		this.xcpPersistSession.executeQuery(this.getEjbVars(), delete);

		return super.remover();
	}

	@Override
	public void selecionaEntidade(XcapeEntity entity) throws Exception {
		super.selecionaEntidade(entity);
	}

	public Path getPathRel() {
		return this.pathRel;
	}

	public Path getJasperFile() {
		if (this.getPathRel() != null) {
			return this.getPathRel().resolve(this.getMestreBacking().getEntity().getCodObjeto()).resolve(this.getEntity().getDesNome() + ".jasper");
		}
		return null;
	}

	public Path getJasperDir() {
		if (this.getPathRel() != null) {
			return this.getPathRel().resolve(this.getMestreBacking().getEntity().getCodObjeto());
		}
		return null;
	}

	public boolean isTemJasper() {
		Path jasperFile = this.getJasperFile();
		if (jasperFile != null) {
			return jasperFile.toFile().exists();
		}
		return false;
	}

	public boolean isTemJrxml() {
		if (this.getEntity().getSeqModelo() != null) {
			Map p = new HashMap();
			p.put("seq_modelo", this.getEntity().getSeqModelo());
			List list = this.xcpQuerySession.executeNativeQueryList(
					this.getEjbVars(),
					"select CASE WHEN OBJ_FONTE is null THEN 0 ELSE 1 END from xcp_objeto_modelo where seq_modelo = :seq_modelo",
					p);
			if (!Util.isEmpty(list)) {
				return INT_1.equals(Converter.toInteger(list.get(0)));
			}
		}
		return false;
	}

	public StreamedContent getFile() {
		return this.file;
	}

	public void actionGerarJrxml() {
		try {
			this.gravar();

			String sql = this.getMestreBacking().getEntity().getDesSqlPadrao();
			if (Util.isEmpty(sql)) {
				//TODO Cadastrar mensagem "Para gerar o relatório é necessário informar o SQL"
				addFacesMessage(this.getTraducao("msg_falta_sql_gerar_jrxml"), MSG_WARN);
				return;
			}

			List queryParams = new ArrayList();
			StringBuffer resultSql = new StringBuffer();
			XcpUtil.montaQuery(sql, new HashMap<String, Object>(), new HashMap<String, Object>(), resultSql, queryParams, XcpUtil.TIPO_QUERY.JDBC_GET_META_DATA);
			try (Connection conn = DBConnect.getConnection(this.getEjbVars());) {
				try (PreparedStatement stmt = conn.prepareStatement(resultSql.toString())) {
					ResultSetMetaData metaData = stmt.getMetaData();

					JasperReportBuilder report = DynamicReports.report();
					report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
					report.getReport().getPage().setMargin(new DRMargin(10));

					StringBuffer query = new StringBuffer();
					XcpUtil.montaQuery(sql, new HashMap<String, Object>(), new HashMap<String, Object>(), query, queryParams, XcpUtil.TIPO_QUERY.CREATE_JASPER);
					report.setQuery(query.toString());

					//Monta as colunas
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						XcpExecObjConsultaColunaVO col = new XcpExecObjConsultaColunaVO(i, metaData.getColumnName(i), Class.forName(metaData.getColumnClassName(i)));

						Class fieldClass = String.class;
						if (Util.in(col.getFormat(), XcpExecObjConsultaColunaVO.COL_NUMBER)) {
							fieldClass = BigDecimal.class;
						} else if (Util.in(
								col.getFormat(),
								XcpExecObjConsultaColunaVO.COL_DATE,
								XcpExecObjConsultaColunaVO.COL_TIME,
								XcpExecObjConsultaColunaVO.COL_DATETIME)) {
							fieldClass = Date.class;
						}

						TextColumnBuilder<String> column = Columns.column(col.getName(), fieldClass);
						column.setTitle(Expressions.jasperSyntaxText(col.getLabel()));
						report.addColumn(column);
					}

					//Monta os parametros
					report.addParameter("empresa", Integer.class);
					report.addParameter("operador", Long.class);

					List<XcpObjetosPars> listPars = this.xcpExecObjSession.findListaXcpObjetosPars(
							this.getEjbVars(),
							this.getMestreBacking().getEntity().getCodObjeto());
					for (XcpObjetosPars p : listPars) {

						if (p.isMultiplo()) {
							report.addParameter(p.getDesParametroSql(), String.class);
							report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_MULTI_SIZE, Integer.class);
						} else if (p.isLov()) {
							if (XcpTipoLista.TIP_CODIGO_1_STRING.equals(p.getCodLovFk().getTipCodigo())) {
								report.addParameter(p.getDesParametroSql(), String.class);
							} else if (XcpTipoLista.TIP_CODIGO_4_DATE.equals(p.getCodLovFk().getTipCodigo())) {
								report.addParameter(p.getDesParametroSql(), Date.class);
							} else {
								report.addParameter(p.getDesParametroSql(), BigDecimal.class);
							}
						} else if (p.isRange()) {
							if (p.isDate()) {
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_INI, Date.class);
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_FIM, Date.class);
							} else if (p.isNumber()) {
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_INI, BigDecimal.class);
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_FIM, BigDecimal.class);
							} else {
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_INI, String.class);
								report.addParameter(p.getDesParametroSql() + XcpExecObjVO.PAR_INTERVALO_FIM, String.class);
							}
						} else {
							if (p.isDate()) {
								report.addParameter(p.getDesParametroSql(), Date.class);
							} else if (p.isNumber()) {
								report.addParameter(p.getDesParametroSql(), BigDecimal.class);
							} else {
								report.addParameter(p.getDesParametroSql(), String.class);
							}
						}

					}

					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						report.toJrXml(out);
						this.file = new DefaultStreamedContent(new ByteArrayInputStream(out.toByteArray()), "application/jrxml", this.getEntity().getDesNome()
								+ ".jrxml");
					}

					getRequestContext().execute("PF('wBtnDownloadModelo').getJQ().click();");

					//TODO Cadastrar mensagem "Relatório gerado com sucesso."
					addFacesMessage(this.getTraducao("msg_jrxml_gerado_sucesso"), MSG_INFO);

				}
			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
		//		}
	}

	public void actionDownloadJrxml() {
		try {
			this.gravar();

			this.file = null;

			try (Connection conn = DBConnect.getConnection(this.getEjbVars());
					PreparedStatement pstmt = conn.prepareStatement("select obj_fonte from xcp_objeto_modelo where seq_modelo = ?");) {
				pstmt.setLong(1, this.getEntity().getSeqModelo());
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Blob blob = rs.getBlob("obj_fonte");
						if (blob != null) {
							this.file = new DefaultStreamedContent(blob.getBinaryStream(), "application/jrxml", this.getEntity().getDesNome() + ".jrxml");
						}
					}
				}
			}

			if (this.file == null) {
				//TODO Cadastrar mensagem "Arquivo fonte do relatório não encontrado"
				addFacesMessage(this.getTraducao("msg_jrxml_nao_encontrado"), MSG_INFO);
			} else {
				getRequestContext().execute("PF('wBtnDownloadModelo').getJQ().click();");
			}

		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public void actionUploadJrxml(FileUploadEvent event) {
		try {
			this.gravar();

			try (InputStream in = event.getFile().getInputstream();
					Connection conn = DBConnect.getConnection(this.getEjbVars());
					PreparedStatement pstmt = conn.prepareStatement("UPDATE xcp_objeto_modelo set obj_fonte = ? where seq_modelo = ?");) {

				if (this.getSession().getUsuario().getNome().equals("Maycon")) {
					System.out.println(123123);
				}

				pstmt.setBinaryStream(1, in);
				pstmt.setLong(2, this.getEntity().getSeqModelo());
				pstmt.execute();
			}
			this.getJasperFile().getParent().toFile().mkdirs();

			logger.info("Caminho para upload: " + this.getJasperFile().toFile().getAbsolutePath());

			try (InputStream in = event.getFile().getInputstream(); FileOutputStream out = new FileOutputStream(this.getJasperFile().toFile())) {
				DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();

				JRPropertiesUtil.getInstance(context).setProperty(JRCompiler.COMPILER_TEMP_DIR, this.getJasperFile().getParent().toFile().getAbsolutePath());
				/**
				 * Uma juncao de setar o classpath do compilador, com achar o javac
				 * via setando o PATH no .sh e necessario para funcioonar.
				 */
				String home = System.getProperty("jboss.home.dir");
				if (!Util.isEmpty(home)) {
					String absolutePath = Paths.get(home, "/modules/com/adm/main/*").toFile().getAbsolutePath();
					JRPropertiesUtil.getInstance(context).setProperty(JRCompiler.COMPILER_CLASSPATH, absolutePath);
				}
				//O jasper procura o jdtcompiler e depois o compilador no path. Setando o path do bin ja resolve.
				JasperCompileManager.getInstance(context).compileToStream(in, out);
			}
			//TODO Cadastrar mensagem "Upload realizado com sucesso"
			addFacesMessage(this.getTraducao("msg_jrxml_upload_sucesso"), MSG_INFO);
			getRequestContext().execute("PF('wDlgUploadJrxml').hide();");
			getRequestContext().update("form:btnsModelo");
			getRequestContext().update("form:outTemJrxml");
			getRequestContext().update("form:outTemJasper");

		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public void actionCompilarJrxml() {
		try {
			this.gravar();

			//Busca do banco para fazer download
			try (Connection conn = DBConnect.getConnection(this.getEjbVars());
					PreparedStatement pstmt = conn.prepareStatement("select obj_fonte from xcp_objeto_modelo where seq_modelo = ?");) {
				pstmt.setLong(1, this.getEntity().getSeqModelo());
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Blob blob = rs.getBlob("obj_fonte");
						if (blob != null) {
							try (FileOutputStream out = new FileOutputStream(this.getJasperFile().toFile())) {
								JasperCompileManager.compileReportToStream(blob.getBinaryStream(), out);
								addFacesMessage(this.getTraducao("msg_compilado_sucesso"), MSG_INFO);
							}
						}
					} else {
						//TODO Cadastrar mensagem "Arquivo fonte do relatório não encontrado"
						addFacesMessage(this.getTraducao("msg_jrxml_nao_encontrado"), MSG_INFO);
					}
				}
			}

		} catch (Exception e) {
			this.addFacesMessage(e);
		}

	}

	public Collection<SelectItem> getItensTipo() {
		return this.getItensSemNull("tipoModelo", XcpObjetoModelo.TIP_MODELO_1_PRINCIPAL, XcpObjetoModelo.TIP_MODELO_2_SUBRELATORIO);
	}

	public Collection<SelectItem> getItensTipoNull() {
		return this.getItens("tipoModelo", XcpObjetoModelo.TIP_MODELO_1_PRINCIPAL, XcpObjetoModelo.TIP_MODELO_2_SUBRELATORIO);
	}

	public StreamedContent getDownloadModelos() throws Exception {
		byte[] pack = this.pack(this.getJasperDir());
		DefaultStreamedContent content = new DefaultStreamedContent(new ByteArrayInputStream(pack));

		StringBuilder sb = new StringBuilder();
		sb.append(this.getMestreBacking().getEntity().getCodObjeto());
		sb.append(".zip");
		content.setName(sb.toString());
		return content;
	}

	private byte[] pack(final Path folder) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(bos)) {
			Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return bos.toByteArray();
	}
}
