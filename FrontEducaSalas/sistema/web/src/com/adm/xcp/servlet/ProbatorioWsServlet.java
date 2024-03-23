package com.adm.xcp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.Boletim;
import com.adm.ejb.entity.BoletimNotas;
import com.adm.ejb.entity.Probatorio;
import com.adm.ejb.session.ProbatorioSessionBean;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.Conhecimento;
import com.adm.ejb.vo.RegistraAvaliacaoReq;
import com.adm.gui.vo.probws.AbreProbatorioReq;
import com.adm.gui.vo.probws.AbreProbatorioResp;
import com.adm.gui.vo.probws.Avaliacao;
import com.adm.gui.vo.probws.BuscaProbatorioReq;
import com.adm.gui.vo.probws.BuscaProbatorioResp;
import com.adm.gui.vo.probws.FechaProbatorioReq;
import com.adm.gui.vo.probws.RegistraAvaliacaoResp;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = { ProbatorioWsServlet.ABRE_PROBATORIO, ProbatorioWsServlet.REGISTRA_AVALIACAO, ProbatorioWsServlet.FECHA_PROBATORIO,
		ProbatorioWsServlet.BUSCA_PROBATORIO })
public class ProbatorioWsServlet extends HttpServlet {

	static final String ABRE_PROBATORIO = "/probatorio/abre-probatorio";
	static final String REGISTRA_AVALIACAO = "/probatorio/registra-avaliacao";
	static final String FECHA_PROBATORIO = "/probatorio/fecha-probatorio";
	static final String BUSCA_PROBATORIO = "/probatorio/busca-probatorio";

	private static final String OK = "OK";
	private static final String NOK = "NOK";

	@EJB
	private XcpQuerySession session;

	@EJB
	private XcpQuerySession query;

	@EJB
	private ProbatorioSessionBean prob;

	@Resource(mappedName = XcpEjbConstants.RESOURCE_DATA_SOURCE)
	private DataSource ds;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			String body = this.getBody(req);

			if (req.getRequestURI().endsWith(ABRE_PROBATORIO)) {
				this.abreProbatorio(body, req, resp);
			} else if (req.getRequestURI().endsWith(REGISTRA_AVALIACAO)) {
				this.registraAvaliacao(body, req, resp);
			} else if (req.getRequestURI().endsWith(FECHA_PROBATORIO)) {
				this.fechaProbatorio(body, req, resp);
			} else if (req.getRequestURI().endsWith(BUSCA_PROBATORIO)) {
				this.buscaProbatorio(body, req, resp);
			}

			resp.setContentType("application/json");

		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().write(e.getMessage());
			e.printStackTrace(resp.getWriter());
		}

	}

	private void abreProbatorio(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		AbreProbatorioResp respvo = new AbreProbatorioResp();

		try {
			AbreProbatorioReq reqvo = this.getGson().fromJson(body, AbreProbatorioReq.class);

			Long prob = null;
			try (Connection conn = this.ds.getConnection()) {
				try (CallableStatement st = conn.prepareCall("{? = call pkg_probatorio_ws.abre_probatorio(?,?,?,?)}")) {
					st.registerOutParameter(1, Types.NUMERIC);
					st.setInt(2, reqvo.getEmpresa());
					st.setLong(3, reqvo.getMatricula());
					st.setDate(4, new java.sql.Date(reqvo.getDataini().getTime()));

					if (reqvo.getDatafim() != null) {
						st.setDate(5, new java.sql.Date(reqvo.getDatafim().getTime()));
					} else {
						st.setNull(5, Types.NULL);
					}

					st.execute();
					prob = st.getLong(1);
				}
			}

			respvo.setStatus(OK);
			respvo.setProbatorio(Converter.toLong(prob));

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		}

		resp.getWriter().write(this.getGson().toJson(respvo));
	}

	private void registraAvaliacao(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		RegistraAvaliacaoResp respvo = new RegistraAvaliacaoResp();

		try {
			RegistraAvaliacaoReq reqvo = this.getGson().fromJson(body, RegistraAvaliacaoReq.class);

			if (Util.isEmpty(reqvo.getNotas())) {
				respvo.setStatus(NOK);
				respvo.setMsg("Necessario informar as notas");
				return;
			}

			this.prob.registraAvaliacaoWS(null, reqvo);

			respvo.setStatus(OK);

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		}

		resp.getWriter().write(this.getGson().toJson(respvo));

	}

	private void fechaProbatorio(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		RegistraAvaliacaoResp respvo = new RegistraAvaliacaoResp();

		try {
			FechaProbatorioReq reqvo = this.getGson().fromJson(body, FechaProbatorioReq.class);

			try (Connection conn = this.ds.getConnection()) {
				try (CallableStatement st = conn.prepareCall("{call pkg_probatorio_ws.fecha_probatorio(?,?)}")) {
					st.setLong(1, reqvo.getProbatorio());
					st.setInt(2, reqvo.getSituacao());
					st.execute();
				}
			}

			respvo.setStatus(OK);

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		}

		resp.getWriter().write(this.getGson().toJson(respvo));

	}

	private void buscaProbatorio(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		BuscaProbatorioResp respvo = new BuscaProbatorioResp();

		try {
			BuscaProbatorioReq reqvo = this.getGson().fromJson(body, BuscaProbatorioReq.class);
			Probatorio prob = this.query.find(null, Probatorio.class, reqvo.getProbatorio());

			respvo.setMatricula(prob.getMatricula());
			respvo.setEmpresa(prob.getEmpresa());
			respvo.setDataini(prob.getInicio());
			respvo.setDatafim(prob.getTermino());

			MontaQuery q = new MontaQuery("Select e from Boletim e");
			q.addWhere("probatorio", "=", prob.getProbatorio());
			q.setOrderBy("numero");
			List<Boletim> lista = this.query.executeQueryList(null, q);

			for (Boletim boletim : lista) {
				Avaliacao ava = new Avaliacao();
				ava.setAvaliador(boletim.getAvaliador());
				ava.setNumero(boletim.getNumero());

				q = new MontaQuery("Select e from BoletimNotas e");
				q.addWhere("seqEtapa", "=", boletim.getSeqEtapa());
				List<BoletimNotas> listnota = this.query.executeQueryList(null, q);

				for (BoletimNotas b : listnota) {
					Conhecimento c = new Conhecimento();
					c.setConhecimento(b.getConhecimento());
					c.setNota(b.getNota());
					ava.getNotas().add(c);
				}
				respvo.getAvaliacoes().add(ava);
			}

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		}

		resp.getWriter().write(this.getGson().toJson(respvo));

	}

	private Gson getGson() throws IOException {
		return new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
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

}
