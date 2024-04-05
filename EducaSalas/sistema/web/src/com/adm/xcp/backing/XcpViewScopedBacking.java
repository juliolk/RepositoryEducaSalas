package com.adm.xcp.backing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.Rotinas;

@ViewScoped
@ManagedBean
public class XcpViewScopedBacking extends XcpAbstractBacking {

	private Rotinas rotina = null;

	private boolean constructXcpExecObjBacking = false;

	@Override
	public Rotinas getRotina() {
		if (this.rotina == null || !getPageName().endsWith(this.rotina.getComponente())) {
			StringBuilder sb = new StringBuilder();
			sb.append("select e from Rotinas as e");
			sb.append(" where e.componente = :desUrl ");

			Map<String, Object> map = new HashMap<String, Object>();
			//Nao se cadastra o caminho completo na tabela de rotinas
			map.put("desUrl", getSimplePageName());

			//Passa null no EJBVArs pois senao entra em loop
			List list = this.xcpQuerySession.executeQueryList(null, sb.toString(), map);
			if (list.isEmpty()) {
				//TODO - pagina nao cadastrada
				this.rotina = new Rotinas();
				this.rotina.setComponente(getPageName());
			} else {
				this.rotina = (Rotinas) list.get(0);
			}
		}
		return this.rotina;
	}

	public void setConstructXcpExecObjBacking(boolean constructXcpExecObjBacking) {
		this.constructXcpExecObjBacking = constructXcpExecObjBacking;
	}

	public boolean isConstructXcpExecObjBacking() {
		return this.constructXcpExecObjBacking;
	}
}
