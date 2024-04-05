package com.adm.xcp.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adm.ejb.entity.VinculosCamposcad;

public class XcpVinculosCamposCache {

	private static XcpVinculosCamposCache instance;
	private boolean loaded = false;
	private Map<Integer, List<VinculosCamposcad>> map = new HashMap<Integer, List<VinculosCamposcad>>();

	private Date lastsearch;

	public boolean isLoaded() {
		//So no portal
		String tipoAcesso = System.getProperty("com.xcape.TIPO_ACESSO");
		if ("2".equals(tipoAcesso)) {
			if (this.lastsearch != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(this.lastsearch);
				cal.add(Calendar.MINUTE, 15);

				if (new Date().after(cal.getTime())) {
					this.loaded = false;
				}
			}
		}
		return this.loaded;
	}

	public static XcpVinculosCamposCache getInstance() {
		if (instance == null) {
			instance = new XcpVinculosCamposCache();
		}
		return instance;
	}

	//	public synchronized void put(Integer empresa, Long operador, Integer rotina, List<PermissaoAcao> list) {
	//
	//		Map<Long, List<VinculosCamposcad>> map = this.getLista().get(empresa);
	//		if (map == null) {
	//			mapOper = new HashMap<Long, Map<Integer, List<PermissaoAcao>>>();
	//			this.getLista().put(empresa, mapOper);
	//		}
	//
	//		Map<Integer, List<PermissaoAcao>> mapRot = mapOper.get(operador);
	//		if (mapRot == null) {
	//			mapRot = new HashMap<Integer, List<PermissaoAcao>>();
	//			mapOper.put(operador, mapRot);
	//		}
	//
	//		List<PermissaoAcao> permissaoAcao = mapRot.get(rotina);
	//		if (permissaoAcao == null) {
	//			mapRot.put(rotina, list);
	//		}
	//	}

	public void load(List<VinculosCamposcad> lista) {
		this.map = new HashMap<Integer, List<VinculosCamposcad>>();
		for (VinculosCamposcad v : lista) {
			List<VinculosCamposcad> list = this.map.get(v.getVinculo());
			if (list == null) {
				list = new ArrayList<VinculosCamposcad>();
			}
			list.add(v);
			this.map.put(v.getVinculo(), list);
		}
		this.loaded = true;
		this.lastsearch = new Date();
	}

	public List<VinculosCamposcad> get(Integer vinculo) {
		return this.map.get(vinculo);
	}

	public synchronized void clear() {
		this.map.clear();
		this.loaded = false;
	}

}
