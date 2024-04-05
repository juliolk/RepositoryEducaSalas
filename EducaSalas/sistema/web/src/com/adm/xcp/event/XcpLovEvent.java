package com.adm.xcp.event;

import javax.faces.component.UIComponent;

public class XcpLovEvent {
	private UIComponent component;
	private Object newValue;
	private boolean cancel;
	private Integer rowindex;

	public XcpLovEvent(Object newValue) {
		this.newValue = newValue;
		this.cancel = false;
	}

	public Object getNewValue() {
		return this.newValue;
	}

	public void setComponent(UIComponent component) {
		this.component = component;
	}

	public UIComponent getComponent() {
		return this.component;
	}

	public void cancel() {
		this.cancel = true;
	}

	public boolean isCancel() {
		return this.cancel;
	}

	public Integer getRowindex() {
		return this.rowindex;
	}

	public void setRowindex(Integer rowindex) {
		this.rowindex = rowindex;
	}

}
