package com.adm.xcp.handler;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;

import com.adm.xcp.event.XcpLovEvent;
import com.adm.xcp.util.TagHandlerUtils;

public abstract class XcpLovAbstractHandler extends ComponentHandler {

	private final TagAttribute _query;
	private final TagAttribute _selectionListener;
	private final TagAttribute _params;
	private final TagAttribute _row;
	private final TagAttribute _rowindex;
	protected final TagAttribute _value;
	protected final TagAttribute _update;
	protected final TagAttribute _execOnOpen;

	public XcpLovAbstractHandler(ComponentConfig config) {
		super(config);
		this._query = this.getRequiredAttribute("query");
		this._params = this.getAttribute("params");
		if (!this.isLovButton(config.getTag())) {
			this._value = this.getRequiredAttribute("value");
		} else {
			this._value = null;
		}
		this._selectionListener = this.getAttribute("selectionListener");
		this._update = this.getAttribute("update");
		this._row = this.getAttribute("row");
		this._rowindex = this.getAttribute("rowindex");
		this._execOnOpen = this.getAttribute("execOnOpen");
	}

	protected void setLovAttributes(UIComponent component, FaceletContext ctx) {
		if (this._value != null) {
			component.setValueExpression("lovValue", this._value.getValueExpression(ctx, Object.class));
		} else {
			component.setValueExpression("lovValue", TagHandlerUtils.createValueExp(ctx, "#{null}", Object.class));
		}
		component.setValueExpression("lovQuery", this._query.getValueExpression(ctx, String.class));

		if (this._params != null) {
			component.setValueExpression("lovParams", this._params.getValueExpression(ctx, Map.class));
		}

		if (this._selectionListener != null) {
			Class[] classes = new Class[] { XcpLovEvent.class };

			component.getAttributes().put("lovSelectionListener", this._selectionListener.getMethodExpression(ctx, null, classes));
		}

		if (this._row != null) {
			component.setValueExpression("lovRow", this._row.getValueExpression(ctx, Object.class));
		}

		if (this._rowindex != null) {
			component.setValueExpression("lovRowindex", this._rowindex.getValueExpression(ctx, Integer.class));
		}

		if (this._update != null) {
			component.setValueExpression("lovUpdate", this._update.getValueExpression(ctx, String.class));
		}

		if (this._execOnOpen != null) {
			component.setValueExpression("lovExecOnOpen", this._execOnOpen.getValueExpression(ctx, String.class));
		}
	}

	protected boolean isLovButton(Tag tag) {
		return tag.getLocalName().equals("lovButton");
	}

}