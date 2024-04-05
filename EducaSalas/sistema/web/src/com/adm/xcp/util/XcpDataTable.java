package com.adm.xcp.util;

import java.util.HashMap;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.primefaces.component.datatable.DataTable;

/**
 * Extending PF data table to allow for binding of the filter map.
 */
public class XcpDataTable extends DataTable {

	protected ValueExpression getFilterFacetValueExpression() {
		ValueExpression ve = getValueExpression("filterMap");
		return ve;
	}

	@Override
	public Map<String, Object> getFilters() {
		ValueExpression ve = getFilterFacetValueExpression();
		if (ve == null)
			return super.getFilters();

		Map<String, Object> map = (Map<String, Object>) ve.getValue(FacesContext.getCurrentInstance().getELContext());
		//LOG.trace("Facet found and map is {}", map);
		if (map == null)
			return new HashMap<String, Object>();
		else
			return map;
	}

	@Override
	public void setFilters(Map<String, Object> filters) {
		ValueExpression ve = getFilterFacetValueExpression();
		if (ve == null) {
			super.setFilters(filters);
			return;
		}

		ve.setValue(FacesContext.getCurrentInstance().getELContext(), filters);
	}

}