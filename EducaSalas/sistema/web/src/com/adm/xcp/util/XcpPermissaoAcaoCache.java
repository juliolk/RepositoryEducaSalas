package com.adm.xcp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.adm.ejb.entity.PermissaoAcao;

public class XcpPermissaoAcaoCache {

	private static XcpPermissaoAcaoCache instance;

	private Map<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>> lista;

	public static XcpPermissaoAcaoCache getInstance() {
		if (instance == null) {
			instance = new XcpPermissaoAcaoCache();
			instance.setLista(new HashMap<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>>());
		}
		return instance;
	}

	public Map<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>> getLista() {
		return lista;
	}

	public void setLista(Map<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>> lista) {
		this.lista = lista;
	}

	public synchronized void put(Integer empresa, Long operador, Integer rotina, List<PermissaoAcao> list) {

		Map<Long, Map<Integer, List<PermissaoAcao>>> mapOper = this.getLista().get(empresa);
		if (mapOper == null) {
			mapOper = new HashMap<Long, Map<Integer, List<PermissaoAcao>>>();
			this.getLista().put(empresa, mapOper);
		}

		Map<Integer, List<PermissaoAcao>> mapRot = mapOper.get(operador);
		if (mapRot == null) {
			mapRot = new HashMap<Integer, List<PermissaoAcao>>();
			mapOper.put(operador, mapRot);
		}

		List<PermissaoAcao> permissaoAcao = mapRot.get(rotina);
		if (permissaoAcao == null) {
			mapRot.put(rotina, list);
		}
	}

	public List<PermissaoAcao> get(Integer empresa, Long operador, Integer rotina) {

		Map<Long, Map<Integer, List<PermissaoAcao>>> mapOper = this.getLista().get(empresa);
		if (mapOper == null) {
			return null;
		}

		Map<Integer, List<PermissaoAcao>> mapRot = mapOper.get(operador);
		if (mapRot == null) {
			return null;
		}

		return mapRot.get(rotina);
	}

	public synchronized void clear(Long operador) {

		Map<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>> listaEmp = this.getLista();

		for (Entry<Integer, Map<Long, Map<Integer, List<PermissaoAcao>>>> entry : listaEmp.entrySet()) {
			Map<Long, Map<Integer, List<PermissaoAcao>>> mapEmp = entry.getValue();
			if (mapEmp == null) {
				continue;
			}

			Map<Integer, List<PermissaoAcao>> map = mapEmp.get(operador);
			if (map == null) {
				continue;
			}

			map.clear();
		}

	}

}
