package com.adm.xcp.vos;

public class MenuAcessoVO {

	private String descricao;
	private Integer rotina;
	private String componente;
	private String codObjeto;
	private Long codPaginaXcp;

	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getRotina() {
		return this.rotina;
	}

	public void setRotina(Integer rotina) {
		this.rotina = rotina;
	}

	public String getComponente() {
		return this.componente;
	}

	public void setComponente(String componente) {
		this.componente = componente;
	}

	public String getCodObjeto() {
		return this.codObjeto;
	}

	public void setCodObjeto(String codObjeto) {
		this.codObjeto = codObjeto;
	}

	public Long getCodPaginaXcp() {
		return this.codPaginaXcp;
	}

	public void setCodPaginaXcp(Long codPaginaXcp) {
		this.codPaginaXcp = codPaginaXcp;
	}
}
