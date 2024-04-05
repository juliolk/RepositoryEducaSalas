package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

import com.xcp.creator.XcpPickListIntf;

@FacesConverter(value = "xcpPickListConverter")
public class XcpPickListConverter implements Converter {

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}
		XcpPickListIntf vo = (XcpPickListIntf) value;
		return vo.getPickListKey();
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.equals("null")) {
			return null;
		}
		PickList p = (PickList) component;

		DualListModel<XcpPickListIntf> rotinas = (DualListModel<XcpPickListIntf>) p.getValue();

		for (XcpPickListIntf item : rotinas.getSource()) {
			if (item.getPickListKey().equalsIgnoreCase(value)) {
				return item;
			}
		}
		for (XcpPickListIntf item : rotinas.getTarget()) {
			if (item.getPickListKey().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return null;
	}
}
