package com.adm.xcp.converter;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "xcpConverterDate")
public class XcpConverterDate implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return com.adm.util.Converter.toDate(value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof Date) {
			return com.adm.util.Converter.toString((Date) value);
		} else {
			return "";
		}
	}

}