package com.adm.xcp.backing;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpServletRequest;

import com.adm.ejb.entity.Operadoresperfis;
import com.adm.ejb.entity.PermissaoAcao;
import com.adm.ejb.entity.XcpTipoLista;
import com.adm.ejb.entity.custom.XcpObjetosPars;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.ejb.vo.XcpExecObjItemMultiLovVO;
import com.adm.util.Converter;
import com.adm.util.Formatter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.XcpFonetica;
import com.adm.util.ejb.XcapeEntity;
import com.adm.xcp.event.XcpLovEvent;
import com.adm.xcp.util.XcpPermissaoAcaoCache;
import com.adm.xcp.vos.MenuAcessoVO;
import com.xcp.creator.XcpCreatorItemComboIntf;

@RequestScoped
@ManagedBean(name = "viewUtil")
public class XcpViewUtilBacking extends XcpAbstractBacking {

	@EJB
	private XcpQuerySession xcpQuerySession;

	private String rootPath;
	private Integer rotinaAnt = -1;
	private Map<String, Boolean> acessoOperador = null;
	private Map<String, Map<Integer, Boolean>> acessoPerfil = null;

	private static final DecimalFormat DECIMAL_FORMAT_FILTER;
	private static final String COLUMN_WILDCARD = "%";
	private static final String COLUMN_WILDCARD_EXP = "[%]";

	static {
		DECIMAL_FORMAT_FILTER = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
		DECIMAL_FORMAT_FILTER.applyPattern("0.0000");
	}

	public void montaAcessos(Integer empresa, Integer rotina, Long operador) {
		this.acessoOperador = new HashMap<String, Boolean>();
		this.acessoPerfil = new HashMap<String, Map<Integer, Boolean>>();
		if (empresa == null || rotina == null || operador == null) {
			return;
		}

		XcpPermissaoAcaoCache instance = XcpPermissaoAcaoCache.getInstance();
		List<PermissaoAcao> list = instance.get(empresa, operador, rotina);
		if (list == null) {
			MontaQuery q = new MontaQuery("select e from PermissaoAcao e");
			q.addWhereFix("(e.operador is null or e.operador = :operador)");
			q.addWhere("empresa", "=", empresa);
			q.addWhere("rotina", "=", rotina);
			q.putParameter("operador", operador);
			list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			instance.put(empresa, operador, rotina, list);
		}

		for (PermissaoAcao per : list) {
			String acao = per.getAcao();
			Boolean bloqueia = false;
			if (per.getSituacao().equals(INT_0)) {
				bloqueia = true;
			}
			if (per.getPerfil() != null) {
				Integer perfilAcao = per.getPerfil();
				if (this.acessoPerfil.containsKey(acao)) {
					this.acessoPerfil.get(acao).put(perfilAcao, bloqueia);
				} else {
					Map<Integer, Boolean> perfil = new HashMap<Integer, Boolean>();
					perfil.put(perfilAcao, bloqueia);
					this.acessoPerfil.put(acao, perfil);
				}
			} else {
				this.acessoOperador.put(acao, bloqueia);
			}
		}
	}

	public String getRootPath() {
		if (this.rootPath == null) {
			StringBuilder sb = new StringBuilder();
			HttpServletRequest req = getRequest();
			sb.append(req.getScheme());
			sb.append("://");

			String serverName = req.getServerName();
			if (serverName.endsWith(".")) {
				serverName = serverName.substring(0, serverName.length() - 1);
			}
			sb.append(serverName);
			sb.append(":");
			sb.append(req.getServerPort());
			this.rootPath = sb.toString();
		}
		return this.rootPath;
	}

	public static XcpViewUtilBacking getInstance() {
		return (XcpViewUtilBacking) getELResolverObject("viewUtil");
	}

	public Integer getTableRows() {
		return this.getSession().getTableRows();
	}

	public Integer getTableMaxPages() {
		return 5;
	}

