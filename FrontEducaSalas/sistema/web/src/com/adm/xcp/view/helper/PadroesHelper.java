package com.adm.xcp.view.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adm.ejb.entity.PadroesHistorico;
import com.adm.ejb.entity.PadroesNiveis;

public class PadroesHelper {
	private Set<Date> listaDatas = new LinkedHashSet<Date>();
	private Map<Date, String> listaObs;
	private Map<Date, BigDecimal> listaPerc;
	private List<String> listaClasses = new ArrayList<String>();
	private Map<Date, Map<String, PadroesHistorico>> dados = new HashMap<Date, Map<String, PadroesHistorico>>();
	private boolean mostraGrade;
	private PadroesNiveis padroesNiveis = new PadroesNiveis();
	private Date dataDoPadrao;

	// Define a data do registro que contém o padrão e não o histório das alterações.
	public Date getDataPadrao() {
		if (this.dataDoPadrao == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2200, 12, 30, 0, 0, 0);
			this.dataDoPadrao = cal.getTime();
		}
		return this.dataDoPadrao;
	}

	public Map<Date, String> getListaObs() {
		if (this.listaObs == null) {
			this.listaObs = new HashMap<Date, String>();
		}
		return this.listaObs;
	}

	public void setListaObs(Map<Date, String> listaObs) {
		this.listaObs = listaObs;
	}

	public Map<Date, BigDecimal> getListaPerc() {
		if (this.listaPerc == null) {
			this.listaPerc = new HashMap<Date, BigDecimal>();
		}
		return this.listaPerc;
	}

	public void setListaPerc(Map<Date, BigDecimal> listaPerc) {
		this.listaPerc = listaPerc;
	}

	public Set<Date> getListaDatas() {
		return this.listaDatas;
	}

	public void setListaDatas(Set<Date> listaDatas) {
		this.listaDatas = listaDatas;
	}

	public List<String> getListaClasses() {
		return this.listaClasses;
	}

	public void setListaClasses(List<String> listaClasses) {
		this.listaClasses = listaClasses;
	}

	public Map<Date, Map<String, PadroesHistorico>> getDados() {
		return this.dados;
	}

	public void setDados(Map<Date, Map<String, PadroesHistorico>> dados) {
		this.dados = dados;
	}

	public void setMostraGrade(boolean mostraGrade) {
		this.mostraGrade = mostraGrade;
	}

	public boolean isMostraGrade() {
		return this.mostraGrade;
	}

	public PadroesNiveis getPadroesNiveis() {
		if (this.padroesNiveis == null) {
			this.padroesNiveis = new PadroesNiveis();
		}
		return this.padroesNiveis;
	}

	public void setPadroesNiveis(PadroesNiveis padroesNiveis) {
		this.padroesNiveis = padroesNiveis;
	}
}
