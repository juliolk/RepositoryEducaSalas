package com.adm.xcp.backing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpServletRequest;

import com.adm.ejb.entity.Funcionarios;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.session.local.XcpServerEmailSession;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.vo.XcpSendEmailVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.vo.XcpEjbVars;

@ManagedBean
@RequestScoped
public class XcpEsqueciSenhaBacking extends XcpAbstractBacking {
	private Long matricula;
	private Long cpf;
	private String identidade;
	private java.util.Date dtnascimento;
	private String desCaptcha;

	@EJB
	private OperadoresSession operadoresSession;
	@EJB
	private XcpServerEmailSession serverEmailSession;
	@EJB
	private XcpPersistSession persist;

	public Long getMatricula() {
		return this.matricula;
	}

	public void setMatricula(Long matricula) {
		this.matricula = matricula;
	}

	public Long getCpf() {
		return this.cpf;
	}

	public void setCpf(Long cpf) {
		this.cpf = cpf;
	}

	public String getIdentidade() {
		return this.identidade;
	}

	public void setIdentidade(String identidade) {
		this.identidade = identidade;
	}

	public java.util.Date getDtnascimento() {
		return this.dtnascimento;
	}

	public void setDtnascimento(java.util.Date dtnascimento) {
		this.dtnascimento = dtnascimento;
	}

	public String actionEsqueciSenha() throws Exception {

		boolean refresh = false;
		Long codServidor = this.getParametroGlobalLong("Esqueci senha", 1);
		Long numTexto = this.getParametroGlobalLong("Esqueci senha", 2);

		MontaQuery qf = new MontaQuery("SELECT e FROM Funcionarios e");
		qf.addWhere("matricula", "=", this.getMatricula());
		qf.addWhere("cpf", "=", this.getCpf());
		qf.addWhere("identidade", "=", this.getIdentidade());
		qf.addWhere("dtnascimento", "=", this.getDtnascimento());
		List<Funcionarios> lf = this.xcpQuerySession.executeQueryList(this.getEjbVars(), qf);
		if (!Util.isEmpty(lf)) {
			Funcionarios func = lf.get(0);
			if (Util.isEmpty(func.getEmail())) {
				addFacesMessage(this.getTraducao("msg_esq_sem_email"), MSG_WARN);
				refresh = true;
			} else {

				MontaQuery qo = new MontaQuery("SELECT e FROM Operadores e");
				qo.addWhere("empresa", "=", func.getEmpresa());
				qo.addWhere("matricula", "=", func.getMatricula());
				// Altera somente a senha do operador "Portal"
				qo.addWhere("permissao", "=", 2);

				List<Operadores> lo = this.xcpQuerySession.executeQueryList(this.getEjbVars(), qo);

				if (!Util.isEmpty(lo)) {

					SecureRandom random = new SecureRandom();
					String newPass = new BigInteger(60, random).toString(32);

					String md5 = Util.getMD5(newPass);

					Operadores operadores = lo.get(0);
					XcpEjbVars ejbVars = this.getEjbVars();
					if (ejbVars.getCodEmpresa() == null && operadores.getEmpresa() != null) {
						HttpServletRequest req = getRequest();
						Sessoes sessao = new Sessoes();
						sessao.setInicio(new Date());
						sessao.setIpv4(req.getRemoteAddr());
						sessao.setOperador(operadores.getOperador());
						sessao.setPorta(req.getLocalPort());
						sessao.setServidor(req.getLocalAddr());
						sessao.setTipo(Sessoes.TIPO_2_ENCERRADA);
						sessao.setTermino(new Date());
						sessao.setEmpresa(operadores.getEmpresa());
						sessao = this.persist.update(this.getEjbVars(), sessao);

						ejbVars.getSystemVars().put(XcpEjbVars.EMPRESA, operadores.getEmpresa());
						ejbVars.getSystemVars().put(XcpEjbVars.OPERADOR, operadores.getOperador());
						ejbVars.getSystemVars().put(XcpEjbVars.ROTINA, Rotinas.XCP_ESQUECI_SENHA_965);
						ejbVars.getSystemVars().put(XcpEjbVars.SESSAO, sessao.getSessao());
					}

					this.operadoresSession.gravaAlteracaoSenha(ejbVars, operadores, md5, true);

					XcpSendEmailVO email = new XcpSendEmailVO(codServidor, numTexto);
					email.addValorSubstituir("senha", newPass);
					email.addDestinatario(func.getEmail());

					this.serverEmailSession.sendMail(ejbVars, email);

					addFacesMessage(this.getTraducao("msg_esq_senha_email"), MSG_INFO);

					getBacking(XcpCaptchaBacking.class).actionTrocarCaptcha("esquecisenha");

					return PG_LOGIN;
				} else {
					addFacesMessage(this.getTraducao("msg_esq_senha_dados_nao_conferem"), MSG_ERROR);
					refresh = true;
				}
			}
		} else {
			addFacesMessage(this.getTraducao("msg_esq_senha_dados_nao_conferem"), MSG_ERROR);
			refresh = true;
		}

		if (refresh) {
			this.setMatricula(null);
			this.setCpf(null);
			this.setIdentidade(null);
			this.setDtnascimento(null);
			this.setDesCaptcha(null);
			XcpCaptchaBacking back = getBacking(XcpCaptchaBacking.class);
			back.actionTrocarCaptcha("esquecisenha");
		}
		return null;
	}

	public String getDesCaptcha() {
		return this.desCaptcha;
	}

	public void setDesCaptcha(String desCaptcha) {
		this.desCaptcha = desCaptcha;
	}
}
