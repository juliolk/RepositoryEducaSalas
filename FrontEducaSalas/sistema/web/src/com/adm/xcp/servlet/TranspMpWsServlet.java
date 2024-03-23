package com.adm.xcp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.TranspMpWsContrachequeExercAntVO;
import com.adm.ejb.vo.TranspMpWsDadosCCFGVO;
import com.adm.ejb.vo.TranspMpWsDadosCadastraisVO;
import com.adm.ejb.vo.TranspMpWsDadosCargosVagosVO;
import com.adm.ejb.vo.TranspMpWsDadosConsursoVO;
import com.adm.ejb.vo.TranspMpWsDadosCurriculosVO;
import com.adm.ejb.vo.TranspMpWsDadosProvimentosVO;
import com.adm.ejb.vo.TranspMpWsDadosVacanciasVO;
import com.adm.ejb.vo.TranspMpWsExperienciaVO;
import com.adm.ejb.vo.TranspMpWsFormacaoVO;
import com.adm.ejb.vo.TranspMpWsRemuneracaoVO;
import com.adm.ejb.vo.TranspMpWsVerbasIndenizatoriasVO;
import com.adm.gui.vo.BuscasWsFiltro;
import com.adm.util.Formatter;
import com.adm.util.Util;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = { TranspMpWsServlet.BUSCA_REMUNERACOES, TranspMpWsServlet.BUSCA_DADOS_CADASTRAIS, TranspMpWsServlet.BUSCA_DADOS_CONCURSO,
		TranspMpWsServlet.BUSCA_DADOS_CARGOS_VAGOS, TranspMpWsServlet.BUSCA_DADOS_CC_FG, TranspMpWsServlet.BUSCA_DADOS_CURRICULOS,
		TranspMpWsServlet.BUSCA_DADOS_VACANCIAS, TranspMpWsServlet.BUSCA_CONTRACHEQUE_EXERC_ANT, TranspMpWsServlet.BUSCA_VERBAS_INDENIZATORIAS,
		TranspMpWsServlet.BUSCA_DADOS_PROVIMENTOS, TranspMpWsServlet.BUSCA_FORMACAO, TranspMpWsServlet.BUSCA_EXPERIENCIA })
public class TranspMpWsServlet extends HttpServlet {

	static final String BUSCA_REMUNERACOES = "/transparencia/remuneracoes";
	static final String BUSCA_DADOS_CADASTRAIS = "/transparencia/dados-cadastrais";
	static final String BUSCA_DADOS_CONCURSO = "/transparencia/dados-concurso";
	static final String BUSCA_DADOS_CARGOS_VAGOS = "/transparencia/dados-cargos-vagos";
	static final String BUSCA_DADOS_CC_FG = "/transparencia/dados-cc-fg";
	static final String BUSCA_DADOS_CURRICULOS = "/transparencia/dados-curriculos";
	static final String BUSCA_DADOS_VACANCIAS = "/transparencia/dados-vacancias";
	static final String BUSCA_CONTRACHEQUE_EXERC_ANT = "/transparencia/contracheque-exerc-ant";
	static final String BUSCA_VERBAS_INDENIZATORIAS = "/transparencia/verbas-indenizatorias";
	static final String BUSCA_DADOS_PROVIMENTOS = "/transparencia/dados-provimentos";
	static final String BUSCA_FORMACAO = "/transparencia/formacao";
	static final String BUSCA_EXPERIENCIA = "/transparencia/experiencia";

