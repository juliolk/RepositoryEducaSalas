package com.adm.xcp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
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
import com.adm.ejb.entity.Avaliacoes;
import com.adm.ejb.entity.Conhecimentos;
import com.adm.ejb.entity.Cursoacad;
import com.adm.ejb.entity.Formacao;
import com.adm.ejb.entity.FormacaoSituacao;
import com.adm.ejb.entity.Instituicoes;
import com.adm.ejb.entity.Processos;
import com.adm.ejb.entity.Tipoformacao;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.gui.vo.funcws.CodigoDescricao;
import com.adm.gui.vo.funcws.CursoAcad;
import com.adm.gui.vo.funcws.ListaCodigoDescricao;
import com.adm.gui.vo.funcws.ListaConhecimentoReq;
import com.adm.gui.vo.funcws.ListaCursoAcadResp;
import com.adm.gui.vo.funcws.RegistraFormacaoReq;
import com.adm.gui.vo.funcws.RegistraFormacaoResp;
import com.adm.gui.vo.funcws.RegistraProcessoReq;
import com.adm.gui.vo.funcws.RegistraProcessoResp;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = { FuncionariosWsServlet.REGISTRA_FORMACAO, FuncionariosWsServlet.LISTA_CURSO, FuncionariosWsServlet.LISTA_CURSOACAD,
		FuncionariosWsServlet.LISTA_INSTITUICAO, FuncionariosWsServlet.LISTA_SITUACAO, FuncionariosWsServlet.LISTA_AVALIACAO,
		FuncionariosWsServlet.LISTA_CONHECIMENTO, FuncionariosWsServlet.REGISTRA_PROCESSO})
public class FuncionariosWsServlet extends HttpServlet {    

    static final String REGISTRA_PROCESSO = "/funcionario/registra-processo";
	static final String REGISTRA_FORMACAO = "/funcionario/registra-formacao";
	static final String LISTA_CURSO = "/funcionario/lista-curso";
	static final String LISTA_CURSOACAD = "/funcionario/lista-cursoacad";
	static final String LISTA_INSTITUICAO = "/funcionario/lista-instituicao";
	static final String LISTA_SITUACAO = "/funcionario/lista-situacao";
	static final String LISTA_AVALIACAO = "/funcionario/lista-avaliacao";
	static final String LISTA_CONHECIMENTO = "/funcionario/lista-conhecimento";

	private static final String OK = "OK";
	private static final String NOK = "NOK";

	@EJB
	private XcpQuerySession session;

	@EJB
	private XcpQuerySession query;

	@EJB
	private XcpPersistSession persist;

	@Resource(mappedName = XcpEjbConstants.RESOURCE_DATA_SOURCE)
	private DataSource ds;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			String body = this.getBody(req);

			if (req.getRequestURI().endsWith(REGISTRA_PROCESSO)) {
                this.registraProcesso(body, req, resp);
            } else if (req.getRequestURI().endsWith(REGISTRA_FORMACAO)) {
				this.registraFormacao(body, req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_CURSO)) {
				this.listaCurso(req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_CURSOACAD)) {
				this.listaCursoacad(req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_INSTITUICAO)) {
				this.listaInstituicao(req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_SITUACAO)) {
				this.listaSituacao(req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_AVALIACAO)) {
				this.listaAvaliacao(req, resp);
			} else if (req.getRequestURI().endsWith(LISTA_CONHECIMENTO)) {
				this.listaConhecimento(body, req, resp);
			}

