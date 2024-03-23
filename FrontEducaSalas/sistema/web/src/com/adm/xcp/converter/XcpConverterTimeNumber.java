package com.adm.xcp.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.adm.util.Util;

@FacesConverter(value = "xcpConverterTimeNumber")
public class XcpConverterTimeNumber implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (Util.isEmpty(value)) {
			return null;
		}
		//qualquer caracter especial
		value = value.replaceAll("[^\\w-]+", ".");
		//letras
		value = value.replaceAll("[A-Za-z]", "");
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		if (value == null) {
			return null;
		}

		if (value instanceof Number) {
			NumberFormat df = NumberFormat.getInstance();
			df.setMinimumFractionDigits(2);
			df.setRoundingMode(RoundingMode.HALF_UP);
			String format = df.format(value);
			format = format.replace(".", "");
			format = format.replace(",", ":");
			return format;
		}

		return "";

	}

	public static void main(String[] args) throws Exception {

		DecimalFormat df = new DecimalFormat("#0.00");

		System.out.println(df.format(new BigDecimal("12312310.55")));

	}
}