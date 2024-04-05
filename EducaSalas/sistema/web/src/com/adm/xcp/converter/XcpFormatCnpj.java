package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "xcpFormatCnpj")
public class XcpFormatCnpj extends XcpConvertInputMask {

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object valor) {
		if (valor != null) {
			StringBuilder sb = new StringBuilder();
			String cnpj = String.format("%014d", com.adm.util.Converter.toLong(valor));
			sb.append(cnpj.substring(0, 2));
			sb.append(".");
			sb.append(cnpj.substring(2, 5));
			sb.append(".");
			sb.append(cnpj.substring(5, 8));
			sb.append("/");
			sb.append(cnpj.substring(8, 12));
			sb.append("-");
			sb.append(cnpj.substring(12, 14));
			return sb.toString();
		}
		return "";
	}
}