	@EJB
	private XcpQuerySession session;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			if (req.getRequestURI().endsWith(BUSCA_REMUNERACOES)) {
				this.remuneracoes(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_CADASTRAIS)) {
				this.buscaDadosCadastrais(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_CONCURSO)) {
				this.buscaDadosConcurso(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_CARGOS_VAGOS)) {
				this.buscaDadosCargosVagos(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_CC_FG)) {
				this.buscaDadosCCFG(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_CURRICULOS)) {
				this.buscaDadosCurriculos(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_VACANCIAS)) {
				this.buscaDadosVacancias(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_CONTRACHEQUE_EXERC_ANT)) {
				this.buscaContrachequeExercAnt(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_VERBAS_INDENIZATORIAS)) {
				this.buscaVerbasIndenizatorias(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_DADOS_PROVIMENTOS)) {
				this.buscaDadosProvimentos(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_FORMACAO)) {
				this.buscaFormacao(req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_EXPERIENCIA)) {
				this.buscaExperiencia(req, resp);
			}
		} catch (Exception e) {
			e.printStackTrace();

			//throw new ServletException(e);
			resp.setStatus(500);
			resp.getWriter().write(e.getMessage());
			e.printStackTrace(resp.getWriter());

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

	private void remuneracoes(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		String referencia = "";
		String matricula = "";
		String cargo = "";
		String setor = "";
		String tipoServidor = "";

		BuscasWsFiltro filtro = this.getFiltro(req);

		if (filtro == null || filtro.getReferencia() == null) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro mês/ano de referência (parâmetro referencia) deve ser informado no formato mm/yyyy.");
			return;
		}

		referencia = "01/" + filtro.getReferencia();

		//Valida formato do parâmtro referencia
		Date dtReferencia = Formatter.converteData(referencia, "dd/MM/yyyy"); //retorna null se não conseguir converter
		if (dtReferencia == null) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro mês/ano de referência (parâmetro referencia) deve ser informado no formato mm/yyyy.");
			return;
		}

		if (filtro.getMatricula() != null) {
			matricula = filtro.getMatricula();
		}
		if (filtro.getCargo() != null) {
			cargo = filtro.getCargo();
		}
		if (filtro.getSetor() != null) {
			setor = filtro.getSetor();
		}
		if (filtro.getTiposervidor() != null) {
			tipoServidor = filtro.getTiposervidor();
		}

		Map<String, Object> parMap = new HashMap<String, Object>();

		parMap.put("referencia", referencia);
		parMap.put("matricula", matricula);
		parMap.put("cargo", cargo);
		parMap.put("setor", setor);
		parMap.put("tiposervidor", tipoServidor);

		List<TranspMpWsRemuneracaoVO> list = this.session.executeNativeQueryList(
				null,
				"SELECT t.* FROM TABLE(pkg_buscas_ws_transp_mp.busca_remuneracoes(&referencia, &matricula, &cargo, &setor, &tiposervidor)) t",
				TranspMpWsRemuneracaoVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaDadosCadastrais(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		//Valida parâmetro tipoServidor
		if (filtro == null || filtro.getTiposervidor() == null || filtro.getTiposervidor().isEmpty()) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro tiposervidor é obrigatório.");
			return;
		}

		Date dtReferencia = null;
		//Valida parâmetro referencia
		if (filtro.getReferencia() != null) {

			String referencia = "01/" + filtro.getReferencia();
			dtReferencia = Formatter.converteData(referencia, "dd/MM/yyyy"); //retorna null se não conseguir converter
			if (dtReferencia == null) {
				resp.setStatus(400);
				resp.getWriter().write("Filtro mês/ano de referência (parâmetro referencia) deve ser informado no formato mm/yyyy.");
				return;
			}
		}

		String tipoServidor = filtro.getTiposervidor();
		String teletrabalho = (filtro.getEm_Teletrabalho() != null) ? filtro.getEm_Teletrabalho() : "";
		String fg_cc = (filtro.getFg_cc() != null) ? filtro.getFg_cc() : "";
		String nome = (filtro.getNome() != null) ? filtro.getNome() : "";
		String nmcargo = (filtro.getNmcargo() != null) ? filtro.getNmcargo() : "";
		String nmlotacao = (filtro.getNmlotacao() != null) ? filtro.getNmlotacao() : "";
		String instituidor = (filtro.getInstituidor() != null) ? filtro.getInstituidor() : "";

		Map<String, Object> parMap = new HashMap<String, Object>();

		parMap.put("dtreferencia", (dtReferencia != null) ? dtReferencia : "");
		parMap.put("tiposervidor", tipoServidor);
		parMap.put("teletrabalho", teletrabalho);
		parMap.put("fg_cc", fg_cc);
		parMap.put("nome", nome);
		parMap.put("nmcargo", nmcargo);
		parMap.put("nmlotacao", nmlotacao);
		parMap.put("instituidor", instituidor);

		String sqlQuery = "SELECT t.* FROM TABLE(pkg_buscas_ws_transp_mp.busca_dados_cadastrais("
				+ "in_referencia => :dtreferencia, in_tipo_servidor => :tiposervidor, "
				+ "in_teletrabalho => :teletrabalho, in_fg_cc => :fg_cc, in_nome => :nome, "
				+ "in_nmcargo => :nmcargo, in_nmlotacao => :nmlotacao, in_instituidor => :instituidor)) t";

		List<TranspMpWsDadosCadastraisVO> list = this.session.executeNativeQueryList(null, sqlQuery, TranspMpWsDadosCadastraisVO.class, parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}

	}

	private void buscaDadosConcurso(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		List<TranspMpWsDadosConsursoVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_concurso()) t",
				TranspMpWsDadosConsursoVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}

	}

	private void buscaDadosCargosVagos(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		//Valida parâmetro tipoServidor
		if (filtro == null || filtro.getTiposervidor() == null || filtro.getTiposervidor().isEmpty()) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro tiposervidor é obrigatório.");
			return;
		}
		String tipoServidor = filtro.getTiposervidor();

		Map<String, Object> parMap = new HashMap<String, Object>();

		parMap.put("tiposervidor", tipoServidor);

		List<TranspMpWsDadosCargosVagosVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_cagos_vagos(&tiposervidor)) t",
				TranspMpWsDadosCargosVagosVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaDadosCCFG(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		List<TranspMpWsDadosCCFGVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_cc_fg()) t",
				TranspMpWsDadosCCFGVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaDadosCurriculos(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		List<TranspMpWsDadosCurriculosVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_curriculos()) t",
				TranspMpWsDadosCurriculosVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaDadosVacancias(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		String tipoServidor = "";
		if (filtro != null && filtro.getTiposervidor() != null) {
			tipoServidor = filtro.getTiposervidor();
		}

		Date dtReferencia = null;
		//Valida parâmetro referencia
		if (filtro != null && filtro.getReferencia() != null) {

			String referencia = "01/" + filtro.getReferencia();
			dtReferencia = Formatter.converteData(referencia, "dd/MM/yyyy"); //retorna null se não conseguir converter
			if (dtReferencia == null) {
				resp.setStatus(400);
				resp.getWriter().write("Filtro mês/ano de referência (parâmetro referencia) deve ser informado no formato mm/yyyy.");
				return;
			}
		}

		String cargo = "";
		if (filtro != null && filtro.getCargo() != null) {
			cargo = filtro.getCargo();
		}

		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("tiposervidor", tipoServidor);
		parMap.put("dtreferencia", (dtReferencia != null) ? dtReferencia : "");
		parMap.put("cargo", cargo);

		List<TranspMpWsDadosVacanciasVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_vacancias(&tiposervidor, &dtreferencia, &cargo)) t",
				TranspMpWsDadosVacanciasVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaContrachequeExercAnt(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		List<TranspMpWsContrachequeExercAntVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_contracheque_exerc_ant()) t",
				TranspMpWsContrachequeExercAntVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaVerbasIndenizatorias(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Map<String, Object> parMap = new HashMap<String, Object>();

		List<TranspMpWsVerbasIndenizatoriasVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_verbas_indenizatorias()) t",
				TranspMpWsVerbasIndenizatoriasVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaDadosProvimentos(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		String tipoServidor = "";
		if (filtro != null && filtro.getTiposervidor() != null) {
			tipoServidor = filtro.getTiposervidor();
		}

		Date dtReferenciaIni = null;
		//Valida parâmetro referencia
		if (filtro != null && filtro.getDt_aposentadoria_inicial() != null) {

			String ReferenciaIni = filtro.getDt_aposentadoria_inicial();
			dtReferenciaIni = Formatter.converteData(ReferenciaIni, "dd/MM/yyyy"); //retorna null se não conseguir converter
			if (dtReferenciaIni == null) {
				resp.setStatus(400);
				resp.getWriter().write("Formato de data invalido informado para o filtro de data de referencia inicial");
				return;
			}
		}

		Date dtReferenciaFim = null;
		//Valida parâmetro referencia
		if (filtro != null && filtro.getDt_aposentadoria_inicial() != null) {

			String ReferenciaFim = filtro.getDt_aposentadoria_inicial();
			dtReferenciaFim = Formatter.converteData(ReferenciaFim, "dd/MM/yyyy"); //retorna null se não conseguir converter
			if (dtReferenciaFim == null) {
				resp.setStatus(400);
				resp.getWriter().write("Formato de data invalido informado para o filtro de data de referencia final");
				return;
			}
		}

		String cargo = "";
		if (filtro != null && filtro.getCargo() != null) {
			cargo = filtro.getCargo();
		}

		String nome = "";
		if (filtro != null && filtro.getNome() != null) {
			nome = filtro.getNome();
		}

		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("tiposervidor", tipoServidor);
		parMap.put("referencia_ini", (dtReferenciaIni != null) ? dtReferenciaIni : "");
		parMap.put("referencia_fim", (dtReferenciaFim != null) ? dtReferenciaFim : "");
		parMap.put("cargo", cargo);
		parMap.put("nome", nome);

		List<TranspMpWsDadosVacanciasVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_dados_provimentos(in_tipo_servidor => &tiposervidor,"
						+ "in_referencia_ini => &referencia_ini, in_referencia_fim => &referencia_fim, in_cargo => &cargo, in_nome => &nome)) t",
				TranspMpWsDadosProvimentosVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaFormacao(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		//Valida parâmetro matricula
		if (filtro == null || filtro.getMatricula() == null || filtro.getMatricula().isEmpty()) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro matricula é obrigatório.");
			return;
		}

		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("matricula", filtro.getMatricula());

		List<TranspMpWsDadosVacanciasVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_formacao(&matricula)) t",
				TranspMpWsFormacaoVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

	private void buscaExperiencia(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BuscasWsFiltro filtro = this.getFiltro(req);

		//Valida parâmetro matricula
		if (filtro == null || filtro.getMatricula() == null || filtro.getMatricula().isEmpty()) {
			resp.setStatus(400);
			resp.getWriter().write("Filtro matricula é obrigatório.");
			return;
		}

		Map<String, Object> parMap = new HashMap<String, Object>();
		parMap.put("matricula", filtro.getMatricula());

		List<TranspMpWsDadosVacanciasVO> list = this.session.executeNativeQueryList(
				null,
				"select * from table(pkg_buscas_ws_transp_mp.busca_experiencia(&matricula)) t",
				TranspMpWsExperienciaVO.class,
				parMap);

		if (Util.isEmpty(list)) {
			resp.setStatus(400);
			resp.getWriter().write("Nenhum registro encontrado.");
		} else {
			resp.setContentType("application/json");
			new GsonBuilder().setDateFormat("dd/MM/yyyy").create().toJson(list, resp.getWriter());
		}
	}

}