			resp.setContentType("application/json");

		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().write(e.getMessage());
			e.printStackTrace(resp.getWriter());
		}

	}

	private void listaConhecimento(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		ListaCodigoDescricao respvo = new ListaCodigoDescricao();

		try {
			ListaConhecimentoReq reqvo = this.getGson().fromJson(body, ListaConhecimentoReq.class);

			MontaQuery q = new MontaQuery("Select e from Conhecimentos e");
			q.addWhere("avaliacao", "=", reqvo.getAvaliacao());
			List<Conhecimentos> list = this.query.executeQueryList(null, q);
			for (Conhecimentos t : list) {
				CodigoDescricao c = new CodigoDescricao();
				c.setCodigo(Converter.toLong(t.getConhecimento()));
				c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
				respvo.getLista().add(c);
			}

			respvo.setStatus(OK);

		} catch (Exception e) {
			respvo.setStatus(NOK);
			respvo.setMsg(e.getMessage());
		}

		resp.getWriter().write(this.getGson().toJson(respvo));
	}

	private void listaAvaliacao(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		MontaQuery q = new MontaQuery("Select e from Avaliacoes e where e.tipo in (1, 3)");

		ListaCodigoDescricao respvo = new ListaCodigoDescricao();
		List<Avaliacoes> list = this.query.executeQueryList(null, q);
		for (Avaliacoes t : list) {
			CodigoDescricao c = new CodigoDescricao();
			c.setCodigo(Converter.toLong(t.getAvaliacao()));
			c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
			respvo.getLista().add(c);
		}

		respvo.setStatus(OK);
		resp.getWriter().write(this.getGson().toJson(respvo));

	}

	private void listaSituacao(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		MontaQuery q = new MontaQuery("Select e from FormacaoSituacao e");

		ListaCodigoDescricao respvo = new ListaCodigoDescricao();
		List<FormacaoSituacao> list = this.query.executeQueryList(null, q);
		for (FormacaoSituacao t : list) {
			CodigoDescricao c = new CodigoDescricao();
			c.setCodigo(Converter.toLong(t.getCodigo()));
			c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
			respvo.getLista().add(c);
		}

		respvo.setStatus(OK);
		resp.getWriter().write(this.getGson().toJson(respvo));
	}

	private void listaInstituicao(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		MontaQuery q = new MontaQuery("select e from Instituicoes e");

		ListaCodigoDescricao respvo = new ListaCodigoDescricao();
		List<Instituicoes> list = this.query.executeQueryList(null, q);
		for (Instituicoes t : list) {
			CodigoDescricao c = new CodigoDescricao();
			c.setCodigo(t.getCodigo());
			c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
			respvo.getLista().add(c);
		}

		respvo.setStatus(OK);
		resp.getWriter().write(this.getGson().toJson(respvo));
	}

	private void listaCurso(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		MontaQuery q = new MontaQuery("select e from Tipoformacao e");

		ListaCodigoDescricao respvo = new ListaCodigoDescricao();

		List<Tipoformacao> list = this.query.executeQueryList(null, q);
		for (Tipoformacao t : list) {
			CodigoDescricao c = new CodigoDescricao();
			c.setCodigo(Converter.toLong(t.getCodigo()));
			c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
			respvo.getLista().add(c);
		}

		respvo.setStatus(OK);
		resp.getWriter().write(this.getGson().toJson(respvo));
	}

	private void listaCursoacad(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		MontaQuery q = new MontaQuery("select e from Cursoacad e");

		ListaCursoAcadResp respvo = new ListaCursoAcadResp();

		List<Cursoacad> list = this.query.executeQueryList(null, q);
		for (Cursoacad t : list) {
			CursoAcad c = new CursoAcad();
			c.setCodigo(t.getCodigo());
			c.setDescricao(StringUtil.retiraAcentos(t.getDescricao()));
			c.setTipo(t.getTipo());
			respvo.getCursosAcad().add(c);
		}

		respvo.setStatus(OK);
		resp.getWriter().write(this.getGson().toJson(respvo));
	}
	
	private void registraProcesso(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
	    RegistraProcessoResp respvo = new RegistraProcessoResp();

        try {
            RegistraProcessoReq reqvo = this.getGson().fromJson(body, RegistraProcessoReq.class);
            
            if(reqvo.getProcesso() == null || reqvo.getDatacriacao() == null || reqvo.getSituacao() == null) {
                respvo.setStatus(NOK);
                respvo.setMsg("Campos obrigatórios: processo, datacriacao(dd/mm/aaaa) e situacao (0-Portaria não foi gerada, 1-Portaria foi gerada).");
                resp.getWriter().write(this.getGson().toJson(respvo));
                return;
            }

            Processos proc = new Processos();

            proc.setProcesso(reqvo.getProcesso());
            proc.setDtcriacao(reqvo.getDatacriacao());
            proc.setSituacao(reqvo.getSituacao());
            proc.setTipo(Processos.TIPO_1_INTEGRADO);

            Processos ett = this.persist.update(null, proc);
            respvo.setProcesso(ett.getProcesso());
            respvo.setStatus(OK);

        } catch (Exception e) {
            respvo.setStatus(NOK);
            respvo.setMsg(e.getMessage());
        }

        resp.getWriter().write(this.getGson().toJson(respvo));
    }

	private void registraFormacao(String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		RegistraFormacaoResp respvo = new RegistraFormacaoResp();

		try {
			RegistraFormacaoReq reqvo = this.getGson().fromJson(body, RegistraFormacaoReq.class);

			Formacao form = new Formacao();

			if (reqvo.getSeqform() != null) {
				form = this.query.find(null, Formacao.class, reqvo.getSeqform());
			}

			form.setEmpresa(reqvo.getEmpresa());
			form.setMatricula(reqvo.getMatricula());
			form.setCurso(reqvo.getCurso());
			form.setAvaliacao(reqvo.getAvaliacao());
			form.setConhecimento(reqvo.getConhecimento());
			form.setCursoacad(reqvo.getCursoAcad());
			form.setDtfim(reqvo.getDtfim());
			form.setDtinicio(reqvo.getDtinicio());
			if (form.getDtregistro() == null) {
				form.setDtregistro(new Date());
			}
			form.setIndPromoutilizado(reqvo.getIndPromoutilizado());
			form.setIndPromovalidado(reqvo.getIndPromovalidado());
			form.setIndRelacaoativ(reqvo.getIndRelacaoativ());
			form.setIndTrabalhoFinal(reqvo.getIndTrabalhoFinal());
			form.setInstituicao(reqvo.getInstituicao());
			form.setObs(reqvo.getObs());
			form.setPontos(reqvo.getPontos());
			form.setQtdhoras(reqvo.getQtdhoras());
			form.setSituacao(reqvo.getSituacao());
			form.setOrigem(Formacao.ORIGEM_2_INTEGRACAO);

			Formacao ett = this.persist.update(null, form);
			respvo.setSeqform(ett.getSeqform());
			respvo.setStatus(OK);

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
