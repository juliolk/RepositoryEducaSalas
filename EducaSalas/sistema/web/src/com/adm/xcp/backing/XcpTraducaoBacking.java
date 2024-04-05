package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.extend.XcpTraducaoManut;
import com.adm.ejb.session.XcpServerCacheSessionBean;
import com.adm.util.MontaQuery;
import com.adm.xcp.util.XcpTraducaoCache;

@ManagedBean
@ViewScoped
public class XcpTraducaoBacking extends XcpManutencaoBacking implements Serializable {

	@EJB
	private XcpServerCacheSessionBean cache;

	@Override
	public XcpTraducaoManut getEntity() {
		XcpTraducaoManut entity = (XcpTraducaoManut) super.getEntity();
		if (entity == null) {
			entity = new XcpTraducaoManut();
			entity.setCodIdioma(1);
			this.setEntity(entity);
		}
		return entity;
	}

	@Override
	public List<?> pesquisar() throws Exception {
		return this.xcpQuerySession.executeQueryList(this.getEjbVars(), new MontaQuery(XcpTraducaoManut.class, "desTraducao"));
	}

	@Override
	public boolean gravar() throws Exception {
		XcpTraducaoCache.limpar();
		this.cache.requestCacheClear(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_XCP_TRADUCAO);
		return super.gravar();
	}
}
