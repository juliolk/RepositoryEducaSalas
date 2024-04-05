package com.adm.xcp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.model.NativeUploadedFile;

import com.adm.xcp.util.XcpTraducaoCache;

@FacesValidator("xcpInputFileValidator")
public class InputFileValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent comp, Object value) throws ValidatorException {

		if (!(value instanceof NativeUploadedFile)) {
			return;
		}

		if (!(comp instanceof FileUpload)) {
			return;
		}

		FileUpload fUp = (FileUpload) comp;
		
		NativeUploadedFile primeValue = (NativeUploadedFile) value;

		if (fUp.getSizeLimit() != null && fUp.getSizeLimit().compareTo(primeValue.getSize()) < 0) {

			String allowedSize = "";
			
			if(fUp.getSizeLimit().compareTo(102400L) > 0){
				Double sizeInMB = fUp.getSizeLimit().doubleValue() / 1024.0 / 1024.0;
				allowedSize = String.format("%.2f", sizeInMB).concat(" MB");
			} else {
				Double sizeInKB = fUp.getSizeLimit().doubleValue() / 1024.0;
				allowedSize = String.format("%.2f", sizeInKB).concat(" KB");
			}
			
			fUp.setValidatorMessage("O tamanho do arquivo é maior que o permitido de " + allowedSize + ".");
			
			throw new ValidatorException(new FacesMessage(
					FacesMessage.SEVERITY_ERROR,
					"Erro de validação: tamanho de arquivo maior que o permitido.",
					"Erro de validação: o tamanho do arquivo é maior que o permitido de " + allowedSize + "."));
		}		
		
		if (fUp.isRequired()) {
			if (primeValue.getSize() == 0L) {
				//O componente inputfile nao estava validando simplesmente com o required. criado validator para fazer isso.
				fUp.setValidatorMessage(XcpTraducaoCache.getInstance().getKey("msg_upload_validacao", XcpTraducaoCache.getInstance().getIdiomaDefault().getCodigo(), null));
				
				throw new ValidatorException(new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Erro de validação: o arquivo é necessário.",
						"Erro de validação: o arquivo é necessário."));
			}
		}

	}
}
