package com.adm.xcp.handler;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectoneradio.SelectOneRadio;

import com.adm.util.Converter;

@FacesComponent("com.xcape.component.XcpPanelDisable")
public class XcpPanelDisable extends UIComponentBase {

	private enum PropertyKeys {
		disabled;
	}

	public XcpPanelDisable() {
		this.setRendererType(null);
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		boolean toDisable = this.isDisabled();
		this.processDisablePanel(this, toDisable);
	}

	public void processDisablePanel(UIComponent root, boolean toDisable) {

		/*
		 * The key point here is that a child component of <x:disablePanel> may
		 * already be disabled, in which case we don't want to enable it if the
		 * <x:disablePanel disabled= attribute is set to true.
		 */

		for (UIComponent c : root.getChildren()) {
			if (c instanceof UIInput || c instanceof UICommand) {
				if (toDisable) { // <x:disablePanel disabled="true">
					Object strbypass = c.getAttributes().get("bypassPanelDisable");
					if (strbypass != null) {
						Boolean bypass = Converter.toBoolean(strbypass);
						if (Boolean.TRUE.equals(bypass)) {
							continue;
						}
					}

					Boolean curState = (Boolean) c.getAttributes().get("disabled");
					if (curState == null || curState == false) {
						c.getAttributes().put("XcpDisablePanelFlag", true);

						if (c instanceof Calendar) {
							c.getAttributes().put("readonly", true);
							c.getAttributes().put("showOn", "false");
						} else if (c instanceof SelectOneMenu || c instanceof SelectOneRadio) {
							c.getAttributes().put("disabled", true);
						} else if (c instanceof UIInput) {
							c.getAttributes().put("readonly", true);
						} else {
							c.getAttributes().put("disabled", true);
						}
					}
				} else { // <x:disablePanel disabled="false">
					if (c.getAttributes().get("XcpDisablePanelFlag") != null) {
						c.getAttributes().remove("XcpDisablePanelFlag");

						if (c instanceof Calendar) {
							c.getAttributes().put("readonly", false);
							c.getAttributes().put("showOn", "both");
						} else if (c instanceof SelectOneMenu || c instanceof SelectOneRadio) {
							c.getAttributes().put("disabled", false);
						} else if (c instanceof UIInput) {
							c.getAttributes().put("readonly", false);
						} else {
							c.getAttributes().put("disabled", false);
						}

					}
				}
			}

			if (c.getChildCount() > 0) {
				this.processDisablePanel(c, toDisable);
			}
		}

	}

	@Override
	public String getFamily() {
		// Got to override it but it doesn't get called.
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isDisabled() {
		return (boolean) this.getStateHelper().eval(PropertyKeys.disabled, false);
	}

	public void setDisabled(boolean disabled) {
		this.getStateHelper().put(PropertyKeys.disabled, disabled);
	}
}