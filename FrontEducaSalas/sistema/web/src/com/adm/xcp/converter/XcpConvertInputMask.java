package com.adm.xcp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.adm.util.Util;

public abstract class XcpConvertInputMask implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String valor) {
		if (Util.isEmpty(valor)) {
			return null;
		}
		valor = valor.toString().replaceAll("[- /.]", "");
		valor = valor.toString().replaceAll("[-()]", "");
		return valor;
	}

}