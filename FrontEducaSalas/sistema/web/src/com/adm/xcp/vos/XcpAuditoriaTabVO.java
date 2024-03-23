package com.adm.xcp.vos;

import java.util.List;

import com.adm.ejb.entity.extend.AuditoriaManut;

public class XcpAuditoriaTabVO {

	private String desTabela;
	private String desTitulo;
	private AuditoriaManut selectedRow;
	private List<XcpAuditoriaTableVO> lista;

	public List<XcpAuditoriaTableVO> getLista() {
		return this.lista;
	}

	public void setLista(List<XcpAuditoriaTableVO> lista) {
		this.lista = lista;
	}

	public AuditoriaManut getSelectedRow() {
		return this.selectedRow;
	}

	public void setSelectedRow(AuditoriaManut selectedRow) {
		this.selectedRow = selectedRow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.desTitulo == null) ? 0 : this.desTitulo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		XcpAuditoriaTabVO other = (XcpAuditoriaTabVO) obj;
		if (this.desTitulo == null) {
			if (other.desTitulo != null)
				return false;
		} else if (!this.desTitulo.equals(other.desTitulo))
			return false;
		return true;
	}

	public String getDesTitulo() {
		return this.desTitulo;
	}

	public void setDesTitulo(String desTitulo) {
		this.desTitulo = desTitulo;
	}

	public String getDesTabela() {
		return this.desTabela;
	}

	public void setDesTabela(String desTabela) {
		this.desTabela = desTabela;
	}

}
