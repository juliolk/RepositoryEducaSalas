package com.adm.xcp.util;

import java.util.ArrayList;
import java.util.List;

public class XcpPickListUtil {

	public static <T> List<T> createSource(List<T> listSource, List<T> listTarget) {
		if (listTarget == null) {
			return listSource;
		}
		if (listTarget.isEmpty()) {
			return listSource;
		}

		List listRetorno = new ArrayList();
		for (Object full : listSource) {
			if (!listTarget.contains(full)) {
				listRetorno.add(full);
			}
		}
		return listRetorno;
	}
}
