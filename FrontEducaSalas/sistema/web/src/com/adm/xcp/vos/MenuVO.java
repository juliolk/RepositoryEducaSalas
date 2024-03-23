package com.adm.xcp.vos;

import org.primefaces.model.menu.MenuModel;

public class MenuVO {
	private String desLabel;
	private MenuModel submenu;

	public String getDesLabel() {
		return this.desLabel;
	}

	public void setDesLabel(String desLabel) {
		this.desLabel = desLabel;
	}

	public MenuModel getSubmenu() {
		return this.submenu;
	}

	public void setSubmenu(MenuModel submenu) {
		this.submenu = submenu;
	}
}