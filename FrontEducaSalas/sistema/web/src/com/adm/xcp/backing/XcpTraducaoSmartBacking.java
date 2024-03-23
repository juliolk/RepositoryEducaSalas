package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.entity.Rotinas;
import com.adm.ejb.entity.extend.XcpTraducaoSmart;
import com.adm.ejb.session.XcpServerCacheSessionBean;
import com.adm.ejb.session.remote.XcpPersistSession;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Util;
import com.adm.xcp.util.XcpTraducaoCache;

@ManagedBean
@ViewScoped
public class XcpTraducaoSmartBacking extends XcpAbstractBacking {

	@EJB
	private XcpQuerySession xcpQuerySession;
	@EJB
	private XcpPersistSession xcpPersistSession;
	@EJB
	private XcpServerCacheSessionBean cache;

	private static final Set<String> IGNORE = new HashSet<String>();

	static {
		IGNORE.add("txt_title");
		IGNORE.add("lnkTraducoes");
		IGNORE.add("msg_boas_vindas");
		IGNORE.add("lbl_empresa");
		IGNORE.add("col_codEmpresa");
		IGNORE.add("col_desEmpresa");
		IGNORE.add("desIdioma");
		IGNORE.add("col_desIdioma");
		IGNORE.add("lnk_logout");
		IGNORE.add("title_menu");
		IGNORE.add("opt_selecione");
		IGNORE.add("lnk_header_traducoes");
		IGNORE.add("lnk_header_limparCaches");
	}

	private List<Rotinas> listaRotinas;

	private Map<Rotinas, List<XcpTraducaoSmart>> listaTraducao;
	private Map<Integer, Set<String>> chaves;

	@PostConstruct
	public void postConstruct() {
		try {
			this.chaves = (Map<Integer, Set<String>>) this.getFlash("chaves_traducao");

			this.carregar();
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	private void carregar() {
		this.listaRotinas = new ArrayList<Rotinas>();
		this.listaTraducao = new HashMap<Rotinas, List<XcpTraducaoSmart>>();
		if (this.chaves != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			StringBuilder sb = new StringBuilder();
			sb.append("select e from XcpTraducaoSmart e");
			sb.append(" where e.codIdioma = :codIdioma and (e.numRotina is null or e.numRotina = :numRotina)");
			sb.append("   and e.desChave = :desChave ");
			sb.append(" order by COALESCE (e.numRotina, -1) desc ");

			map.put("codIdioma", COD_IDIOMA);

			for (Entry<Integer, Set<String>> e : this.chaves.entrySet()) {
				if (e.getKey() != null) {
					Rotinas pg = this.xcpQuerySession.find(this.getEjbVars(), Rotinas.class, e.getKey());
					List<XcpTraducaoSmart> listaTraducao = new ArrayList<XcpTraducaoSmart>();
					this.listaTraducao.put(pg, listaTraducao);
					this.listaRotinas.add(pg);
					map.put("numRotina", e.getKey());
					for (String c : e.getValue()) {
						if (!IGNORE.contains(c)) {
							map.put("desChave", c);
							List<XcpTraducaoSmart> traducao = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sb.toString(), map);
							if (Util.isEmpty(traducao)) {
								XcpTraducaoSmart trad = new XcpTraducaoSmart();
								trad.setDesChave(c);
								trad.setEditar(true);
								listaTraducao.add(trad);
							} else {
								listaTraducao.add(traducao.get(0));
							}
						}
					}
				}
			}
		}
	}

	public List<Rotinas> getListaRotinas() {
		return this.listaRotinas;
	}

	public Map<Rotinas, List<XcpTraducaoSmart>> getListaTraducao() {
		return this.listaTraducao;
	}

	public void actionEditar(XcpTraducaoSmart traducao) {
		traducao.setEditar(true);
		if (traducao.getNumRotina() == null) {
			traducao.setIndRotina(true);
		}
	}

	public void actionGravar() {
		boolean atualizar = false;
		try {
			for (Entry<Rotinas, List<XcpTraducaoSmart>> e : this.listaTraducao.entrySet()) {
				for (XcpTraducaoSmart t : e.getValue()) {
					if (t.isEditar() && !Util.isEmpty(t.getDesTraducao())) {
						t.setCodIdioma(COD_IDIOMA);
						if (t.isIndRotina()) {
							if (t.getNumRotina() == null) {
								t.setNumSeqTraducao(null);
							}
							t.setNumRotina(e.getKey().getRotina());
						} else {
							if (t.getNumRotina() != null) {
								t.setNumSeqTraducao(null);
							}
							t.setNumRotina(null);
						}
						this.xcpPersistSession.update(this.getEjbVars(), t);
						atualizar = true;
					}
				}
			}

			if (atualizar) {
				this.cache.requestCacheClear(this.getEjbVars(), XcpServerCacheSessionBean.CACHE_XCP_TRADUCAO);
			}

			addFacesMessage(super.getTraducao("msg_gravado_ok"), MSG_INFO);
			this.carregar();
			XcpTraducaoCache.limpar();
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}
}
