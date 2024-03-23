package com.adm.xcp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.adm.util.Formatter;
import com.adm.util.Util;

@FacesValidator("xcpCPFValidator")
public class CPFValidator extends ValidatorFunctions implements Validator {

	private static final int[] pesoCPF = { 11, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String valor = Formatter.limpaCaracteres(value + "");
		if (!Util.isEmpty(valor)) {
			if (!isValidCPF(valor)) {
				String mensagem = "CPF Inválido";
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
			}
		}
	}

	public static boolean isValidCPF(String cpf) {
		if ((cpf == null) || (cpf.length() != 11)) {
			return false;
		}

		Integer digito1 = calcularDigitoCPFCNPJ(cpf.substring(0, 9), pesoCPF);
		Integer digito2 = calcularDigitoCPFCNPJ(cpf.substring(0, 9) + digito1, pesoCPF);
		return cpf.equals(cpf.substring(0, 9) + digito1.toString() + digito2.toString());
	}
}
