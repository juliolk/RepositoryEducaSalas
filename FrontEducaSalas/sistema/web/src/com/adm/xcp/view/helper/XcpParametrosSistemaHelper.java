package com.adm.xcp.view.helper;

import java.util.List;

import com.adm.ejb.entity.XcpTipoParametro;
import com.adm.ejb.entity.custom.XcpParamSistema;

public class XcpParametrosSistemaHelper {

	private XcpTipoParametro codParametro;

	private List<XcpParamSistema> listaParametros;

	public XcpTipoParametro getCodParametro() {
		return this.codParametro;
	}

	public void setCodParametro(XcpTipoParametro codParametro) {
		this.codParametro = codParametro;
	}

	public List<XcpParamSistema> getListaParametros() {
		return this.listaParametros;
	}

	public void setListaParametros(List<XcpParamSistema> listaParametros) {
		this.listaParametros = listaParametros;
	}
}
