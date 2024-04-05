package com.adm.xcp.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.adm.ejb.entity.Empresas;
import com.adm.ejb.entity.ReqDocsMobile;
import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.entity.XcpObjeto;
import com.adm.ejb.entity.XcpObjetoParametro;
import com.adm.ejb.entity.XcpTipoCampo;
import com.adm.ejb.entity.custom.XcpObjetosPars;
import com.adm.ejb.session.remote.XcpExecObjSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.util.XcpExecObjUtil;
import com.adm.ejb.vo.XcpExecObjInputVO;
import com.adm.ejb.vo.XcpExecObjVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.vo.XcpEjbVars;
import com.adm.xcp.util.IpAddressUtil;

@WebServlet("/reqdocs")
public class ReqDocsMobileServlet extends HttpServlet {

	@EJB
	private XcpQuerySession xcpQuerySession;
	
	@EJB
	private XcpExecObjSession xcpExecObjSession;
	
	@EJB
	private XcpPersistSession xcpPersistSession;	

	private static Logger log = Logger.getLogger(ReqDocsMobileServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletOutputStream out = resp.getOutputStream();
		
		try {

			String strEmpresa = req.getParameter("empresa");
			String strMatricula = req.getParameter("matricula");
			String strOperador = req.getParameter("operador");
			String hash = req.getParameter("hash");
			String codObjeto = "";
			String nomeModelo = "";
			
			UUID reqDocUUID = UUID.fromString(hash);			
			
			Integer empresa = Integer.valueOf(strEmpresa);
			Long operador = Long.valueOf(strOperador);
			Long matricula = Long.valueOf(strMatricula);
			
			MontaQuery mq = new MontaQuery("Select e from ReqDocsMobile e");
			mq.addWhere("uuid", "=", reqDocUUID);
			mq.addWhere("empresa", "=", empresa);
			mq.addWhere("matricula", "=", matricula);
			mq.addWhere("dataVisualizacao is null");
			
			List<ReqDocsMobile> listReqDocsMobile = xcpQuerySession.executeQueryList(null, mq);
			ReqDocsMobile reqDocsMobile = null;
			if (listReqDocsMobile.size() == 0) {
				resp.setContentType("application/json");
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				String retorno = "{\"status\": \"erro\",\"code\": 500, \"messages\": \"Requisição já visualizada ou não encontrada\"}";
				
				out.write(retorno.getBytes(StandardCharsets.UTF_8));
				return;
			}
			else {
				reqDocsMobile = listReqDocsMobile.get(0);
				codObjeto = reqDocsMobile.getCodObjeto(); 
				nomeModelo = reqDocsMobile.getNomeModelo();
			}
			
			Map<String, Object> p = new HashMap<String, Object>();
			p.put(XcpEjbVars.EMPRESA, empresa);
			p.put(XcpEjbVars.OPERADOR, operador);
			p.put(XcpEjbVars.CLASS_NAME, this.getClass().getSimpleName());
			p.put(XcpEjbVars.COD_OBJETO, codObjeto);
			p.put(XcpEjbVars.NOME_MODELO, nomeModelo);
			p.put(XcpEjbVars.SIMPLE_PAGE_NAME, "downloadDocumentoMobile");
			p.put(XcpEjbVars.IP_ACESSO, IpAddressUtil.getRequestIpAddress(req));
			p.put(XcpEjbVars.IP_SERVIDOR, Util.getHostAddress());
			p.put(XcpEjbVars.NUM_PORTA, req.getRemotePort());

			p.put(XcpEjbVars.RESTRICAO_VINCULO, null);
			p.put(XcpEjbVars.RESTRICAO_DIVISAO, null);
			p.put(XcpEjbVars.RESTRICAO_ORGAO, null);
			p.put(XcpEjbVars.RESTRICAO_SETOR, null);
			
			XcpEjbVars ejbVars = new XcpEjbVars(p);
			
			if (empresa != null) {
				Empresas emp1 = xcpQuerySession.find(ejbVars, Empresas.class, empresa);
				if (emp1 != null) {
					ejbVars.getSystemVars().put(XcpEjbVars.EMPRESA_NOME, emp1.getNome());
				}
			}
			
			Sessoes sessao = new Sessoes();
			sessao.setInicio(new Date());
			sessao.setIpv4(req.getRemoteAddr());
			sessao.setOperador(operador);
			sessao.setPorta(req.getLocalPort());
			sessao.setServidor(req.getLocalAddr());
			sessao.setTipo(Sessoes.TIPO_2_ENCERRADA);
			sessao.setTermino(new Date());
			sessao.setEmpresa(empresa);
			sessao = xcpPersistSession.update(ejbVars, sessao);
			ejbVars.getSystemVars().put(XcpEjbVars.SESSAO, sessao.getSessao());			

			
			XcpExecObjInputVO input = new XcpExecObjInputVO(codObjeto, nomeModelo);
			input.setReportParams(reqDocsMobile.getParametrosMap());
		
			Long seqExecucao = null;

			XcpObjeto xcpObjeto = this.xcpQuerySession.find(ejbVars, XcpObjeto.class, input.getCodObjeto());
			if (xcpObjeto != null) {
				XcpExecObjVO execObj = this.xcpExecObjSession.carregarXcpExecucao(ejbVars, xcpObjeto, input, seqExecucao);
				
				if (execObj != null && XcpObjeto.TIP_OBJETO_1_RELATORIO.equals(execObj.getXcpObjeto().getTipObjeto())) {
					List<XcpObjetosPars> lista = execObj.getListaParametros();
					if (!Util.isEmpty(lista)) {
						for (XcpObjetosPars objPar : lista) {

							if (XcpTipoCampo.TIP_PARAMETRO_3_TEXTO.equals(objPar.getTipParametro())) {
								if (XcpObjetoParametro.TIP_PARAMETRO_0_UNICO.equals(objPar.getTipParametroIntervalo())) {
									if (Util.isEmpty(objPar.getTextValue())) {
										objPar.setTextValue(null);
									}
								} else if (XcpObjetoParametro.TIP_PARAMETRO_1_INTERVALO.equals(objPar.getTipParametroIntervalo())) {
									if (Util.isEmpty(objPar.getTextValue())) {
										objPar.setTextValue(null);
									}
									if (Util.isEmpty(objPar.getTextValueEnd())) {
										objPar.setTextValueEnd(null);
									}
								}
							}
						}
					}

					if (execObj.getXcpObjeto() != null) {

						log.debug("executando objeto: " + execObj.getXcpObjeto().getCodObjeto());
						execObj.setDesArquivo(execObj.getXcpObjeto().getDesNomeArqDownload());
						//Executa o objeto
						execObj = this.xcpExecObjSession.executar(ejbVars, execObj);

						String fileName = XcpExecObjUtil.getOutputFileName(execObj.getXcpExecucao());
						StreamedContent file = new DefaultStreamedContent(new FileInputStream(execObj.getArquivoGerado()), Util.toMimeType(fileName), fileName);
						
						resp.setContentType("application/pdf");

						try (BufferedInputStream is = new BufferedInputStream(file.getStream(), 10240);
								BufferedOutputStream output = new BufferedOutputStream(out, 10240)) {

							byte[] buffer = new byte[10240];
							int length;
							while ((length = is.read(buffer)) > 0) {
								output.write(buffer, 0, length);
							}
						}
						
						reqDocsMobile.setDataVisualizacao(Calendar.getInstance().getTime());
						xcpPersistSession.update(ejbVars, reqDocsMobile);
						
						return;
					}
				}
			} else {
				resp.setContentType("application/json");
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				String retorno = "{\"status\": \"erro\",\"code\": 500, \"messages\": \"Objeto requisitado não encontrado\"}";
				
				out.write(retorno.getBytes(StandardCharsets.UTF_8));
				return;
			}			
		} catch (Exception e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			String retorno = e.getMessage();
			
			out.write(retorno.getBytes(StandardCharsets.UTF_8));
			return;
			
		}

	}
}
