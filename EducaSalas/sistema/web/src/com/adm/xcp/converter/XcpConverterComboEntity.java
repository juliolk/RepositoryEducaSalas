package com.adm.xcp.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;

public abstract class XcpConverterComboEntity implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (Util.isEmpty(value)) {
			return null;
		}
		UIComponent itens = component.findComponent(component.getId().replace("_c", "_itens"));
		if (itens instanceof UISelectItems) {
			Object id = value;
			if (this instanceof XcpConverterComboEntityInteger) {
				id = com.adm.util.Converter.toInteger(value);
			} else if (this instanceof XcpConverterComboEntityLong) {
				id = com.adm.util.Converter.toLong(value);
			}
			UISelectItems uiItems = (UISelectItems) itens;
			List<SelectItem> l = (List<SelectItem>) uiItems.getValue();
			for (SelectItem it : l) {
				if (id.equals(((XcapeEntity) it.getValue()).getPK())) {
					return it.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof XcapeEntity) {
			return com.adm.util.Converter.toString(((XcapeEntity) value).getPK());
		} else {
			return "";
		}
	}
}