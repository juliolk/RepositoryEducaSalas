package com.adm.gui.backing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import java.util.List;
import java.util.Collection;

import javax.faces.model.SelectItem;

import com.adm.ejb.entity.extend.SalaManut;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpManutencaoBacking;

@ManagedBean
@ViewScoped
public class SalaBacking extends XcpManutencaoBacking implements Serializable {

	public SalaBacking() {
	}

	@Override
	public SalaManut getEntity() {
		SalaManut entity = (SalaManut) super.getEntity();
		if (entity == null) {
			entity = new SalaManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(getEjbVars(),
				new MontaQuery(SalaManut.class, "nome"));
	}


}
