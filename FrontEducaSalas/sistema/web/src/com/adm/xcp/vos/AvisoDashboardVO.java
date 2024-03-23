package com.adm.xcp.vos;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AvisoDashboardVO implements Serializable {

	private Integer codigoRotina;
	private String destaqueAviso;
	private String conteudoAviso;
	private String msgListener;

	public AvisoDashboardVO(Integer codigoRotina, String destaqueAviso, String conteudoAviso) {
		super();
		this.codigoRotina = codigoRotina;
		this.destaqueAviso = destaqueAviso;
		this.conteudoAviso = conteudoAviso;
	}

	public Integer getCodigoRotina() {
		return this.codigoRotina;
	}

	public void setCodigoRotina(Integer codigoRotina) {
		this.codigoRotina = codigoRotina;
	}

	public String getDestaqueAviso() {
		return this.destaqueAviso;
	}

	public void setDestaqueAviso(String destaqueAviso) {
		this.destaqueAviso = destaqueAviso;
	}

	public String getConteudoAviso() {
		return this.conteudoAviso;
	}

	public void setConteudoAviso(String conteudoAviso) {
		this.conteudoAviso = conteudoAviso;
	}

	public String getMsgListener() {
		return this.msgListener;
	}

	public void setMsgListener(String msgListener) {
		this.msgListener = msgListener;
	}
}
