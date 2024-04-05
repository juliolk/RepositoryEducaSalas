package com.adm.xcp.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.orderlist.OrderList;

import com.xcp.creator.XcpOrderListIntf;

@FacesConverter(value = "xcpOrderListConverter")
public class XcpOrderListConverter implements Converter {

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}

		XcpOrderListIntf vo = (XcpOrderListIntf) value;
		return vo.getKey();
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.equals("null")) {
			return null;
		}
		OrderList p = (OrderList) component;

		List<XcpOrderListIntf> list = (List<XcpOrderListIntf>) p.getValue();

		for (XcpOrderListIntf item : list) {
			if (item.getKey().equalsIgnoreCase(value)) {
				return item;
			}
		}

		return null;
	}
}
