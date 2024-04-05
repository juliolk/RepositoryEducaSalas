package com.adm.xcp.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("xcpEmailValidator")
public class EmailValidator implements Validator {

	@Override
	public void validate(FacesContext facesContext, UIComponent uIComponent, Object object) throws ValidatorException {
		String strValue = (String) object;
		if (strValue != null && !"".equals(strValue)) {

			String mensagem = "Email inválido";

			String enteredEmail = (String) object;

			if (enteredEmail.contains(" ") || enteredEmail.toLowerCase().contains("ç")) {
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
			// Set the email pattern string
			Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

			// Match the given string with the pattern
			Matcher m = p.matcher(enteredEmail);

			// Check whether match is found
			boolean matchFound = m.matches();

			if (!matchFound) {
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
		}
	}
}
