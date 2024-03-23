package com.adm.xcp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.adm.util.Formatter;
import com.adm.util.Util;

@FacesValidator("xcpPISValidator")
public class PISValidator extends ValidatorFunctions implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String valor = Formatter.limpaCaracteres(value + "");
		if (!Util.isEmpty(valor)) {
			if (valor.length() < 10) {
				String mensagem = "PIS Invalido";
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
		}
	}
}
