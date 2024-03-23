package com.adm.xcp.vos;

public class XcpMultiDialogCtrlVO {
	private long uid = -1;
	private String url;
	private String name;

	public XcpMultiDialogCtrlVO() {

	}

	public XcpMultiDialogCtrlVO(long uid, String name, String url) {
		this.uid = uid;
		this.name = name;
		this.url = url;
	}

	public String getId() {
		return "dlgXcpMultiDialogCtrl_" + this.uid;
	}

	public String getWidgetVar() {
		return "w_" + this.getId();
	}

	public String getUrl() {
		return this.url;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		XcpMultiDialogCtrlVO other = (XcpMultiDialogCtrlVO) obj;
		if (this.uid != other.uid) {
			return false;
		}
		return true;
	}

}
