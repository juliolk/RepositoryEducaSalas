package com.adm.xcp.vos;

public class XcpImportarRelatorioVO {

	private String codObjeto;
	private String codModelo;
	private Integer temFonte;
	private Long seqModelo;

	public String getCodObjeto() {
		return this.codObjeto;
	}

	public void setCodObjeto(String codObjeto) {
		this.codObjeto = codObjeto;
	}

	public String getCodModelo() {
		return this.codModelo;
	}

	public void setCodModelo(String codModelo) {
		this.codModelo = codModelo;
	}

	public Integer getTemFonte() {
		return this.temFonte;
	}

	public void setTemFonte(Integer temFonte) {
		this.temFonte = temFonte;
	}

	public Long getSeqModelo() {
		return this.seqModelo;
	}

	public void setSeqModelo(Long seqModelo) {
		this.seqModelo = seqModelo;
	}
}