	public char getDecimalSeparator() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(getFacesContext().getViewRoot().getLocale());
		return dfs.getDecimalSeparator();
	}

	public char getGroupingSeparator() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(getFacesContext().getViewRoot().getLocale());
		return dfs.getGroupingSeparator();
	}

	public String getLabelSufix() {
		return (XcpLayoutAppBacking.getTipoAcessoStatic() == 2 ? "" : ":");
	}

	public String getPageTitle() {
		MenuAcessoVO menuAcesso = this.getSession().getMenuAcesso(getPageName(), this.getCodObjeto());
		if (menuAcesso != null && menuAcesso.getDescricao() != null) {
			return menuAcesso.getDescricao();
		}

		// Não é possível cadastrar o XcpHome.xhtml pois faria aparecer nas rotinas disponíveis para perfil e não está correto
		return this.getTraducao("title_home");
	}

	public Boolean isBloqueado(String id, String bloqueado) {

		if (!Util.isEmpty(bloqueado)) {
			if (bloqueado.equals("true")) {
				return true;
			}
		}

		if (Util.isEmpty(id)) {
			return false;
		}

		return this.isBloqueado(id);
	}

	public String isBloqueadoShowOn(String id, String bloqueado) {

		if (!Util.isEmpty(bloqueado)) {
			if (bloqueado.equals("true")) {
				return "false";
			}
		}

		Boolean block = this.isBloqueado(id, bloqueado);
		if (block == null || !block) {
			return "both";
		}

		return "false";
	}

	public Boolean isBloqueado(String id) {
		Integer empresa = null;
		try {
			empresa = this.getCodEmpresa();
		} catch (Exception e) {
			return null;
		}

		Long operador = null;
		if (this.getSession() != null) {
			if (this.getSession().getUsuario() != null) {
				if (this.getSession().getUsuario().getOperador() != null) {
					operador = this.getSession().getUsuario().getOperador();
				}
			}
		}
		Integer rotina = this.getRotina().getRotina();
		if (rotina == null) {
			return null;
		}
		if (!this.rotinaAnt.equals(rotina)) {
			this.rotinaAnt = rotina;
			this.montaAcessos(empresa, rotina, operador);
		}
		if (this.acessoOperador == null || this.acessoPerfil == null) {
			return null;
		}
		//System.out.println(id);
		if (this.acessoOperador.containsKey(id)) {
			return this.acessoOperador.get(id);
		}
		if (this.acessoPerfil.containsKey(id)) {
			List<Operadoresperfis> listaPerfis = this.getSession().getListaPerfisOperador();
			if (Util.isEmpty(listaPerfis)) {
				return null;
			}
			Map<Integer, Boolean> acessos = this.acessoPerfil.get(id);
			for (Operadoresperfis perfil : listaPerfis) {
				if (acessos.containsKey(perfil.getPerfil())) {
					return acessos.get(perfil.getPerfil());
				}
			}
		}
		return null;
	}

	public String getCurrentPageName() {
		return getFacesContext().getViewRoot().getViewId();
	}

	public String getHomePageName() {
		return PG_HOME;
	}

	public String getPanelFormColumnClasses(Integer columns, String labelClass, String contentClass) {
		if (columns == null) {
			columns = 2;
		}
		String[] lc = new String[0];
		String[] cc = new String[0];
		if (!Util.isEmpty(labelClass)) {
			lc = labelClass.split(",");
		}
		if (!Util.isEmpty(contentClass)) {
			cc = contentClass.split(",");
		}
		StringBuilder styles = new StringBuilder();
		for (int i = 0; i < columns / 2; i++) {
			if (i > 0) {
				styles.append(",");
			}
			styles.append("form_label");
			if (lc.length > i) {
				styles.append(" ");
				styles.append(lc[i]);
			}
			styles.append(",form_content");
			if (cc.length > i) {
				styles.append(" ");
				styles.append(cc[i]);
			}
		}

		return styles.toString();
	}

	public boolean filterXcpColumn(Object value, Object filter, Locale locale) {

		String strfilter = Converter.toString(filter);
		if (Util.isEmpty(strfilter)) {
			return true;
		}
		if (value == null) {
			return false;
		}

		String filterText = strfilter.toUpperCase();
		filterText = Formatter.removeAccents(filterText);
		String valueText = Converter.toString(value).toUpperCase();

		if (value instanceof Date) {
			valueText = Converter.toStringDateTime((Date) value, true);
		} else if (value instanceof BigDecimal) {
			filterText = filterText.replace(":", ",");
			valueText = DECIMAL_FORMAT_FILTER.format(value);
		} else {
			valueText = Formatter.removeAccents(valueText);
			//Verifica se busca simples ja resolve
			if (this.matchWildcard(valueText, filterText)) {
				return true;
			}

			//Caso contrario, pesquisa por fonetica
			filterText = XcpFonetica.execute(strfilter.toUpperCase());
			valueText = XcpFonetica.execute(Converter.toString(value).toUpperCase());
		}

		//		
		//
		//		String filterText = (filter == null) ? null : filter.toString().trim().toUpperCase();
		//		if (filterText == null || filterText.equals("")) {
		//			return true;
		//		}
		//		filterText = Formatter.removeAccents(filterText);
		//
		//		String valueText = value.toString().toUpperCase();
		//		valueText = Formatter.removeAccents(valueText);
		//
		//		if (value instanceof Date) {
		//			valueText = Converter.toStringDateTime((Date) value, true);
		//		} else if (value instanceof BigDecimal) {
		//			//dois pontos tambem filtra em numeros (as horas sao gravadas em numeros)
		//			filterText = filterText.replace(":", ",");
		//			valueText = DECIMAL_FORMAT_FILTER.format(value);
		//		}

		return this.matchWildcard(valueText, filterText);
	}

	private boolean matchWildcard(String value, String filter) {

		if (value.contains(filter)) {
			return true;
		}

		if (filter.contains(COLUMN_WILDCARD)) {
			String[] split = filter.split(COLUMN_WILDCARD_EXP);

			if (!filter.startsWith(COLUMN_WILDCARD)) {
				if (!value.startsWith(split[0])) {
					return false;
				}
			}

			int fromIndex = 0;
			for (String part : split) {
				int indexOf = value.indexOf(part, fromIndex);
				if (indexOf < 0) {
					return false;
				}

				fromIndex = indexOf;
			}
			return true;
		}
		return false;
	}

	private void actionAddValor(XcpObjetosPars obj) {

		if (obj == null || obj.getLovValue() == null) {
			return;
		}

		XcapeEntity e = obj.getLovValue();
		for (XcpExecObjItemMultiLovVO el : obj.getXcpExecObjItemMultLov()) {
			if (el.getEntity().getPK().equals(e.getPK())) {
				obj.setLovValue(null);
				//se ja tem o elemento, nao adiciona denovo
				return;
			}
		}

		XcpExecObjItemMultiLovVO vo = new XcpExecObjItemMultiLovVO();
		vo.setEntity(e);

		Object value = Util.getValue(e, e.getCode());

		if (XcpTipoLista.TIP_CODIGO_1_STRING.equals(obj.getCodLovFk().getTipCodigo())) {
			vo.setTextValue(Converter.toString(value));
		} else if (XcpTipoLista.TIP_CODIGO_4_DATE.equals(obj.getCodLovFk().getTipCodigo())) {
			vo.setTextValue(Converter.toString(value));
		} else {
			vo.setNumberValue(Converter.toBigDecimal(value));
		}

		StringBuilder sbDesLabel = new StringBuilder();
		sbDesLabel.append(Converter.toString(value));
		sbDesLabel.append(" - ");
		sbDesLabel.append(Converter.toString(Util.getValue(e, e.getDescription())));

		vo.setDesLabel(sbDesLabel.toString());

		obj.getMultiValue().add(vo);
		obj.getXcpExecObjItemMultLov().add(vo);

		long i = 0L;
		for (XcpCreatorItemComboIntf el : obj.getMultiValue()) {
			XcpExecObjItemMultiLovVO mLov = (XcpExecObjItemMultiLovVO) el;
			mLov.setId(i++);
			for (XcpCreatorItemComboIntf el2 : obj.getXcpExecObjItemMultLov()) {
				XcpExecObjItemMultiLovVO mLov2 = (XcpExecObjItemMultiLovVO) el2;
				if (mLov.getEntity().getPK().equals(mLov2.getEntity().getPK())) {
					mLov2.setId(mLov.getId());
					break;
				}
			}
		}

		obj.setLovValue(null);
	}

	public void actionAddValor(XcpLovEvent event) {
		Object object = event.getComponent().getAttributes().get("rowCreatorValue");
		Object newValue = event.getNewValue();
		if (object != null && newValue != null) {
			XcpObjetosPars pars = (XcpObjetosPars) object;
			pars.setLovValue((XcapeEntity) newValue);
			this.actionAddValor(pars);
			event.cancel();
		}
	}

	public void actionLimparSelecao(XcpObjetosPars obj) {
		obj.getXcpExecObjItemMultLov().clear();
	}

}
