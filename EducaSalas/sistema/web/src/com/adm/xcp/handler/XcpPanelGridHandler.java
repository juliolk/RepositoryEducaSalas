package com.adm.xcp.handler;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;

import com.adm.xcp.util.TagHandlerUtils;

public class XcpPanelGridHandler extends ComponentHandler {

	private final TagAttribute _styleClass;

	public XcpPanelGridHandler(ComponentConfig config) {
		super(config);
		this._styleClass = this.getAttribute("styleClass");
	}

	@Override
	public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
		super.onComponentCreated(ctx, c, parent);
		if (this._styleClass != null) {
			c.setValueExpression("styleClass", TagHandlerUtils.createValueExp(ctx, this._styleClass.getValue() + " xcp", Object.class));
		} else {
			c.setValueExpression("styleClass", TagHandlerUtils.createValueExp(ctx, "xcp", Object.class));
		}
	}

}