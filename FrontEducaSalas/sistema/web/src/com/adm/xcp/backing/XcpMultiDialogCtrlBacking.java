package com.adm.xcp.backing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.adm.xcp.vos.XcpMultiDialogCtrlVO;

@ManagedBean
@ViewScoped
public class XcpMultiDialogCtrlBacking extends XcpAbstractBacking {
	private XcpMultiDialogCtrlVO nextDialog;
	private Map<String, XcpMultiDialogCtrlVO> openedDialogs = new LinkedHashMap<String, XcpMultiDialogCtrlVO>();

	public XcpMultiDialogCtrlBacking() {

	}

	public void actionOpenNewDialog() {
		String url = getExternalContext().getRequestParameterMap().get("xcp_dlg_url");
		String title = getExternalContext().getRequestParameterMap().get("xcp_dlg_title");
		if (url != null && title != null) {
			this.nextDialog = new XcpMultiDialogCtrlVO(System.currentTimeMillis(), title, url);
			this.openedDialogs.put(this.nextDialog.getId(), this.nextDialog);
			getRequestContext().execute(String.format("xcpMultiDlgHandleNewDialog('%s');", this.getNextDialog().getWidgetVar()));
		} else {
			//	TODO Cadastrar mensagem "Parametros não informados."
			addFacesMessage(this.getTraducao("msg_parametros_nao_informados"));
		}
	}

	public XcpMultiDialogCtrlVO getNextDialog() {
		if (this.nextDialog == null) {
			this.nextDialog = new XcpMultiDialogCtrlVO();
		}
		return this.nextDialog;
	}

	public Collection<XcpMultiDialogCtrlVO> getOpenedDialogs() {
		return this.openedDialogs.values();
	}

	public void actionClose() {
		String dlgId = getExternalContext().getRequestParameterMap().get("xcp_dlg_id");
		if (dlgId != null) {
			this.openedDialogs.remove(dlgId);
		}
	}

	public void actionCloseAll() {
		this.openedDialogs.clear();
	}

	public void actionCloseOthers() {
		String dlgId = getExternalContext().getRequestParameterMap().get("xcp_dlg_id");
		if (dlgId != null) {
			XcpMultiDialogCtrlVO dialog = this.openedDialogs.get(dlgId);
			this.openedDialogs.clear();
			this.openedDialogs.put(dlgId, dialog);
		}
	}

}
