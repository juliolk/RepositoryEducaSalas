package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adm.util.Formatter;
import com.adm.util.Util;

@FacesConverter(value = "xcpConvertPis")
public class XcpConvertPis extends XcpConvertInputMask {

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object valor) {
		if (valor != null) {
			return String.format("%011d", com.adm.util.Converter.toLong(valor));
		}
		return "";
	}

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String valor) {
		if (Util.isEmpty(valor)) {
			return null;
		}
		valor = Formatter.limpaCaracteres(valor);
		return valor;
	}

}