package com.adm.xcp.backing;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.XcpObjeto;
import com.adm.ejb.entity.XcpObjetoModelo;
import com.adm.ejb.entity.extend.XcpObjetoManut;
import com.adm.ejb.session.remote.XcpObjetoSession;
import com.adm.ejb.util.ConstantesValidacao;
import com.adm.gui.backing.XcpObjetoGruposBacking;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;

@ManagedBean
@ViewScoped
public class XcpObjetoBacking extends XcpManutencaoBacking implements Serializable {

	@EJB
	private XcpObjetoSession session;

	private String desNovoNome;

	private boolean isPermidido() {
		if (super.isClienteStatic()) {
			if (this.getEntity().getCodObjeto() != null) {
				if (!this.getEntity().getCodObjeto().matches(ConstantesValidacao.OJBETO_VALIDO_CLIENTE(super.getCodClienteStatic()))) {
					return false;
				}
			}
		}
		return true;
	}

	private String montaNomePadrao() {
		if (super.isClienteStatic()) {
			StringBuffer sb = new StringBuffer();
			sb.append("CLI_");
			sb.append(super.getCodClienteStatic());
			sb.append("_");
			return sb.toString();
		}
		return null;
	}

	public XcpObjetoBacking() {
		this.setTipomManut(TIPO_MANUT.MESTRE);
		this.setDetalhesBacking(XcpObjetoGruposBacking.class, XcpObjetoModeloBacking.class, XcpObjetoParametroBacking.class);
	}

