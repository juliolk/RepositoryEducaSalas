package com.adm.xcp.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.adm.util.Util;
import com.xcp.creator.XcpCreatorItemComboIntf;

@FacesConverter(value = "xcpConverterComboCreator")
public class XcpConverterComboCreator implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (Util.isEmpty(value)) {
			return null;
		}
		UIComponent itens = component.findComponent(component.getId().replace("_c", "_creatorItens"));
		if (itens instanceof UISelectItems) {
			Long id = com.adm.util.Converter.toLong(value);
			UISelectItems uiItems = (UISelectItems) itens;
			List<XcpCreatorItemComboIntf> l = (List<XcpCreatorItemComboIntf>) uiItems.getValue();
			for (XcpCreatorItemComboIntf it : l) {
				if (it.getId().equals(id)) {
					return it;
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
		if (value instanceof XcpCreatorItemComboIntf) {
			return com.adm.util.Converter.toString(((XcpCreatorItemComboIntf) value).getId());
		} else {
			return "";
		}
	}
}