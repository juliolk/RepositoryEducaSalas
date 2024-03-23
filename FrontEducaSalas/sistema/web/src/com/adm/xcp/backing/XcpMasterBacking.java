package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.component.datatable.DataTable;

import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;

@ManagedBean
@ViewScoped
public abstract class XcpMasterBacking extends XcpManutencaoBacking implements Serializable {

	private List listaMaster = null;
	private List<XcapeEntity> listaMasterFiltrados = null;

	private XcapeEntity entityMaster;

	public XcapeEntity getSelectedMasterEntity() {
		return this.getMasterEntity();
	}

	public void setMasterEntity(XcapeEntity entity) {
		this.entityMaster = entity;
	}

	public void setSelectedMasterEntity(XcapeEntity entity) throws CloneNotSupportedException {
		this.entityMaster = entity;
	}

	public List<XcapeEntity> getListaMasterFiltrados() {
		return this.listaMasterFiltrados;
	}

	public void setListaMasterFiltrados(List<XcapeEntity> listaMasterFiltrados) {
		this.listaMasterFiltrados = listaMasterFiltrados;
	}

	public XcapeEntity getMasterEntity() {
		return this.entityMaster;
	}

	public List<XcapeEntity> getListaMaster() throws Exception {
		if (this.listaMaster == null) {
			this.setListaMaster(this.pesquisarMaster());
		}
		return this.listaMaster;
	}

	public void setListaMaster(List<?> listaMaster) {
		this.listaMaster = listaMaster;
	}

	public void actionPesquisarMaster(String id) throws Exception {
		DataTable tab = (DataTable) this.findComponent("tableMaster_" + id);
		if (tab != null) {
			tab.reset();
			tab.setValueExpression("sortBy", null);
		}
		this.setListaMasterFiltrados(null);
		this.setListaMaster(this.pesquisarMaster());
		if (!Util.isEmpty(this.getListaMaster())) {
			this.setMasterEntity(this.getListaMaster().get(0));
			this.onRowMasterSelect();
		}
		this.setModo(MODO_LISTA);
	}

	public void onRowMasterSelect() throws Exception {
		this.setMasterEntity(this.getSelectedMasterEntity());
		if (this.entityMaster == null) {
			this.setLista(null);
			return;
		}
		this.setLista(this.pesquisar());
	}

	public abstract List<?> pesquisarMaster() throws Exception;

	public String getChildRowStyleClass(Object o) throws Exception {
		return "";
	}

}
