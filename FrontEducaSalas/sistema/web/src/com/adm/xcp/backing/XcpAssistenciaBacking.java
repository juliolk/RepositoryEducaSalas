package com.adm.xcp.backing;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import com.adm.ejb.entity.Assistencia;
import com.adm.ejb.entity.Assistenciasocial;
import com.adm.ejb.entity.FuncionariosListaView;
import com.adm.ejb.entity.Operadores;
import com.adm.ejb.entity.pk.FuncionariosPK;
import com.adm.ejb.session.AssistenciaSessionBean;
import com.adm.ejb.session.ResponsaveisSessionBean;
import com.adm.util.MontaQuery;
import com.adm.util.Util;

public abstract class XcpAssistenciaBacking extends XcpFuncionarioVinculoBacking {

	private Integer funcaoOperador;
	private Assistencia assistencia;

	@EJB
	public AssistenciaSessionBean assistSession;

	@EJB
	public ResponsaveisSessionBean setoresSession;

	@Override
	public boolean isDisabledNovo() {

		if (this.assistencia == null || this.assistencia.getAssistencia() == null) {
			return true;
		}

		return super.isDisabledNovo();
	}

	public void initAssistencia(Integer tipo) {
		this.setFuncaoOperador(null);
		Operadores oper = super.getSession().getUsuario();
		if (oper.getMatricula() == null) {
			return;
		}

		MontaQuery q = new MontaQuery("select e.funcao from Funcionarios e");
		q.addWhere("empresa", "=", oper.getEmpresa());
		q.addWhere("matricula", "=", oper.getMatricula());
		this.setFuncaoOperador((Integer) this.xcpQuerySession.executeQuerySingle(this.getEjbVars(), q));
		if (this.getFuncaoOperador() == null) {
			return;
		}

		Map<String, Object> map = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		if (Assistencia.TIP_ASSIST_4_AVALIACOES.equals(tipo)) {
			sb.append("select e from Assistencia e ");
			sb.append("  where e.tipo = :tipo ");
		} else {
			sb.append("select e from Assistencia e ");
			sb.append(" where e.assistencia in (select f.assistencia from Assistenciafuncao f ");
			sb.append("  where f.funcao = :funcao ");
			sb.append("  and f.empresa = :empresa) ");
			sb.append("  and e.tipo = :tipo ");

			map.put("empresa", oper.getEmpresa());
			map.put("funcao", this.getFuncaoOperador());
		}

		map.put("tipo", tipo);

		List<Assistencia> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb.toString(), map);

		if (Util.isEmpty(list)) {
			return;
		}

		this.setAssistencia(list.get(0));
	}

	public List<?> pesquisarAtend(Integer tipo) {
		FuncionariosListaView funcionario = this.getFuncionario();
		if (funcionario == null) {
			return null;
		}
		MontaQuery q = new MontaQuery("select e from AssistenciasocialManut e");
		q.addWhere("empresa", "=", funcionario.getCodEmpresa());
		q.addWhere("matricula", "=", funcionario.getNumMatricula());
		q.addWhere("assistenciaFk.tipo", "=", tipo);

		if (this.getAssistencia().getAssistencia() == null) {
			q.addWhere("assistencia", "=", -1);
		} else {
			q.addWhere("assistencia", "=", this.getAssistencia().getAssistencia());
		}
		q.setOrderBy("dtatendimento desc");
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
	}

	public Map getParamsAssistenciaFuncao() {
		Map p = new HashMap();
		p.put("funcao", this.getFuncaoOperador());
		return p;
	}

	public Integer getFuncaoOperador() {
		return this.funcaoOperador;
	}

	public void setFuncaoOperador(Integer funcaoOperador) {
		this.funcaoOperador = funcaoOperador;
	}

	public Assistencia getAssistencia() {
		if (this.assistencia == null) {
			this.assistencia = new Assistencia();
		}
		return this.assistencia;
	}

	public void setAssistencia(Assistencia assistencia) {
		this.assistencia = assistencia;
	}

	public Collection<SelectItem> getItensSituacao() {
		return this.getItens(
				"sitAssistSoc",
				Assistenciasocial.SITUACAO_1_AGENDADO,
				Assistenciasocial.SITUACAO_2_ATENDIDO,
				Assistenciasocial.SITUACAO_3_FALTOU,
				Assistenciasocial.SITUACAO_4_CANCELOU,
				Assistenciasocial.SITUACAO_5_ATRASADO);
	}

	@Override
	public void actionNovo(String id) throws Exception {
		FuncionariosListaView funcionario = this.getFuncionario();
		if (funcionario == null) {
			return;
		}

		Integer assistencia = this.getAssistencia().getAssistencia();
		if (assistencia != null) {
			FuncionariosPK pk = new FuncionariosPK();
			pk.setMatricula(funcionario.getNumMatricula());
			pk.setEmpresa(funcionario.getCodEmpresa());
			if (!this.assistSession.podeAtender(this.getEjbVars(), pk, assistencia)) {
				addFacesMessage(this.getTraducao("msg_sem_vinculos_atendimento"));
				return;
			}
		}

		super.actionNovo(id);
	}

}