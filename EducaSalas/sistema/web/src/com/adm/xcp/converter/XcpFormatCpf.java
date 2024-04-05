package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "xcpFormatCpf")
public class XcpFormatCpf extends XcpConvertInputMask {

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object valor) {
		if (valor != null) {
			StringBuilder sb = new StringBuilder();
			String cpf = String.format("%011d", com.adm.util.Converter.toLong(valor));
			sb.append(cpf.substring(0, 3));
			sb.append(".");
			sb.append(cpf.substring(3, 6));
			sb.append(".");
			sb.append(cpf.substring(6, 9));
			sb.append("-");
			sb.append(cpf.substring(9, 11));
			return sb.toString();
		}
		return "";
	}
}