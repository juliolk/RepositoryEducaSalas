package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.adm.ejb.entity.Turmas;
import com.adm.ejb.entity.extend.TreinamentoManut;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;

public abstract class XcpTreinamentoBacking extends XcpManutencaoBacking {
	private Turmas turmaSelecionada;
	private List<Turmas> listaTurmas = null;

	public List<Turmas> getListaTurmas() {
		if (this.listaTurmas == null) {
			this.listaTurmas = new ArrayList<Turmas>();
		}
		return this.listaTurmas;
	}

	public void setListaTurmas(List<Turmas> listaTurmas) {
		this.listaTurmas = listaTurmas;
	}

	@Override
	public TreinamentoManut getEntity() {
		TreinamentoManut entity = (TreinamentoManut) super.getEntity();
		if (entity == null) {
			entity = new TreinamentoManut();
			this.setEntity(entity);
		}
		return entity;
	}

	public List<Turmas> carregaTurmas(TreinamentoManut treinamento) {
		MontaQuery q = new MontaQuery(Turmas.class, "qtdvagas");
		q.addWhere("empresa", "=", treinamento.getEmpresa());
		q.addWhere("treinamento", "=", treinamento.getTreinamento());
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
	}

	private void carregaListaTurmas(TreinamentoManut treinamento) {
		this.setListaTurmas(this.carregaTurmas(treinamento));
		this.setTurmaSelecionada(null);
		if (!Util.isEmpty(this.getListaTurmas())) {
			this.setTurmaSelecionada(this.getListaTurmas().get(0));
		}
		this.onRowSelectTurma();
	}

	@Override
	public void selecionaEntidade(XcapeEntity entity) throws Exception {
		TreinamentoManut treinamento = (TreinamentoManut) entity;
		this.setListaTurmas(null);
		if (treinamento != null) {
			this.carregaListaTurmas(treinamento);
		}
		super.selecionaEntidade(entity);
	}

	@Override
	public List<?> pesquisar() throws Exception {
		MontaQuery q = new MontaQuery(TreinamentoManut.class, "data desc, descricao");
		q.addWhere("empresa", "=", this.getCodEmpresa());
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
	}

	public abstract void onRowSelectTurma();

	public boolean isValidoIncricao() {
		Date d = Util.trunc(new Date());
		if (this.getEntity().getDtinscini() != null) {
			if (d.compareTo(this.getEntity().getDtinscini()) < 0) {
				//Periodo de inscricoes ainda nao aberto 
				addFacesMessage(this.getTraducao("msg_data_trei_inferior"));
				return false;
			}
		}
		if (this.getEntity().getDtinscfim() != null) {
			if (d.compareTo(this.getEntity().getDtinscfim()) > 0) {
				//Periodo de inscricoes encerrado
				addFacesMessage(this.getTraducao("msg_data_trei_superior"));
				return false;
			}
		}

		return true;
	}

	public Turmas getTurmaSelecionada() {
		return this.turmaSelecionada;
	}

	public void setTurmaSelecionada(Turmas turmaSelecionada) {
		this.turmaSelecionada = turmaSelecionada;
	}
}