package com.adm.xcp.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.adm.util.Converter;
import com.adm.util.Util;

@FacesValidator("xcpDayTimeValidator")
public class DayTimeValidator implements Validator {

	private static final String MSG = "O valor deve estar entre 00:00 e 23:59";
	private static final String MSG_60 = "Os minutos não podem ser maior que 59";

	@Override
	public void validate(FacesContext facesContext, UIComponent uIComponent, Object object) throws ValidatorException {
		String strValue = (String) object;

		if (strValue == null || Util.isEmpty(strValue)) {
			return;
		}

		String validate = (String) uIComponent.getAttributes().get("validate");
		if (validate == null || "false".equals(validate)) {
			return;
		}

		BigDecimal vlrBig = Converter.toBigDecimal(strValue);
		if ("true".equals(validate)) {
			if (vlrBig.compareTo(BigDecimal.ZERO) < 0 || vlrBig.compareTo(new BigDecimal("24")) >= 0) {
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, MSG, MSG));
			}
		}

		if ("true".equals(validate) || "minutes".equals(validate)) {
			vlrBig = vlrBig.subtract(vlrBig.setScale(0, RoundingMode.FLOOR));

			if (vlrBig.compareTo(BigDecimal.ZERO) < 0 || vlrBig.compareTo(new BigDecimal("0.60")) >= 0) {
				String msg = MSG;
				if ("minutes".equals(validate)) {
					msg = MSG_60;
				}
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
			}
		}

	}
}
