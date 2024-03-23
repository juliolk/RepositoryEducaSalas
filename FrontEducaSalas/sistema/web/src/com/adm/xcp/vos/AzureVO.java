package com.adm.xcp.vos;

import com.adm.ejb.entity.Operadores;

public class AzureVO {

	private String msgErro;
	private String compl;
	private Operadores operador;

	public AzureVO() {

	}

	public AzureVO(String msgErro) {
		this.msgErro = msgErro;
	}

	public AzureVO(String msgErro, String compl) {
		this.msgErro = msgErro;
		this.compl = compl;
	}

	public AzureVO(Operadores operador) {
		this.operador = operador;
	}

	public Operadores getOperador() {
		return this.operador;
	}

	public void setOperador(Operadores operador) {
		this.operador = operador;
	}

	public String getMsgErro() {
		return this.msgErro;
	}

	public void setMsgErro(String msgErro) {
		this.msgErro = msgErro;
	}

	public String getCompl() {
		return this.compl;
	}

	public void setCompl(String compl) {
		this.compl = compl;
	}

}
