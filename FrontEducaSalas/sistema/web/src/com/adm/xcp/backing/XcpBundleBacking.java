package com.adm.xcp.backing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "bundle")
@ApplicationScoped
public class XcpBundleBacking extends XcpAbstractBacking implements Map {

	@Override
	public void clear() {
		//Metodo do Map nao usado
	}

	@Override
	public boolean containsKey(Object arg0) {
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		return false;
	}

	@Override
	public Set entrySet() {
		return null;
	}

	@Override
	public Object get(Object key) {
		return super.getTraducao(key.toString());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Set keySet() {
		return null;
	}

	@Override
	public Object put(Object arg0, Object arg1) {
		return null;
	}

	@Override
	public void putAll(Map arg0) {
		//Metodo do Map nao usado
	}

	@Override
	public Object remove(Object arg0) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Collection values() {
		return null;
	}

}
