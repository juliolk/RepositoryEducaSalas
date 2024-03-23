package com.adm.xcp.vos;

import java.util.List;

public class XcpAuditEntityVO {
	private String desLabel;
	private List<?> list;
	private boolean load;

	public XcpAuditEntityVO(String desLabel, List<?> list) {
		this.desLabel = desLabel;
		this.list = list;
	}

	public List<?> getList() {
		return this.list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public String getDesLabel() {
		return this.desLabel;
	}

	public void setDesLabel(String desLabel) {
		this.desLabel = desLabel;
	}

	public boolean isLoad() {
		return this.load;
	}

	public void setLoad(boolean load) {
		this.load = load;
	}

}
