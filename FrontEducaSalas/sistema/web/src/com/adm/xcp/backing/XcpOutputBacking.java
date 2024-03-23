package com.adm.xcp.backing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

@ManagedBean
@ApplicationScoped
public class XcpOutputBacking implements Serializable {

	public Map getItensValue() {
		return new HashMap() {
			@Override
			public Object get(final Object itens) {
				return new HashMap() {
					@Override
					public Object get(Object value) {
						if (itens instanceof List) {
							List<SelectItem> list = (List<SelectItem>) itens;
							for (SelectItem i : list) {
								if (i.getValue() != null && i.getValue().equals(value)) {
									return i.getLabel();
								}
							}
						}
						return value;
					}
				};
			}
		};
	}
}