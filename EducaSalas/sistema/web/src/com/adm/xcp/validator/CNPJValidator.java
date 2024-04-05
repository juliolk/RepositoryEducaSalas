package com.adm.xcp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.adm.util.Formatter;
import com.adm.util.Util;

@FacesValidator("xcpCNPJValidator")
public class CNPJValidator extends ValidatorFunctions implements Validator {

	private static final int[] pesoCNPJ = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String valor = Formatter.limpaCaracteres(value + "");
		if (!Util.isEmpty(valor)) {
			if (!isValidCNPJ(valor)) {
				String mensagem = "CNPJ Inválido";
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
		}
	}

	public static boolean isValidCNPJ(String cnpj) {
		if ((cnpj == null) || (cnpj.length() != 14)) {
			return false;
		}

		Integer digito1 = calcularDigitoCPFCNPJ(cnpj.substring(0, 12), pesoCNPJ);
		Integer digito2 = calcularDigitoCPFCNPJ(cnpj.substring(0, 12) + digito1, pesoCNPJ);
		return cnpj.equals(cnpj.substring(0, 12) + digito1.toString() + digito2.toString());
	}

}
