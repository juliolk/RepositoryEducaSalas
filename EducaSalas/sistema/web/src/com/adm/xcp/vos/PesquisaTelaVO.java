package com.adm.xcp.vos;

public class PesquisaTelaVO {
	private String desModulo;
	private String desRotina;
	private Integer codRotina;
	private String onclick;

	public String getOnclick() {
		return this.onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public Integer getCodRotina() {
		return this.codRotina;
	}

	public void setCodRotina(Integer codRotina) {
		this.codRotina = codRotina;
	}

	public String getDesModulo() {
		return this.desModulo;
	}

	public void setDesModulo(String desModulo) {
		this.desModulo = desModulo;
	}

	public String getDesRotina() {
		return this.desRotina;
	}

	public void setDesRotina(String desRotina) {
		this.desRotina = desRotina;
	}
}