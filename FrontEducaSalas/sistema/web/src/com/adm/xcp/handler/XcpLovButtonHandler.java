package com.adm.xcp.handler;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.FaceletContext;

import org.primefaces.component.commandbutton.CommandButton;

import com.adm.xcp.util.TagHandlerUtils;

public class XcpLovButtonHandler extends XcpLovAbstractHandler {

	public XcpLovButtonHandler(ComponentConfig config) {
		super(config);
	}

	@Override
	public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
		super.onComponentCreated(ctx, c, parent);

		CommandButton btn = (CommandButton) c;

		btn.addActionListener(TagHandlerUtils.createActionListener(ctx, "#{xcpLovBacking.actionBuscar}"));

		this.setLovAttributes(btn, ctx);

		btn.setProcess("@this");
	}

}