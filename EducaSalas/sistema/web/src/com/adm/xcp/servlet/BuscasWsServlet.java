package com.adm.xcp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;

import com.adm.ejb.entity.Cidades;
import com.adm.ejb.entity.Documentospessoais;
import com.adm.ejb.entity.Vinculos;
import com.adm.ejb.entity.extend.CalendarioSetor;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.BuscaWsSetorVO;
import com.adm.ejb.vo.BuscasWsArquivoVO;
import com.adm.ejb.vo.BuscasWsCargoVO;
import com.adm.ejb.vo.BuscasWsCidadeVO;
import com.adm.ejb.vo.BuscasWsDefVO;
import com.adm.ejb.vo.BuscasWsEmailVO;
import com.adm.ejb.vo.BuscasWsFeriadoVO;
import com.adm.ejb.vo.BuscasWsFonesVO;
import com.adm.ejb.vo.BuscasWsFuncVO;
import com.adm.ejb.vo.BuscasWsGestoresVO;
import com.adm.ejb.vo.BuscasWsListaVincVO;
import com.adm.ejb.vo.BuscasWsMatriculaVO;
import com.adm.ejb.vo.BuscasWsSubsVO;
import com.adm.ejb.vo.BuscasWsVinculoVO;
import com.adm.gui.vo.BuscasWsFiltro;
import com.adm.gui.vo.BuscasWsFiltroMat;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.StringUtil;
import com.adm.util.Util;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = { BuscasWsServlet.BUSCA_VINCULOS, BuscasWsServlet.BUSCA_PESSOAS, BuscasWsServlet.BUSCA_CARGOS,
		BuscasWsServlet.BUSCA_MATRICULAS_POR_LOTACAO, BuscasWsServlet.BUSCA_FERIADOS, BuscasWsServlet.TJCE_BUSCA_SERV_ATIV_ESTAG,
		BuscasWsServlet.TJCE_BUSCA_SERV_ATIV_DESEMP, BuscasWsServlet.TJCE_BUSCA_SUBS_DESEMP, BuscasWsServlet.TJCE_BUSCA_SUBS_ESTAG,
		BuscasWsServlet.BUSCA_GESTORES, BuscasWsServlet.BUSCA_LISTA_VINCULOS, BuscasWsServlet.BUSCA_SETORES, BuscasWsServlet.BUSCA_FOTO })
public class BuscasWsServlet extends HttpServlet {

	static final String BUSCA_VINCULOS = "/busca/vinculos";
	static final String BUSCA_PESSOAS = "/busca/pessoas";
	static final String BUSCA_CARGOS = "/busca/cargos";
	static final String BUSCA_SETORES = "/busca/setores";
	static final String BUSCA_GESTORES = "/busca/gestores";
	static final String BUSCA_FOTO = "/busca/foto";
	static final String BUSCA_LISTA_VINCULOS = "/busca/lista-vinculos";
	static final String BUSCA_MATRICULAS_POR_LOTACAO = "/busca/matriculas-por-lotacao";
	static final String BUSCA_FERIADOS = "/busca/feriados";
	static final String TJCE_BUSCA_SERV_ATIV_ESTAG = "/busca/tjce-servidores-ativos-estag";
	static final String TJCE_BUSCA_SERV_ATIV_DESEMP = "/busca/tjce-servidores-ativos-desemp";
	static final String TJCE_BUSCA_SUBS_ESTAG = "/busca/tjce-substituicoes-estag";
	static final String TJCE_BUSCA_SUBS_DESEMP = "/busca/tjce-substituicoes-desemp";

	@EJB
	private XcpQuerySession session;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			if (req.getRequestURI().endsWith(BUSCA_LISTA_VINCULOS)) {
				this.buscaListaVinculo(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_VINCULOS)) {

				BuscasWsFiltro filtro = this.getFiltro(req);
				if (filtro == null
						|| (Util.isEmpty(filtro.getCpf()) && Util.isEmpty(filtro.getMatriculas()) && Util.isEmpty(filtro.getCargos()) && Util.isEmpty(filtro.getVinculos()))) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro cpf, matriculas, cargos ou vinculos deve ser informado.");
					return;
				}

				if (!Util.isEmpty(filtro.getCpf())) {
					if (filtro.getCpf().trim().length() == 0) {
						resp.setStatus(400);
						resp.getWriter().write("Filtro cpf, matriculas, cargos ou vinculos deve ser informado.");
						return;
					}
				}

