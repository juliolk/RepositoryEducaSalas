package com.adm.xcp.handler;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;

import org.primefaces.component.commandlink.CommandLink;
import org.primefaces.component.tooltip.Tooltip;

import com.adm.xcp.backing.XcpViewUtilBacking;
import com.adm.xcp.util.TagHandlerUtils;

public class XcpCommandLinkHandler extends ComponentHandler {

	private final TagAttribute _id;
	private final TagAttribute _onstart;
	private final TagAttribute _oncomplete;
	private final TagAttribute _showStatus;

	public XcpCommandLinkHandler(ComponentConfig config) {
		super(config);
		this._id = this.getAttribute("id");
		this._onstart = this.getAttribute("onstart");
		this._oncomplete = this.getAttribute("oncomplete");
		this._showStatus = this.getAttribute("showStatus");
	}

	@Override
	public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
		super.onComponentCreated(ctx, c, parent);
		CommandLink btn = ((CommandLink) c);
		if (this._id != null) {
			String id = this._id.getValue(ctx);

			Boolean bloqueado = XcpViewUtilBacking.getInstance().isBloqueado(id);
			if (Boolean.TRUE.equals(bloqueado)) {
				btn.setDisabled(true);
			}

			UIComponent cTooltip = btn.findComponent(c.getId() + "_tooltip");
			if (cTooltip == null) {
				Tooltip tooltip = new Tooltip();
				tooltip.setId(c.getId() + "_tooltip");
				tooltip.setHideEvent("click mouseout");
				tooltip.setValueExpression("value", TagHandlerUtils.createValueExp(ctx, String.format("#{toolTip['%s']}", id), String.class));
				tooltip.setValueExpression("rendered", TagHandlerUtils.createValueExp(ctx, String.format("#{toolTip['%s']!=null}", id), Boolean.class));
				tooltip.setFor(id);
				btn.getChildren().add(tooltip);
			} else {
				btn.getChildren().remove(cTooltip);
				btn.getChildren().add(cTooltip);
			}
		}
		boolean showStatus = true;
		if (this._showStatus != null && Boolean.FALSE.equals(this._showStatus.getBoolean(ctx))) {
			showStatus = false;
		}

		if (showStatus) {
			if (this._onstart != null) {
				btn.setValueExpression(
						"onstart",
						TagHandlerUtils.createValueExp(ctx, this._onstart.getValue() + " ; PF('w_dlgXcpExecStatus').show();", Object.class));
			} else {
				btn.setOnstart("PF('w_dlgXcpExecStatus').show();");
			}

			if (this._oncomplete != null) {
				btn.setValueExpression(
						"oncomplete",
						TagHandlerUtils.createValueExp(ctx, this._oncomplete.getValue() + " ; PF('w_dlgXcpExecStatus').hide();", Object.class));
			} else {
				btn.setOncomplete("PF('w_dlgXcpExecStatus').hide();");
			}
		}
	}
}