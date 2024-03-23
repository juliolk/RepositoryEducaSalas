package com.adm.xcp.converter;

import java.math.BigDecimal;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.adm.util.Util;

@FacesConverter(value = "xcpConverterBigDecimal")
public class XcpConverterBigDecimal implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (Util.isEmpty(value)) {
			return null;
		}
		BigDecimal vb = new BigDecimal(value);
		return vb;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			BigDecimal vb = (BigDecimal) value;
			String vs = vb.toPlainString();
			return vs;
		} else {
			return "";
		}
	}
}