				this.buscaVinculos(filtro, req, resp);

			} else if (req.getRequestURI().endsWith(BUSCA_PESSOAS)) {

				BuscasWsFiltro filtro = this.getFiltro(req);
				if (filtro == null || (Util.isEmpty(filtro.getCpfs()) && Util.isEmpty(filtro.getNomes()))) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro cpfs ou nomes deve ser informado.");
					return;
				}

				this.buscaPessoas(filtro, req, resp);

			} else if (req.getRequestURI().endsWith(BUSCA_CARGOS)) {
				this.buscaCargos(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_SETORES)) {
				this.buscaSetores(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_MATRICULAS_POR_LOTACAO)) {

				BuscasWsFiltro filtro = this.getFiltro(req);

				if (filtro == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtros devem ser informados.");
					return;
				}

				if (Util.isEmpty(filtro.getLotacao()) && Util.isEmpty(filtro.getCargos()) && Util.isEmpty(filtro.getVinculos())) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro lotacao ou cargos ou vinculos deve ser informado.");
					return;
				}

				this.buscaMatriculas(filtro, req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_FERIADOS)) {

				BuscasWsFiltro filtro = this.getFiltro(req);
				if (filtro == null || filtro.getDtinicio() == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro dtinicio deve ser informado.");
					return;
				}

				if (filtro.getDtfim() == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro dtfim deve ser informado.");
					return;
				}

				this.buscaFeriados(filtro, req, resp);

			} else if (req.getRequestURI().endsWith(TJCE_BUSCA_SERV_ATIV_ESTAG) || req.getRequestURI().endsWith(TJCE_BUSCA_SUBS_ESTAG)) {

				BuscasWsFiltro filtro = this.getFiltro(req);
				if (filtro == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtros devem ser informados.");
					return;
				}

				Date dtinicio = Converter.toDate(filtro.getDtinicio());
				if (dtinicio == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro data de inicio deve ser informado no formato dd/mm/yyyy.");
					return;
				}

				Date dtfim = Converter.toDate(filtro.getDtfim());
				if (dtfim == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro data de fim deve ser informado no formato dd/mm/yyyy.");
					return;
				}

				if (req.getRequestURI().endsWith(TJCE_BUSCA_SERV_ATIV_ESTAG)) {
					this.buscaServAtivosEstag(2, dtinicio, dtfim, resp);
				} else {
					this.buscaSubstituicoes(2, dtinicio, dtfim, resp);
				}

			} else if (req.getRequestURI().endsWith(TJCE_BUSCA_SERV_ATIV_DESEMP) || req.getRequestURI().endsWith(TJCE_BUSCA_SUBS_DESEMP)) {

				BuscasWsFiltro filtro = this.getFiltro(req);
				if (filtro == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtros devem ser informados.");
					return;
				}

				Date dtinicio = Converter.toDate(filtro.getDtinicio());
				if (dtinicio == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro data de inicio deve ser informado no formato dd/mm/yyyy.");
					return;
				}

				Date dtfim = Converter.toDate(filtro.getDtfim());
				if (dtfim == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro data de fim deve ser informado no formato dd/mm/yyyy.");
					return;
				}

				if (req.getRequestURI().endsWith(TJCE_BUSCA_SERV_ATIV_DESEMP)) {
					this.buscaServAtivosEstag(1, dtinicio, dtfim, resp);
				} else {
					this.buscaSubstituicoes(1, dtinicio, dtfim, resp);
				}

			} else if (req.getRequestURI().endsWith(BUSCA_GESTORES)) {
				BuscasWsFiltro filtro = this.getFiltro(req);
				this.buscaGestores(filtro, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_FOTO)) {
				BuscasWsFiltroMat filtro = new GsonBuilder().create().fromJson(this.getBody(req), BuscasWsFiltroMat.class);

				if (filtro.getMatricula() == null) {
					resp.setStatus(400);
					resp.getWriter().write("Filtro matricula deve ser informado.");
					return;
				}

				this.buscaFoto(resp, filtro.getEmpresa(), filtro.getMatricula());
			}
		} catch (Exception e) {
			e.printStackTrace();

			//throw new ServletException(e);
			resp.setStatus(500);
			resp.getWriter().write(e.getMessage());
			e.printStackTrace(resp.getWriter());

		}

	}

	private void buscaGestores(BuscasWsFiltro filtro, HttpServletResponse resp) throws IOException, ServletException {
		Map<String, Object> parMap = new HashMap<String, Object>();
		if (filtro == null) {
			parMap.put("empresa", "");
			parMap.put("setor", "");
		} else {
			if (filtro.getEmpresa() == null) {

				parMap.put("empresa", "");
			} else {
				parMap.put("empresa", filtro.getEmpresa());
			}

			if (filtro.getSetor() == null) {
				parMap.put("setor", "");
			} else {
				parMap.put("setor", filtro.getSetor());
			}
		}
		List<BuscasWsVinculoVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_ws.busca_gestores(&empresa, &setor)) t",
				BuscasWsGestoresVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}

	}

	private void buscaVinculos(BuscasWsFiltro filtro, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		Map<String, Object> parMap = new HashMap<String, Object>();
		if (filtro.getCpf() == null) {
			parMap.put("cpf", "");
		} else {
			parMap.put("cpf", filtro.getCpf());
		}
		parMap.put("matriculas", filtro.getMatriculas());
		parMap.put("cargos", filtro.getCargos());
		parMap.put("vinculos", filtro.getVinculos());

		List<BuscasWsVinculoVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_ws.busca_vinculos(&cpf, &matriculas, &cargos, &vinculos)) t",
				BuscasWsVinculoVO.class,
				parMap);

		for (BuscasWsVinculoVO b : list) {

			if (!Util.isEmpty(b.getTels())) {
				String[] split = b.getTels().split(";-;");
				for (String tupla : split) {
					String[] teltipo = tupla.split(Pattern.quote("|-|"));
					BuscasWsFonesVO vo = new BuscasWsFonesVO();
					if (!Util.isEmpty(teltipo[0])) {
						vo.setTipo(Converter.toInteger(teltipo[0]));
					}
					vo.setFone(teltipo[1]);
					b.getFones().add(vo);
				}
			}

			if (!Util.isEmpty(b.getMails())) {
				String[] split = b.getMails().split(";-;");
				for (String tupla : split) {
					String[] mailtipo = tupla.split(Pattern.quote("|-|"));
					BuscasWsEmailVO vo = new BuscasWsEmailVO();
					if (!Util.isEmpty(mailtipo[0])) {
						vo.setTipo(Converter.toInteger(mailtipo[0]));
					}
					vo.setEmail(mailtipo[1]);
					b.getEmails().add(vo);
				}
			}
		}

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaPessoas(BuscasWsFiltro filtro, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		parMap.put("cpfs", filtro.getCpfs());
		parMap.put("nomes", filtro.getNomes());
		List<BuscasWsVinculoVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_ws.busca_pessoas(&cpfs, &nomes)) t",
				BuscasWsVinculoVO.class,
				parMap);

		for (BuscasWsVinculoVO b : list) {
			if (!Util.isEmpty(b.getMails())) {
				String[] split = b.getMails().split(";-;");
				for (String tupla : split) {
					String[] mailtipo = tupla.split(Pattern.quote("|-|"));
					BuscasWsEmailVO vo = new BuscasWsEmailVO();
					if (!Util.isEmpty(mailtipo[0])) {
						vo.setTipo(Converter.toInteger(mailtipo[0]));
					}
					vo.setEmail(mailtipo[1]);
					b.getEmails().add(vo);
				}
			}

			if (!Util.isEmpty(b.getDefs())) {
				String[] split = b.getDefs().split(";-;");
				for (String tupla : split) {
					String[] cmps = tupla.split(Pattern.quote("|-|"), -1);
					BuscasWsDefVO vo = new BuscasWsDefVO();
					vo.setDeficiencia(Converter.toInteger(cmps[0]));
					vo.setDescricao(cmps[1]);
					if (!Util.isEmpty(cmps[2])) {
						vo.setNecessidade(cmps[2]);
					}
					vo.setDtinicio(Converter.toDate(cmps[3]));
					if (!Util.isEmpty(cmps[4])) {
						vo.setCongenita(Converter.toInteger(cmps[4]));
					}
					b.getDeficiencias().add(vo);
				}
			}

		}

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private BuscasWsFiltro getFiltro(HttpServletRequest req) throws IOException {
		return new GsonBuilder().create().fromJson(this.getBody(req), BuscasWsFiltro.class);
	}

	private String getBody(HttpServletRequest req) throws IOException {
		try (ServletInputStream inputStream = req.getInputStream(); ByteArrayOutputStream result = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[1024];
			for (int length; (length = inputStream.read(buffer)) != -1;) {
				result.write(buffer, 0, length);
			}
			return result.toString();
		}
	}

	private void buscaCargos(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Map<String, Object> parMap = new HashMap<String, Object>();

		StringBuilder q = new StringBuilder();

		q.append("SELECT cargos.cargo                     ");
		q.append("      ,cargos.descricao     nmcargo     ");
		q.append("      ,cargos.tipo                      ");
		q.append("      ,tipocargos.descricao nmtipocargo ");
		q.append("      ,cargos.situacao                  ");
		q.append("      ,decode(cargos.situacao, 0, 'Extinto', 1, 'Ativo', 2, 'Em Extinção') nmsituacao  ");
		q.append("  FROM cargos                           ");
		q.append("  JOIN tipocargos                       ");
		q.append("    ON tipocargos.tipo = cargos.tipo    ");

		List<BuscasWsCargoVO> list = this.session.executeNativeQueryList(null, q.toString(), BuscasWsCargoVO.class, parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaSetores(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Map<String, Object> parMap = new HashMap<String, Object>();

		StringBuilder q = new StringBuilder();

		q.append("SELECT orgao ,nmorgao          ");
		q.append("      ,divisao, nmdivisao      ");
		q.append("      ,setor, nmsetor          ");
		q.append("      ,CASE WHEN situacao = 1  ");
		q.append("        THEN 'ATIVO'           ");
		q.append("        ELSE 'INATIVO'         ");
		q.append("      END AS situacao          ");
		q.append("      ,jurisdicao tipo 		  ");
		q.append("      ,descjurisdicao desctipo ");
		q.append("      ,dtinicio, dtfim			  ");
		q.append("  FROM setores_view            ");

		List<BuscaWsSetorVO> list = this.session.executeNativeQueryList(null, q.toString(), BuscaWsSetorVO.class, parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}

	}

	private void buscaMatriculas(BuscasWsFiltro filtro, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Map<String, Object> parMap = new HashMap<String, Object>();

		if (filtro.getLotacao() != null) {
			parMap.put("lotacao", filtro.getLotacao());
		}

		StringBuilder q = new StringBuilder();

		q.append("SELECT v.empresa                 ");
		q.append("      ,v.matricula               ");
		q.append("      ,v.nome                    ");
		q.append("      ,v.vinculo                 ");
		q.append("      ,v.nmvinculo               ");
		q.append("  FROM funcionarios_view v       ");
		q.append(" WHERE (v.clftipmov NOT IN (1, 5) or (v.clftipmov IN (1, 5) and v.dtmov>=trunc(sysdate))) ");

		if (filtro.getLotacao() != null) {
			q.append("   AND v.setor = &lotacao        ");
		}

		StringBuilder sbcargo = new StringBuilder();
		if (!Util.isEmpty(filtro.getCargos())) {
			String[] split = filtro.getCargos().split(",");
			for (String cargo : split) {
				if (Util.isEmpty(cargo)) {
					continue;
				}
				sbcargo.append(cargo);
				sbcargo.append(",");
			}
		}

		if (sbcargo.length() > 0) {
			sbcargo.setLength(sbcargo.length() - 1);
			q.append(" AND v.cargo in ( ");
			q.append(sbcargo.toString());
			q.append(" ) ");
		}

		StringBuilder sbvinc = new StringBuilder();
		if (!Util.isEmpty(filtro.getVinculos())) {
			String[] split = filtro.getVinculos().split(",");
			for (String vinc : split) {
				if (Util.isEmpty(vinc)) {
					continue;
				}
				sbvinc.append(vinc);
				sbvinc.append(",");
			}
		}

		if (sbvinc.length() > 0) {
			sbvinc.setLength(sbvinc.length() - 1);
			q.append(" AND v.vinculo in ( ");
			q.append(sbvinc.toString());
			q.append(" ) ");
		}

		List<BuscasWsMatriculaVO> list = this.session.executeNativeQueryList(null, q.toString(), BuscasWsMatriculaVO.class, parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaFeriados(BuscasWsFiltro filtro, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Date dtaini = Converter.toDate(filtro.getDtinicio());
		Date dtafim = Converter.toDate(filtro.getDtfim());

		List<BuscasWsFeriadoVO> listvo = new ArrayList<BuscasWsFeriadoVO>();
		MontaQuery q = new MontaQuery("Select e from CalendarioSetor e");
		q.addWhereBetween("data", dtaini, dtafim);
		List<CalendarioSetor> listcal = this.session.executeQueryList(null, q);
		for (CalendarioSetor cal : listcal) {
			BuscasWsFeriadoVO calvo = new BuscasWsFeriadoVO();
			calvo.setData(cal.getData());
			calvo.setDescricao(StringUtil.retiraAcentos(cal.getDescricao()));
			calvo.setPonto(cal.getPonto());
			calvo.setSetor(cal.getSetor());
			if (cal.getSetorFk() != null) {
				calvo.setDescsetor(StringUtil.retiraAcentos(cal.getSetorFk().getDescricao()));
			}

			MontaQuery q2 = new MontaQuery("Select e from Cidades e");
			q2.addWhere("calendario", "=", cal.getCalendario());
			List<Cidades> listcid = this.session.executeQueryList(null, q2);
			for (Cidades cid : listcid) {
				BuscasWsCidadeVO cidvo = new BuscasWsCidadeVO();
				cidvo.setCodigo(cid.getCidade());
				cidvo.setCodibge(cid.getCodmunicipio());
				cidvo.setDescricao(StringUtil.retiraAcentos(cid.getDescricao()));
				calvo.getCidades().add(cidvo);
			}
			listvo.add(calvo);
		}

		if (Util.isEmpty(listvo)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(listvo, resp.getWriter());
		}
	}

	private void buscaServAtivosEstag(Integer tipo, Date dtaini, Date dtafim, HttpServletResponse resp) throws IOException {
		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("tipo", tipo);
		parMap.put("per_ini", dtaini);
		parMap.put("per_fim", dtafim);

		List<BuscasWsFuncVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_tjce.busca_func_cargos(&tipo, &per_ini, &per_fim)) t",
				BuscasWsFuncVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaSubstituicoes(Integer tipo, Date dtaini, Date dtafim, HttpServletResponse resp) throws IOException {
		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("tipo", tipo);
		parMap.put("per_ini", dtaini);
		parMap.put("per_fim", dtafim);

		List<BuscasWsSubsVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_tjce.busca_substituicoes(&tipo, &per_ini, &per_fim)) t",
				BuscasWsSubsVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaListaVinculo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MontaQuery q = new MontaQuery("Select e from Vinculos e");
		List<Vinculos> list = this.session.executeQueryList(null, q);
		List<BuscasWsListaVincVO> listvo = new ArrayList<BuscasWsListaVincVO>();
		for (Vinculos v : list) {
			BuscasWsListaVincVO vo = new BuscasWsListaVincVO();
			vo.setVinculo(v.getVinculo());
			vo.setDescricao(StringUtil.retiraAcentos(v.getDescricao()));
			listvo.add(vo);
		}

		if (Util.isEmpty(listvo)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(listvo, resp.getWriter());
		}

	}

	private void buscaFoto(HttpServletResponse resp, Integer empresa, Long matricula) throws IOException {
		MontaQuery mq = new MontaQuery("Select e from Documentospessoais e");
		if (empresa != null) {
			mq.addWhere("empresa", "=", empresa);
		}
		mq.addWhere("matricula", "=", matricula);
		mq.addWhere("tipdocumento", "=", Documentospessoais.TIPDOCUMENTO_1_FOTO);

		List<Documentospessoais> list = this.session.executeQueryList(null, mq);
		List<BuscasWsArquivoVO> listvo = new ArrayList<BuscasWsArquivoVO>();

		Detector detector = new DefaultDetector();

		if (!Util.isEmpty(list)) {
			for (Documentospessoais p : list) {
				BuscasWsArquivoVO vo = new BuscasWsArquivoVO();
				vo.setEmpresa(p.getEmpresa());
				vo.setMatricula(p.getMatricula());
				vo.setArquivo(DatatypeConverter.printBase64Binary(p.getArquivo()));

				TikaInputStream tis = TikaInputStream.get(p.getArquivo());
				MediaType mediaType = detector.detect(tis, new Metadata());
				TikaConfig config = TikaConfig.getDefaultConfig();
				try {
					MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
					vo.setMime(mimeType.getName());
				} catch (MimeTypeException e) {
					//ignora
				}

				//	resp.setContentType(mimeType.getName());

				listvo.add(vo);
			}
		}

		if (Util.isEmpty(listvo)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(listvo, resp.getWriter());
		}

	}
}
