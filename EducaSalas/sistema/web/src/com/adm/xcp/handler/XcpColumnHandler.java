package com.adm.xcp.handler;

import java.math.BigDecimal;
import java.util.Locale;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;

import org.primefaces.component.column.Column;

import com.adm.xcp.util.TagHandlerUtils;

public class XcpColumnHandler extends ComponentHandler {

	private final TagAttribute _value;
	private final TagAttribute _itens;
	private final TagAttribute _size;
	private final TagAttribute _type;

	public XcpColumnHandler(ComponentConfig config) {
		super(config);
		this._value = this.getAttribute("value");
		this._itens = this.getAttribute("itens");
		this._size = this.getAttribute("size");
		this._type = this.getAttribute("type");
	}

	@Override
	public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
		super.onComponentCreated(ctx, c, parent);
		Column col = ((Column) c);

		if (this._value != null) {
			ValueExpression valExp = this._value.getValueExpression(ctx, Object.class);
			c.setValueExpression("sortBy", valExp);
			c.setValueExpression("filterBy", valExp);
			if ("exact".equals(col.getFilterMatchMode())) {
				col.setFilterMatchMode("exact");
			} else {
				col.setFilterMatchMode("contains");
				if (this._itens == null) {
					col.setFilterFunction(TagHandlerUtils.createMethodExp(
							ctx,
							"#{viewUtil.filterXcpColumn}",
							new Class[] { Object.class, Object.class, Locale.class }));
				}
			}
		}

		if (this._size != null) {
			BigDecimal tam = new BigDecimal(this._size.getValue());
			//StringBuilder sp = new StringBuilder();
			//sp.append("width:");
			tam = tam.multiply(new BigDecimal(7));
			//sp.append(tam.toString());
			//sp.append("px;");
			//col.setFilterStyle(sp.toString());
			col.setWidth(tam.toString());
		}

		if (this._type != null) {

			//			StringBuilder estilo = new StringBuilder();
			//			estilo.append("vertical-align: top;");
			//
			//			if (this._type.getValue().equals("Number")) {
			//				estilo.append("text-align: right;");
			//			} else if (this._type.getValue().equals("Date")) {
			//				estilo.append("text-align: center;");
			//			} else if (this._type.getValue().equals("Center")) {
			//				estilo.append("text-align: center;");
			//			} else {
			//				estilo.append("text-align: left;");
			//			}
			//col.setStyle(estilo.toString());
			col.setStyleClass("xcp_column_" + this._type.getValue());
			col.setFilterStyleClass("xcp_filter" + this._type.getValue());
		}

		if (this._itens != null) {
			col.setFilterMatchMode("exact");
			c.setValueExpression("filterOptions", this._itens.getValueExpression(ctx, Object.class));
		}

	}
}