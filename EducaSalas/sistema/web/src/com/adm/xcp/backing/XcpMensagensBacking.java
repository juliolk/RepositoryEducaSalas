package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.Mensagens;
import com.adm.ejb.entity.Menus;
import com.adm.util.MontaQuery;
import com.adm.util.ejb.XcapeEntity;

@ManagedBean
@ViewScoped
public class XcpMensagensBacking extends XcpManutencaoBacking implements Serializable {

	private String textoPesquisa;
	private Boolean somenteNaoLidas; 
	private Boolean listaCarregada;
	
	public String getTextoPesquisa() {
		return textoPesquisa;
	}

	public void setTextoPesquisa(String textoPesquisa) {
		this.textoPesquisa = textoPesquisa;
	}

	public Boolean getSomenteNaoLidas() {
		return somenteNaoLidas;
	}

	public void setSomenteNaoLidas(Boolean somenteNaoLidas) {
		this.somenteNaoLidas = somenteNaoLidas;
	}

	public Boolean getListaCarregada() {
		return listaCarregada;
	}

	public void setListaCarregada(Boolean listaCarregada) {
		this.listaCarregada = listaCarregada;
	}

	public XcpMensagensBacking() {
		this.textoPesquisa = null;
		this.somenteNaoLidas = false;
	}

	@Override
	public Mensagens getEntity() {
		Mensagens entity = (Mensagens) super.getEntity();
		if (entity == null) {
			entity = new Mensagens();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List getLista() throws Exception {
		return this.lista;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		List<?> result = null;
		
		this.listaCarregada = false;
		
		if (this.getSession().getUsuario() == null) {
			return null;
		}
		
		if(this.getTipoAcesso().equals(Menus.PERMISSAO_2_PORTAL)){
			result = this.executeQueryMensagensPortal();
			this.setLista(result);
			if(this.getLista().isEmpty()){				
				this.setEntity(null);
			} else {
				this.actionSelecionarMensagem((Mensagens)this.getLista().get(0));
			}
		} else {
			result = this.executeQueryMensagens();			
		}
		
		this.listaCarregada = true;
		return result;
	}

	private List<?> executeQueryMensagens(){
		MontaQuery q = new MontaQuery(Mensagens.class, "CASE WHEN e.dtvisualizacao is null THEN 0 ELSE 1 END ,e.dtgeracao desc");
		q.addWhere("operador", "=", this.getSession().getUsuario().getOperador());			
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
	}
	
	private List<?> executeQueryMensagensPortal(){
		Map<String, Object> parametros = new HashMap<String, Object>();
		StringBuilder sql = new StringBuilder();
		sql.append("select e from Mensagens e where e.operador = :operador ");
		parametros.put("operador", this.getSession().getUsuario().getOperador());
		if(this.textoPesquisa != null){				
			sql.append(" and (e.assunto like :textopesquisa or e.texto like :textopesquisa)");
			parametros.put("textopesquisa", "%" + this.textoPesquisa + "%");
		} 
		if(this.somenteNaoLidas){
			sql.append(" and e.dtlida is null");
		}
		sql.append(" order by e.dtgeracao desc");
		
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), sql.toString(), parametros);
	}

	public void actionAbrir() throws Exception {
		this.setListaFiltrados(null);
		this.setLista(this.pesquisar());
		this.setModo(MODO_LISTA);

		getRequestContext().execute("PF('w_dlgMensagensOperador').show();");
	}

	@Override
	public void selecionaEntidade(XcapeEntity entity) throws Exception {
		super.selecionaEntidade(entity);
		this.setPesquisaNoVoltar(true);

		if (this.getEntity().getDtvisualizacao() == null && this.getEntity().getPK() != null) {
			this.getEntity().setDtvisualizacao(new Date());
			this.xcpPersistSession.update(this.getEjbVars(), this.getEntity());
		}
	}

	public void actionLida(String id) throws Exception {
		this.getEntity().setDtlida(new Date());
		this.xcpPersistSession.update(this.getEjbVars(), this.getEntity());
		this.setEntity(null);
		this.actionPesquisar(id);
	}

	public void actionSelecionarMensagem(Mensagens msg) throws Exception{
		this.setEntity(msg);
		this.selecionaEntidade(msg);
	}
	
	public void actionMarcarMensagemComoLida() throws Exception{
		this.getEntity().setDtlida(new Date());
		this.xcpPersistSession.update(this.getEjbVars(), this.getEntity());
	}
}
