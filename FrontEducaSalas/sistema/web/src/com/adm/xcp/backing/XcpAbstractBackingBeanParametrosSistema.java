package com.adm.xcp.backing;

import java.math.BigDecimal;
import java.util.Date;

import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.util.exceptions.ParametroNaoEncontradoException;

public abstract class XcpAbstractBackingBeanParametrosSistema extends XcpAbstractBackingBase {

	private XcpParametrosSistema getParametro(Integer codEmpresa, String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		return this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), codEmpresa, codParametro, numParametro);
	}

	protected Date getParametroDate(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(this.getCodEmpresa(), codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getDtaParametro() != null) {
			return XcpParametrosSistema.getDtaParametro();
		}
		return null;
	}

	protected BigDecimal getParametroBigDecimal(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(this.getCodEmpresa(), codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getVlrParametro() != null) {
			return XcpParametrosSistema.getVlrParametro();
		}
		return null;
	}

	protected Long getParametroLong(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(this.getCodEmpresa(), codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getVlrParametro() != null) {
			return XcpParametrosSistema.getVlrParametro().longValue();
		}
		return null;
	}

	protected String getParametroString(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(this.getCodEmpresa(), codParametro, numParametro);
		if (XcpParametrosSistema != null) {
			return XcpParametrosSistema.getDesParametro();
		}
		return null;
	}

	protected String getParametroGlobalString(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(null, codParametro, numParametro);
		if (XcpParametrosSistema != null) {
			return XcpParametrosSistema.getDesParametro();
		}
		return null;
	}

	protected Long getParametroGlobalLong(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(null, codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getVlrParametro() != null) {
			return XcpParametrosSistema.getVlrParametro().longValue();
		}
		return null;
	}

	protected BigDecimal getParametroGlobalBigDecimal(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(null, codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getVlrParametro() != null) {
			return XcpParametrosSistema.getVlrParametro();
		}
		return null;
	}

	protected Date getParametroGlobalDate(String codParametro, Integer numParametro) throws ParametroNaoEncontradoException {
		XcpParametrosSistema XcpParametrosSistema = this.getParametro(null, codParametro, numParametro);
		if (XcpParametrosSistema != null && XcpParametrosSistema.getDtaParametro() != null) {
			return XcpParametrosSistema.getDtaParametro();
		}
		return null;
	}
}