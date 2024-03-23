package com.adm.xcp.vos;

import java.util.List;

import com.adm.ejb.entity.extend.AuditoriaManut;

public class XcpAuditoriaTableVO {

	private String desLinha;
	private List<AuditoriaManut> lista;

	public XcpAuditoriaTableVO(String desLinha, List lista) {
		this.desLinha = desLinha;
		this.lista = lista;
	}

	public String getDesLinha() {
		return this.desLinha;
	}

	public void setDesLinha(String desLinha) {
		this.desLinha = desLinha;
	}

	public List<AuditoriaManut> getLista() {
		return this.lista;
	}

	public void setLista(List<AuditoriaManut> lista) {
		this.lista = lista;
	}

}
