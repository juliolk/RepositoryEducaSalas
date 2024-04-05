package com.adm.xcp.backing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.ejb.entity.XcpTipoLista;
import com.adm.ejb.entity.XcpTipoParametro;
import com.adm.ejb.entity.custom.XcpParamSistema;
import com.adm.util.Converter;
import com.adm.util.Util;
import com.adm.util.ejb.XcapePK;
import com.adm.xcp.event.XcpLovEvent;
import com.adm.xcp.view.helper.XcpParametrosSistemaHelper;

@ManagedBean
@ViewScoped
public class XcpParametrosSistemaBacking extends XcpAbstractBacking implements XcpEjbConstants {

	private XcpParametrosSistemaHelper helper = new XcpParametrosSistemaHelper();

	public XcpParametrosSistemaHelper getHelper() {
		return this.helper;
	}

	public void selectCodParametro(XcpLovEvent event) {
		this.carregar((XcpTipoParametro) event.getNewValue());
		getRequestContext().execute("rcupdate()");
	}

	public void carregar(XcpTipoParametro codParametro) {
		try {
			this.getHelper().setCodParametro(codParametro);

			this.getHelper().setListaParametros(null);

			if (codParametro != null) {
				this.getHelper().setListaParametros(this.xcpSession.findXcpListaParametrosSistemas(this.getEjbVars(), codParametro));
			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public void actionGravar() {
		try {
			List<XcpParametrosSistema> lista = new ArrayList<XcpParametrosSistema>();
			XcpParametrosSistema parametro;
			for (XcpParamSistema row : this.getHelper().getListaParametros()) {
				parametro = new XcpParametrosSistema();
				parametro.setSeqParametro(row.getSeqParametro());
				if (XcpTipoParametro.TIP_PARAMETRO_0_EMPRESA.equals(this.getHelper().getCodParametro().getTipParametro())) {
					parametro.setCodEmpresa(this.getCodEmpresa());
				}
				parametro.setCodParametro(row.getCodParametro());
				parametro.setNumParametro(row.getNumParametro());
				if (row.isDate()) {
					parametro.setDtaParametro(row.getDtaParametro());
				} else if (row.isNumber()) {
					parametro.setVlrParametro(row.getVlrParametro());
				} else if (row.isLov()) {

					if (Util.isEmpty(row.getDesParametro()) && row.getVlrParametro() == null) {
						Object value = null;
						if (row.getLovValue() != null) {
							value = Util.getValue(row.getLovValue(), row.getLovValue().getCode());
						}

						//Grava o codigo na resposta
						if (XcpTipoLista.TIP_CODIGO_1_STRING.equals(row.getCodLovFk().getTipCodigo())) {
							parametro.setDesParametro(Converter.toString(value));
						} else {
							parametro.setVlrParametro(Converter.toBigDecimal(value));
						}
					} else {
						parametro.setDesParametro(row.getDesParametro());
						parametro.setVlrParametro(row.getVlrParametro());
					}

					parametro.setObjPkLov(row.getObjPkLov());
					if (row.getPk() != null) {
						if (row.getPk() instanceof XcapePK) {
							XcapePK xpk = (XcapePK) row.getPk();
							parametro.setDesPk(xpk.getStringKey());
						}
					} else {
						parametro.setDesPk(null);
					}

				} else {
					parametro.setDesParametro(row.getDesParametro());
				}

				if ("ESocial".equals(parametro.getCodParametro()) && parametro.getNumParametro() == 11) {
					if (parametro.getSeqParametro() != null) {
						XcpParametrosSistema parant = this.xcpQuerySession.find(this.getEjbVars(), XcpParametrosSistema.class, parametro.getSeqParametro());
						//esta tentando desativar o esocial
						if (BIG_1.equals(parant.getVlrParametro()) && BIG_0.equals(parametro.getVlrParametro())) {
							Long count = Converter.toLong(this.xcpQuerySession.executeNativeQuerySingle(this.getEjbVars(), "Select count(1) from esoc_alteracao", null));
							if (count > 0L) {
								//Desativação do esocial não é permitida apos o início das transmissões
								addFacesMessage(super.getTraducao("msg_esoc_desat_trans"), MSG_ERROR);
								return;
							}
						}
					}
				} else if ("ESocialEmp".equals(parametro.getCodParametro()) && parametro.getNumParametro() == 3) {
					if (parametro.getSeqParametro() != null) {
						XcpParametrosSistema parant = this.xcpQuerySession.find(this.getEjbVars(), XcpParametrosSistema.class, parametro.getSeqParametro());
						//esta tentando desativar o esocial
						if (BIG_1.equals(parant.getVlrParametro()) && BIG_0.equals(parametro.getVlrParametro())) {
							StringBuilder sb = new StringBuilder();
							sb.append("Select count(1) from esoc_alteracao ");
							sb.append(" where empresa = :empresa ");
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("empresa", parametro.getCodEmpresa());
							Long count = Converter.toLong(this.xcpQuerySession.executeNativeQuerySingle(this.getEjbVars(), sb.toString(), map));
							if (count > 0L) {
								//Desativação do esocial não é permitida apos o início das transmissões
								addFacesMessage(super.getTraducao("msg_esoc_desat_trans"), MSG_ERROR);
								return;
							}
						}
					}
				}

				lista.add(parametro);
			}
			this.xcpSession.updateXcpParametrosSistema(this.getEjbVars(), lista);

			this.getHelper().setCodParametro(null);
			this.getHelper().setListaParametros(null);

			addFacesMessage(super.getTraducao("msg_gravado_ok"), MSG_INFO);
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public void actionLimpar() {
		this.getHelper().setCodParametro(null);
		this.getHelper().setListaParametros(null);
	}

}