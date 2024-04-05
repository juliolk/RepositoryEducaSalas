package com.adm.xcp.util;

import java.util.List;

import org.primefaces.model.DualListModel;

public class XcpPickListDataModel<T> extends DualListModel<T> {
	//TODO: Henrique ver o motivo para isso não funcionar.
	private List<T> sourceFull;

	public XcpPickListDataModel(List<T> source, List<T> target) {
		this.setSourceFull(source);
		List<T> newSource = XcpPickListUtil.createSource(source, target);
		this.setSource(newSource);
		this.setTarget(target);
	}

	public List<T> getSourceFull() {
		return this.sourceFull;
	}

	private void setSourceFull(List<T> sourceFull) {
		this.sourceFull = sourceFull;
	}

}
