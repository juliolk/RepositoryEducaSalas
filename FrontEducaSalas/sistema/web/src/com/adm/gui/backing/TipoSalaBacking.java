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

import com.adm.ejb.entity.TipoSala;
import com.adm.ejb.entity.extend.TipoSalaManut;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.backing.XcpManutencaoBacking;

@ManagedBean
@ViewScoped
public class TipoSalaBacking extends XcpManutencaoBacking implements
		Serializable {

	public TipoSalaBacking() {
	}

	@Override
	public TipoSalaManut getEntity() {
		TipoSalaManut entity = (TipoSalaManut) super.getEntity();
		if (entity == null) {
			entity = new TipoSalaManut();
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(getEjbVars(),
				new MontaQuery(TipoSalaManut.class, "descricao"));
	}
	
	@Override
	public boolean gravar() throws Exception {
		return super.gravar();
	}

	public Collection<SelectItem> getItensTipoSala() {
		return this.getItens("tipoSala", TipoSala.TIPO_SALA_1,
				TipoSala.TIPO_SALA_2, TipoSala.TIPO_SALA_3);
	}

}