	@Override
	public XcpObjetoManut getEntity() {
		XcpObjetoManut entity = (XcpObjetoManut) super.getEntity();
		if (entity == null) {
			entity = new XcpObjetoManut();
			entity.setCodObjeto(this.montaNomePadrao());
			entity.setTipObjeto(XcpObjeto.TIP_OBJETO_1_RELATORIO);
			entity.setIndDownload(INT_0);
			entity.setIndUpload(INT_0);
			entity.setIndAgendamento(INT_1);
			entity.setIndSalvarConfig(INT_1);
			entity.setIndPaisagem(INT_1);
			entity.setIndCancelar(INT_1);
			entity.setIndLimpar(INT_1);
			entity.setIndCabRod(INT_1);
			entity.setIndSegundos(INT_0);
			entity.setIndVincdoc(INT_0);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpObjetoManut.class, "codObjeto"));
	}

	public Collection<SelectItem> getItensTipObjeto() {
		return this.getItens(
				"tipObjeto",
				XcpObjeto.TIP_OBJETO_1_RELATORIO,
				XcpObjeto.TIP_OBJETO_2_CONSULTA,
				XcpObjeto.TIP_OBJETO_3_PROCESSO,
				XcpObjeto.TIP_OBJETO_4_ARQ_SQL);
	}

	@Override
	public boolean gravar() throws Exception {
		if (this.getEntity().isNovo() && !this.isPermidido()) {
			addFacesMessage(this.getTraducao("msg_nome_invalido_obj"));
			return false;
		}
		String sql = this.getEntity().getDesSqlPadrao();
		if (sql != null) {
			Pattern pattern = Pattern.compile(
					"\\b(DELETE|INSERT|DROP|CREATE|CALL|MERGE|LOCK|COMMENT|RENAME|TRUNCATE|REPLACE|ALTER|EXEC|EXECUTE)\\b",
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(sql);
			if (matcher.find()) {
				addFacesMessage(this.getTraducao("msg_comando_sql_invalido", matcher.group(1)));
				return false;
			}
		}
		if (XcpObjeto.TIP_OBJETO_4_ARQ_SQL.equals(this.getEntity().getTipObjeto())) {
			this.getEntity().setDesProcedure("xcp_exp_file_prc");
			this.getEntity().setDesClasse(null);
			this.getEntity().setIndDownload(INT_1);
			if (sql == null) {
				addFacesMessage(this.getTraducao("msg_comando_sql_invalido"));
				return false;
			}
			if (!sql.toUpperCase().contains(" DES_LINHA")) {
				addFacesMessage(this.getTraducao("msg_comando_sql_invalido", "Falta DES_LINHA no comando"));
				return false;
			}
		}
		boolean gravar = super.gravar();

		if (gravar && XcpObjeto.TIP_OBJETO_2_CONSULTA.equals(this.getEntity().getTipObjeto())) {
			MontaQuery q = new MontaQuery("delete from XcpObjetoModelo e");
			q.addWhere("codObjeto", "=", this.getEntity().getCodObjeto());
			this.xcpPersistSession.executeQuery(this.getEjbVars(), q);
			//Recarrega os modelos
			this.carregarDetalhes();
		}
		return gravar;
	}

	public void actionDuplicarObj() throws Exception {
		if (Util.isEmpty(this.getDesNovoNome())) {
			addFacesMessage(this.getTraducao("msg_nome_invalido_obj"));
		}
		if (super.isClienteStatic()) {
			if (!this.getDesNovoNome().matches(ConstantesValidacao.OJBETO_VALIDO_CLIENTE(super.getCodClienteStatic()))) {
				addFacesMessage(this.getTraducao("msg_nome_invalido_obj"));
				return;
			}
		}

		this.session.duplicar(this.getEjbVars(), this.getEntity(), this.getDesNovoNome());

		String dirRel = this.getParametroString("Sistema", 4);
		if (!Util.isEmpty(dirRel)) {
			Path p = Paths.get(dirRel);
			MontaQuery q = new MontaQuery("Select e from XcpObjetoModelo e");
			q.addWhere("codObjeto", "=", this.getEntity().getCodObjeto());
			List<XcpObjetoModelo> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			for (XcpObjetoModelo m : list) {
				Path prel = p.resolve(this.getEntity().getCodObjeto()).resolve(m.getDesNome() + ".jasper");
				if (prel.toFile().exists()) {
					Path pdest = p.resolve(this.getDesNovoNome());
					Files.createDirectories(pdest);
					Files.copy(prel, pdest.resolve(m.getDesNome() + ".jasper"));
				}
			}
		}
		addFacesMessage(this.getTraducao("msg_duplicado_sucesso"), MSG_INFO);
		getRequestContext().execute("PF('dlgDup_w').hide();");
		this.setModo(MODO_LISTA);
	}

	public String getDesNovoNome() {
		if (Util.isEmpty(this.desNovoNome)) {
			this.desNovoNome = this.montaNomePadrao();
		}
		return this.desNovoNome;
	}

	public void setDesNovoNome(String desNovoNome) {
		this.desNovoNome = desNovoNome;
	}

	public void changeIndUpload() {
		this.getEntity().setDesUploadCharset(null);
	}

	@Override
	public void selecionaEntidade(XcapeEntity entity) throws Exception {

		if (entity != null) {
			this.validateObjeto();

			XcpObjetoManut xcpObj = ((XcpObjetoManut) entity);
			if (xcpObj.getIndVincdoc() == null) {
				xcpObj.setIndVincdoc(INT_0);
			}
		}

		super.selecionaEntidade(entity);
	}

	public void validateObjeto() {

		StringBuilder sb = new StringBuilder();
		sb.append("  SELECT des_parametro_sql  ");
		sb.append("    FROM (SELECT p.des_parametro_sql ");
		sb.append("            FROM xcp_objeto_parametro p ");
		sb.append("           WHERE cod_objeto = :cod_objeto ");
		sb.append("          UNION ALL ");
		sb.append("          SELECT p.des_parametro_sql ");
		sb.append("            FROM xcp_objeto_parametro p ");
		sb.append("           WHERE p.cod_grupo IN ");
		sb.append("                 (SELECT g.cod_grupo FROM xcp_objeto_grupos g WHERE g.cod_objeto = :cod_objeto)) ");
		sb.append("   GROUP BY des_parametro_sql ");
		sb.append("  HAVING COUNT(1) > 1 ");

		Map map = new HashMap<String, Object>();
		map.put("cod_objeto", this.getEntity().getCodObjeto());
		List<String> listPars = this.xcpQuerySession.executeNativeQueryList(this.getEjbVars(), sb.toString(), map);

		if (!Util.isEmpty(listPars)) {
			for (String par : listPars) {
				addFacesMessage(this.getTraducao("msg_conflito", par), MSG_INFO);
			}
		}
	}

	public Collection<SelectItem> getItensTipUtilizacao() {
		return this.getItens(
				"tipUtilizacaoObj",
				XcpObjeto.UTILIZACAO_1_FERIAS,
				XcpObjeto.UTILIZACAO_2_AFASTAMENTOS,
				XcpObjeto.UTILIZACAO_3_PARAMETROS,
				XcpObjeto.UTILIZACAO_4_LICENCA,
				XcpObjeto.UTILIZACAO_7_ACORDO,
				XcpObjeto.UTILIZACAO_8_TRANSFERENCIA,
				XcpObjeto.UTILIZACAO_9_PENALIDADES,
				XcpObjeto.UTILIZACAO_10_PROVIMENTO,
				XcpObjeto.UTILIZACAO_11_AVERBACOES,
				XcpObjeto.UTILIZACAO_12_COMISSOES,
				XcpObjeto.UTILIZACAO_13_INQUERITOS);
	}
}
