package com.adm.xcp.handler;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.outputpanel.OutputPanel;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectoneradio.SelectOneRadio;

import com.adm.ejb.entity.VinculosCamposcad;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.EJBLookup;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.util.XcpVinculosCamposCache;

@FacesComponent("com.xcape.component.XcpPanelVinculoEval")
public class XcpPanelVinculoEval extends UIComponentBase {

	private static final String VISIBILITY_HIDDEN = "xcp_custom:true;visibility:hidden";
	private static final String FLAG = "XcpPanelVinculoEvalFlag";
	private List<VinculosCamposcad> list;

	private enum PropertyKeys {
		value;
	}

	public XcpPanelVinculoEval() {
		this.setRendererType(null);
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		Integer vinculo = Converter.toInteger(this.getValue());
		if (vinculo == null) {
			return;
		}

		XcpVinculosCamposCache instance = XcpVinculosCamposCache.getInstance();
		if (!instance.isLoaded()) {
			synchronized (XcpPanelVinculoEval.class) {
				XcpQuerySession q = (XcpQuerySession) EJBLookup.getInstance().get(XcpQuerySession.JNDI_NAME);
				instance.load(q.executeQueryList(null, new MontaQuery("Select e from VinculosCamposcad e ")));
			}
		}

		this.processCleanState(this);

		this.list = XcpVinculosCamposCache.getInstance().get(vinculo);
		if (Util.isEmpty(this.list)) {
			return;
		}

		this.processApplyState(this);
	}

	private VinculosCamposcad getCampo(String desId) {

		for (VinculosCamposcad v : this.list) {
			if (Objects.equals(v.getId(), desId)) {
				return v;
			}
		}

		return null;
	}

	private void processCleanState(UIComponent root) {
		for (UIComponent c : root.getChildren()) {
			if (c instanceof UIInput || c instanceof UICommand || c instanceof OutputPanel) {
				Boolean curState = (Boolean) c.getAttributes().get("disabled");
				Boolean curFlag = (Boolean) c.getAttributes().get(FLAG);
				if (curState == null || Boolean.TRUE.equals(curFlag)) {
					c.getAttributes().remove(FLAG);
					if (c instanceof Calendar) {
						c.getAttributes().put("readonly", false);
						c.getAttributes().put("showOn", "both");
						c.getAttributes().put("required", false);
					} else if (c instanceof SelectOneMenu || c instanceof SelectOneRadio) {
						c.getAttributes().put("disabled", false);
						c.getAttributes().put("required", false);
					} else if (c instanceof UIInput) {
						c.getAttributes().put("readonly", false);
						c.getAttributes().put("required", false);
					} else if (!(c instanceof OutputPanel)) {
						c.getAttributes().put("disabled", false);
					}

					String curstyle = (String) c.getAttributes().get("style");
					if (curstyle != null) {
						if (curstyle.contains(VISIBILITY_HIDDEN)) {
							curstyle = curstyle.replaceFirst(VISIBILITY_HIDDEN, "");
							c.getAttributes().put("style", curstyle);
						}
					}
				}
			}

			if (c.getChildCount() > 0) {
				this.processCleanState(c);
			}
		}
	}

	private void processApplyState(UIComponent root) {

		for (UIComponent c : root.getChildren()) {
			if (c instanceof UIInput || c instanceof UICommand || c instanceof OutputPanel) {

				VinculosCamposcad campo = this.getCampo(c.getClientId());

				if (campo != null) {
					c.getAttributes().put(FLAG, true);
					if (Util.in(campo.getSituacao(), VinculosCamposcad.SITUACAO_1_BLOQUEADO, VinculosCamposcad.SITUACAO_2_OCULTADO)) {
						if (c instanceof Calendar) {
							c.getAttributes().put("readonly", true);
							c.getAttributes().put("showOn", "false");
						} else if (c instanceof SelectOneMenu || c instanceof SelectOneRadio) {
							c.getAttributes().put("disabled", true);
						} else if (c instanceof UIInput) {
							c.getAttributes().put("readonly", true);
						} else if (!(c instanceof OutputPanel)) {
							c.getAttributes().put("disabled", true);
						}

						if (VinculosCamposcad.SITUACAO_2_OCULTADO.equals(campo.getSituacao())) {
							String curstyle = (String) c.getAttributes().get("style");
							if (curstyle == null) {
								curstyle = "";
							}
							if (!curstyle.contains(VISIBILITY_HIDDEN)) {
								c.getAttributes().put("style", curstyle + VISIBILITY_HIDDEN);
							}
						}
					} else if (Util.in(campo.getSituacao(), VinculosCamposcad.SITUACAO_3_REQUIRED)) {
						if (c instanceof Calendar || c instanceof SelectOneMenu || c instanceof SelectOneRadio || c instanceof UIInput) {
							c.getAttributes().put("required", true);
						}
					}
				}
			}

			if (c.getChildCount() > 0) {
				this.processApplyState(c);
			}
		}
	}

	@Override
	public String getFamily() {
		// Got to override it but it doesn't get called.
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Object getValue() {
		return this.getStateHelper().eval(PropertyKeys.value, null);
	}

	public void setValue(Object value) {
		this.getStateHelper().put(PropertyKeys.value, value);
	}
}