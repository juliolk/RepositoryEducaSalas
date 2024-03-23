package com.adm.xcp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adm.ejb.entity.Idiomas;
import com.adm.ejb.entity.XcpTraducao;
import com.adm.util.Util;
import com.adm.xcp.vos.TraducaoVO;

public class XcpTraducaoCache {
	private static XcpTraducaoCache instance;

	private List<Idiomas> listaIdiomas;
	private Idiomas idiomaDefault;
	private boolean carregado = false;
	private final Map<Integer, Map<String, TraducaoVO>> mapa;

	//Variaveis para a TraducaoSmart
	private Integer numRotinaSmart = null;
	private Map<Integer, Set<String>> chavesAcessadasSmart;

	private XcpTraducaoCache() {
		this.mapa = new HashMap<Integer, Map<String, TraducaoVO>>();
		this.listaIdiomas = new ArrayList<Idiomas>();
		this.chavesAcessadasSmart = new HashMap<Integer, Set<String>>();
	}

	public static XcpTraducaoCache getInstance() {
		if (instance == null) {
			instance = new XcpTraducaoCache();
		}
		return instance;
	}

	public boolean isCarregado() {
		return this.carregado;
	}

	public void setCarregado(boolean carregado) {
		this.carregado = carregado;
	}

	public static void limpar() {
		instance = null;
	}

	private String createRotinaKey(XcpTraducao xcp) {
		StringBuilder sb = new StringBuilder();
		sb.append(xcp.getDesChave());
		sb.append("_");
		sb.append(xcp.getNumRotina());
		return sb.toString();
	}

	public void setListaIdiomas(List<Idiomas> listaIdiomas) {
		this.listaIdiomas = listaIdiomas;
	}

	public List<Idiomas> getListaIdiomas() {
		return this.listaIdiomas;
	}

	public void putKey(XcpTraducao xcpTraducao) {

		Map<String, TraducaoVO> map = this.mapa.get(xcpTraducao.getCodIdioma());

		if (map == null) {
			map = new HashMap<String, TraducaoVO>();
			this.mapa.put(xcpTraducao.getCodIdioma(), map);
		}

		TraducaoVO newTraducao = new TraducaoVO(xcpTraducao.getDesChave(), true);
		newTraducao.setDesTraducao(xcpTraducao.getDesTraducao());

		if (xcpTraducao.getNumRotina() != null) {
			String key = this.createRotinaKey(xcpTraducao);
			TraducaoVO traducaoVO = map.get(key);
			if (traducaoVO == null) {
				map.put(key, newTraducao);
			}
			return;
		}

		TraducaoVO traducaoVO = map.get(xcpTraducao.getDesChave());
		if (traducaoVO == null) {
			map.put(xcpTraducao.getDesChave(), newTraducao);
		}
	}

	public String getKey(String desChave, Integer codIdioma, Integer numRotina) {
		if (numRotina != null) {
			//TraducaoSmart
			//Se trocou de Tela(rotina) limpa as chaves
			if (!Util.in(numRotina, this.numRotinaSmart)) {
				this.numRotinaSmart = numRotina;
				this.getChavesAcessadasSmart().clear();
				this.getChavesAcessadasSmart().put(numRotina, new LinkedHashSet<String>());
			}

			Map<Integer, Set<String>> map = this.getChavesAcessadasSmart();
			if (map != null) {
				Set<String> list = map.get(numRotina);
				if (list != null) {
					list.add(desChave);
				}
			}
		}

		XcpTraducao xcpAux = new XcpTraducao();
		xcpAux.setCodIdioma(codIdioma);
		xcpAux.setDesChave(desChave);
		xcpAux.setNumRotina(numRotina);

		Map<String, TraducaoVO> map = this.mapa.get(codIdioma);

		if (map == null) {
			map = new HashMap<String, TraducaoVO>();
			this.mapa.put(codIdioma, map);
		}

		TraducaoVO vo = map.get(this.createRotinaKey(xcpAux));
		if (vo == null) {
			vo = map.get(xcpAux.getDesChave());
		}

		if (vo == null) {
			vo = new TraducaoVO(xcpAux.getDesChave(), false);
		}

		return vo.getDesTraducao();
	}

	public Idiomas getIdiomaDefault() {
		return this.idiomaDefault;
	}

	public void setIdiomaDefault(Idiomas idiomaDefault) {
		this.idiomaDefault = idiomaDefault;
	}

	public Map<Integer, Set<String>> getChavesAcessadasSmart() {
		return this.chavesAcessadasSmart;
	}

}
