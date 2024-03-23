package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.adm.ejb.entity.Sessoes;
import com.adm.ejb.session.remote.XcpPersistSession;

@ApplicationScoped
@ManagedBean
public class XcpApplicationBacking extends XcpAbstractBacking {

	@EJB
	private XcpPersistSession xcpPersistSession;

	private List<Sessoes> usuariosLogados;

	private Sessoes usuarioDeslogar;

	public XcpApplicationBacking() {
		this.usuariosLogados = new ArrayList<Sessoes>();
	}

	public List<Sessoes> getUsuariosLogados() {
		return this.usuariosLogados;
	}

	public Sessoes getUsuarioDeslogar() {
		return this.usuarioDeslogar;
	}

	public void setUsuarioDeslogar(Sessoes usuarioDeslogar) {
		this.usuarioDeslogar = usuarioDeslogar;
	}

	// Metodo utilizado para finalizar a sessão quando for pelo listner de Sessão.
	// repensar esse mecanismo
	public void finalizarSessao(Sessoes sessao) {
		this.getUsuariosLogados().remove(sessao);
	}
}
