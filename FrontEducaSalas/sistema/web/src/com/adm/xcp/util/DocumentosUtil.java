package com.adm.xcp.util;

import com.adm.ejb.entity.Tipodocumento;

public class DocumentosUtil {
	public static boolean limpaCampos(Tipodocumento tipoDocumentoAnt, Tipodocumento tipoDocumentoNovo) {
		if (tipoDocumentoAnt == null || tipoDocumentoNovo == null) {
			return true;
		}
		if (Tipodocumento.TIPOPROCESSO_1_GERADO.equals(tipoDocumentoAnt.getTipoprocesso())
				|| Tipodocumento.TIPOPROCESSO_1_GERADO.equals(tipoDocumentoNovo.getTipoprocesso())) {
			return true;
		}
		return false;
	}
}
