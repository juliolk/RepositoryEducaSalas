package com.adm.xcp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.adm.util.Formatter;
import com.adm.util.Util;

@FacesValidator("xcpCNHValidator")
public class CNHValidator extends ValidatorFunctions implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String valor = Formatter.limpaCaracteres(value + "");
		if (!Util.isEmpty(valor)) {
			if (valor.length() < 11) {
				String mensagem = "CNH Invalida";
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
		}
	}
}
