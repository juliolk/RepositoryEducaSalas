package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.adm.util.Util;

@FacesConverter(value = "convertCep")
public class XcpConvertCep implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String valor) {
		if (Util.isEmpty(valor) || "__.___-___".equals(valor)) {
			return null;
		}
		valor = valor.replace(".", "").replace("-", "");
		return valor;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object valor) {
		String formatado = "00000000" + valor;
		formatado = formatado.substring(formatado.length() - 8, formatado.length());
		StringBuilder sb = new StringBuilder();
		sb.append(formatado.substring(0, 2));
		sb.append(".");
		sb.append(formatado.substring(2, 5));
		sb.append("-");
		sb.append(formatado.substring(5, 8));
		return sb.toString();
	}

}