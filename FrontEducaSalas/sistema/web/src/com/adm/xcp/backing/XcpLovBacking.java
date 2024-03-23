package com.adm.xcp.backing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import com.adm.ejb.entity.Orgaos;
import com.adm.ejb.entity.XcpTipoLista;
import com.adm.ejb.session.remote.XcpQuerySession;
import com.adm.util.Converter;
import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;
import com.adm.util.exceptions.ParametroNaoEncontradoException;
import com.adm.xcp.event.XcpLovEvent;
import com.adm.xcp.util.TagHandlerUtils;
import com.adm.xcp.vos.MenuAcessoVO;

@ManagedBean
@ViewScoped
public class XcpLovBacking extends XcpAbstractBacking {

	private static final Logger log = Logger.getLogger(XcpLovBacking.class);

	@EJB
	private XcpQuerySession xcpQuerySession;

	private List<XcapeEntity> lista;
	private List<XcapeEntity> listaFiltrados;
	private XcpTipoLista xcpTipoLista;
	private ValueExpression expValueLov;
	private MethodExpression selectionListener;
	private String pesquisa;
	private String update;
	private String lovId;
	private String lovClientId;
	private boolean execOnOpen = false;
	private Map<String, Object> params;
	private String[] campos;
	private String[] titulos;
	private XcapeEntity selectedEntity;
	private Integer lovRowindex;

	private Object rowObject;
	private String rowAttribute;

	private boolean focusOnClose = true;

	public XcpLovBacking() {
	}

	private String showDialogScript() {
		String title = "LOV";
		if (this.xcpTipoLista != null) {
			title = this.getTraducao("title_lov_" + this.xcpTipoLista.getCodLov());
		}
		return " PF('lovDialog_w').show(); 	$('.ui-dialog-title', PF('lovDialog_w').titlebar).text('" + title + "');";
	}

	public void valueChangeCodigo(ValueChangeEvent event) {
		HtmlInputText txt = (HtmlInputText) event.getComponent();

		Object newValue = event.getNewValue();
		Object oldValue = event.getOldValue();
		XcapeEntity value = null;
		boolean achou = false;
		boolean abrir = false;

		boolean newNull = newValue == null || (newValue instanceof String && ((String) newValue).trim().equals(""));
		boolean oldNull = oldValue == null || (oldValue instanceof String && ((String) oldValue).trim().equals(""));

		// Testa se o new e old é  null/vazio
		if (oldNull && newNull) {
			return;
		}

		//Testa se o conteudo é igual..
		if (!oldNull && !newNull) {
			if (oldValue instanceof BigDecimal) {
				BigDecimal newBig = Converter.toBigDecimal(newValue);
				BigDecimal oldBig = (BigDecimal) oldValue;
				if (newBig.compareTo(oldBig) == 0) {
					return;
				}
			} else if (Util.in(newValue, oldValue, Converter.toString(oldValue))) {
				return;
			}
		}

		try {
			this.buscaParametros(event);

			if (newNull) {
				achou = true;
			} else {
				try {
					List<XcapeEntity> result = this.xcpSession.findLov(this.getEjbVars(), this.xcpTipoLista, newValue, true, this.getSystemVars(), this.params);

					if (result.size() == 0) {
						addFacesMessage(this.getTraducao("txt_lov_cod_not_found"));
					} else if (result.size() == 1) {
						value = result.get(0);
						achou = true;
					} else {
						abrir = true;
						//	addFacesMessage(this.getTraducao("txt_lov_cod_many_rows"));
						this.abreDialog(newValue, result);
					}
				} catch (Exception e) {
					String msg = e.getMessage();
					if (msg != null && msg.toUpperCase().startsWith("MSG")) {
						addFacesMessage(this.getTraducao(msg));
					} else {
						addFacesMessage(e.getMessage());
					}
				}

			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		} finally {
			if (!achou) {
				txt.setValue(oldValue);
				if (!abrir) {
					RequestContext.getCurrentInstance().execute(String.format("PrimeFaces.focus('%s')", txt.getClientId(getFacesContext())));
				}
			} else {
				this.atribuiValor(event, value);
			}
			if (!abrir) {
				this.limpar();
			}
		}
	}

	public void valueChangeDescricao(ValueChangeEvent event) {
		HtmlInputText txt = (HtmlInputText) event.getComponent();
		Object newValue = event.getNewValue();
		Object oldValue = event.getOldValue();
		XcapeEntity value = null;
		boolean achou = false;

		boolean newNull = newValue == null || (newValue instanceof String && ((String) newValue).trim().equals(""));
		boolean oldNull = oldValue == null || (oldValue instanceof String && ((String) oldValue).trim().equals(""));

		// Testa se o new e old é  null/vazio
		if (oldNull && newNull) {
			return;
		}

		//Testa se o conteudo é igual..
		if (!oldNull && !newNull) {
			if (oldValue instanceof BigDecimal) {
				BigDecimal newBig = Converter.toBigDecimal(newValue);
				BigDecimal oldBig = (BigDecimal) oldValue;
				if (newBig.compareTo(oldBig) == 0) {
					return;
				}
			} else if (Util.in(newValue, oldValue, Converter.toString(oldValue))) {
				return;
			}
		}

		try {
			this.buscaParametros(event);

			if (newNull) {
				achou = true;
			} else {
				try {
					List<XcapeEntity> result = this.xcpSession.findLov(this.getEjbVars(), this.xcpTipoLista, newValue, false, this.getSystemVars(), this.params);
					if (result.size() == 1) {
						value = result.get(0);
						achou = true;
					} else {
						this.abreDialog(newValue, result);
					}
				} catch (Exception e) {
					//FIXME Aqui era uma MensagemException. Fica desse jeito?
					addFacesMessage(e.getMessage());
				}
			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		} finally {
			if (!achou) {
				txt.setValue(oldValue);
			} else {
				this.atribuiValor(event, value);
				this.limpar();
			}
		}
	}

	private void abreDialog(Object vlrPesquisa, List<XcapeEntity> result) {
		// Abre a dialog
		RequestContext requestContext = getRequestContext();
		requestContext.execute(this.showDialogScript());

		this.focusOnClose = true;

		this.setListaFiltrados(null);
		this.setLista(result);
		this.setPesquisa(Converter.toString(vlrPesquisa));
		this.setSelectedEntity((XcapeEntity) this.expValueLov.getValue(getFacesContext().getELContext()));
		requestContext.update("lovForm:tableLovContent");
		requestContext.update("lovForm:txtLovPesq");
	}

	private void buscaParametros(FacesEvent event) {
		FacesContext facesContext = getFacesContext();

		this.expValueLov = event.getComponent().getValueExpression("lovValue");

		this.lovId = event.getComponent().getParent().getParent().getId();

		this.lovClientId = event.getComponent().getParent().getParent().getClientId(facesContext);

		String lovExecOnOpen = (String) event.getComponent().getAttributes().get("lovExecOnOpen");
		this.execOnOpen = (lovExecOnOpen != null && lovExecOnOpen.trim().equalsIgnoreCase("true"));

		this.update = (String) event.getComponent().getAttributes().get("lovUpdate");

		this.selectionListener = (MethodExpression) event.getComponent().getAttributes().get("lovSelectionListener");

		this.params = (Map<String, Object>) event.getComponent().getAttributes().get("lovParams");

		this.lovRowindex = Converter.toInteger(event.getComponent().getAttributes().get("lovRowindex"));

		String query = (String) event.getComponent().getAttributes().get("lovQuery");
		if (query != null) {
			XcpTipoLista lov = this.xcpQuerySession.find(this.getEjbVars(), XcpTipoLista.class, query);

			if (Util.in(query, (Object[]) Orgaos.LOVS)) {
				try {
					if (LONG_1.equals(this.getParametroLong("Sistema", 15))) {
						lov.setDesCampos(lov.getDesCampos() + ";desZona;desNucleo");
					}
				} catch (ParametroNaoEncontradoException e) {
					//ignora
				}
			}

			if (lov != null) {
				this.setXcpTipoLista(lov);
			} else {
				addFacesMessage("Query invalida");
			}
		}

		//Verifica se esta dentro de um repeat,datalist,table
		this.rowObject = null;
		if (Pattern.compile(":\\d+:").matcher(this.lovClientId).find()) {
			String elExpString = this.expValueLov.getExpressionString();
			Matcher matcher = Pattern.compile("\\.(\\w+)}$").matcher(elExpString);
			if (matcher.find()) {
				this.rowAttribute = matcher.group(1);
				elExpString = elExpString.replaceAll("\\.\\w+}$", "}");
				this.rowObject = TagHandlerUtils.createValueExp(facesContext, elExpString, Object.class).getValue(facesContext.getELContext());
			} else {
				this.rowObject = event.getComponent().getAttributes().get("lovRow");
			}
			if (this.rowObject == null) {
				log.error("A LOV " + this.lovClientId + "ESTA EM UM COMPONENTE 'REPEAT' E NAO FOI POSSIVEL IDENTIFICAR A VARIAVEL, ESPECIFIQUE O ATRIBUTO 'row'");
			}
		}
	}

	private void atribuiValor(FacesEvent event, XcapeEntity newValue) {
		FacesContext ctx = getFacesContext();
		Method setMethod = null;
		Class<?> expType = null;
		UIComponent lovComp = this.findComponent(this.lovClientId.replaceAll(":\\d+:", ":"));
		if (lovComp == null) {
			lovComp = this.findComponent(this.lovId);
		}

		if (this.rowObject == null) {
			expType = this.expValueLov.getType(ctx.getELContext());
		} else {
			try {
				Class rowClass = this.rowObject.getClass();
				String upperCase = this.rowAttribute.substring(0, 1).toUpperCase() + this.rowAttribute.substring(1, this.rowAttribute.length());
				String getName = "get" + upperCase;
				Method getMethod = rowClass.getDeclaredMethod(getName);
				expType = getMethod.getReturnType();
				String setName = "set" + upperCase;
				setMethod = rowClass.getDeclaredMethod(setName, expType);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}

		if (this.selectionListener != null) {
			XcpLovEvent lovEvent = new XcpLovEvent(newValue);
			lovEvent.setComponent(lovComp);
			lovEvent.setRowindex(this.lovRowindex);
			this.selectionListener.invoke(ctx.getELContext(), new Object[] { lovEvent });
			if (lovEvent.isCancel()) {
				newValue = null;
			}
		}
		if (this.rowObject == null) {
			if (!this.expValueLov.isReadOnly(ctx.getELContext())) {
				this.expValueLov.setValue(ctx.getELContext(), newValue);

				UIComponent btnNavegar = lovComp.findComponent(this.lovClientId + "_btn_navegar");
				if (btnNavegar != null) {
					if (newValue != null) {
						btnNavegar.getAttributes().put("entityId", newValue.getPK());
					} else {
						btnNavegar.getAttributes().remove("entityId");
						btnNavegar.getAttributes().remove("entityValueExp");
					}
				}
			}
		} else if (setMethod != null) {
			try {
				setMethod.invoke(this.rowObject, newValue);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		if (lovComp != null) {
			RequestContext requestContext = RequestContext.getCurrentInstance();
			// Tira o indice da linha quando esta num datatable, pois o reset tem q ser no componente sem o indice, já o update tem q ser com o indice
			requestContext.reset(this.lovClientId.replaceAll(":\\d+:", ":"));
			requestContext.update(this.lovClientId);

			if (this.update != null) {
				String[] comps = this.update.split("\\s+|,");
				for (String c : comps) {
					if (!Util.isEmpty(c)) {
						UIComponent comp = lovComp.findComponent(c);
						if (comp != null) {
							String clientId = comp.getClientId(ctx);

							// Limpa o entityId dos botões de navegação das lovs dependentes (usado apenas nas Lovs da rotina 407 - DocumentosForm)
							UIComponent btnNavegar = comp.findComponent(clientId + "_btn_navegar");
							if (btnNavegar != null) {
								btnNavegar.getAttributes().remove("entityId");
								btnNavegar.getAttributes().remove("entityValueExp");
							}

							requestContext.reset(clientId);
							requestContext.update(clientId);
						} else {
							throw new FacesException(String.format("Componente não encontrado com id='%s' referenciado por '%s'", c, this.lovId));
						}
					}
				}
			}

			String id = this.lovClientId;
			if (this.focusOnClose) {
				//Seta o foco nele mesmo antes, caso o que for encontrado nao receba foco e fique sem foco em nenhum lugar
				requestContext.execute(String.format("PrimeFaces.focus('%s')", id));
				if (newValue != null) {
					id = Util.nvl(this.buscaProximoFoco(ctx, lovComp.getParent(), lovComp, 1), id);
				}
				if (id != null) {
					requestContext.execute(String.format("PrimeFaces.focus('%s')", id));
				}
			}
			//Foco
			this.focusOnClose = false;
		}
	}

	private String buscaProximoFoco(FacesContext ctx, UIComponent parent, UIComponent compAtual, int nivel) {
		boolean panelForm = "panelForm".equals(parent.getAttributes().get("xccomponent"));
		int indexOf = parent.getChildren().indexOf(compAtual);
		//Busca os proximos componentes renderizados
		// no primeiro seria é o label e o segundo é onde irá o foco
		int count = 0;
		for (int i = indexOf + 1; i < parent.getChildren().size(); i++) {
			UIComponent c = parent.getChildren().get(i);
			if (c.isRendered()) {
				if (panelForm) {
					if (count == 0) {
						count++;
					} else {
						return c.getClientId(ctx);
					}
				} else {
					return c.getClientId(ctx);
				}
			}
		}
		if (nivel < 5 && parent.getParent() != null) {
			return this.buscaProximoFoco(ctx, parent.getParent(), parent, ++nivel);
		}
		return null;
	}

	private void setXcpTipoLista(XcpTipoLista xcpTipoLista) {
		this.xcpTipoLista = xcpTipoLista;
		if (xcpTipoLista != null) {
			this.campos = xcpTipoLista.getDesCampos().split(";");
			this.titulos = new String[this.campos.length];
			for (int i = 0; i < this.campos.length; i++) {
				this.titulos[i] = "col_" + this.xcpTipoLista.getCodLov() + "_" + this.campos[i];
			}
		} else {
			this.campos = null;
			this.titulos = null;
		}
	}

	private void limpar() {
		this.setLista(null);
		this.setListaFiltrados(null);
		this.setXcpTipoLista(null);
		this.expValueLov = null;
		this.selectionListener = null;
		this.pesquisa = null;
		this.setSelectedEntity(null);
		this.rowObject = null;
		this.execOnOpen = false;
	}

	public void actionBuscar(ActionEvent event) {
		try {
			this.limpar();
			this.buscaParametros(event);
			this.setSelectedEntity((XcapeEntity) this.expValueLov.getValue(getFacesContext().getELContext()));
			RequestContext requestContext = getRequestContext();
			requestContext.execute(this.showDialogScript());

			if (this.execOnOpen) {
				this.filtrar();
			}

			requestContext.update("lovForm:tableLovContent");
			requestContext.update("lovForm:txtLovPesq");
			requestContext.update("lovForm:lovFocus");

			this.focusOnClose = true;
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	public void actionFiltrar(ActionEvent event) {
		try {
			this.filtrar();
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	private void filtrar() throws Exception {
		DataTable t = (DataTable) this.findComponent("tableLovContent");
		t.reset();
		t.setValueExpression("sortBy", null);
		this.setListaFiltrados(null);
		this.setLista(this.xcpSession.findLov(this.getEjbVars(), this.xcpTipoLista, this.getPesquisa(), false, this.getSystemVars(), this.params));
	}

	public void actionSelecionar(SelectEvent event) {
		try {
			XcapeEntity entity = this.getSelectedEntity();

			this.atribuiValor(event, entity);

		} catch (Exception e) {
			this.addFacesMessage(e);
		} finally {
			this.limpar();
		}
	}

	public void actionClose(CloseEvent event) {
		if (this.focusOnClose) {
			//Seta o foco nele mesmo antes, caso o que for encontrado nao receba foco e fique sem foco em nenhum lugar
			RequestContext.getCurrentInstance().execute(String.format("PrimeFaces.focus('%s')", this.lovClientId));
		}
		this.focusOnClose = true;
		this.limpar();
	}

	public List<XcapeEntity> getLista() {
		return this.lista;
	}

	public void setLista(List<XcapeEntity> lista) {
		this.lista = lista;
	}

	public List<XcapeEntity> getListaFiltrados() {
		return this.listaFiltrados;
	}

	public void setListaFiltrados(List<XcapeEntity> listaFiltrados) {
		this.listaFiltrados = listaFiltrados;
	}

	public String getPesquisa() {
		return this.pesquisa;
	}

	public void setPesquisa(String pesquisa) {
		this.pesquisa = pesquisa;
	}

	public String[] getCampos() {
		if (this.campos == null) {
			return new String[0];
		}
		return this.campos;
	}

	public int getQtdCampos() {
		return this.getCampos().length;
	}

	public XcapeEntity getSelectedEntity() {
		return this.selectedEntity;
	}

	public void setSelectedEntity(XcapeEntity selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public String[] getTitulos() {
		if (this.titulos == null) {
			return new String[0];
		}
		return this.titulos;
	}

	public void actionLimpar(ActionEvent event) {
		this.buscaParametros(event);
		this.atribuiValor(event, null);
		this.limpar();
	}

	public void actionNavegar(ActionEvent event) {
		String query = (String) event.getComponent().getAttributes().get("lovQuery");
		if (query == null) {
			addFacesMessage(this.getTraducao("msg_sem_navegacao"), MSG_INFO);
			return;
		}
		XcpTipoLista lov = this.xcpQuerySession.find(this.getEjbVars(), XcpTipoLista.class, query);
		if (lov == null) {
			addFacesMessage(this.getTraducao("msg_sem_navegacao"), MSG_INFO);
			return;
		}
		if (lov.getRotina() == null) {
			addFacesMessage(this.getTraducao("msg_sem_navegacao"), MSG_INFO);
			return;
		}

		Integer rotina = lov.getRotina();
		MenuAcessoVO acesso = this.getSession().getMenuAcesso(rotina);

		if (acesso == null) {
			addFacesMessage(this.getTraducao("msg_sem_navegacao"), MSG_INFO);
			return;
		}
		if (acesso.getComponente() == null) {
			addFacesMessage(this.getTraducao("msg_sem_navegacao"), MSG_INFO);
			return;
		}
		String param = "";
		// Customização para DocumentosForm
		if (rotina == 407) {
			try {
				Object entityId = event.getComponent().getAttributes().get("entityId");
				Object entityValueExp = event.getComponent().getAttributes().get("entityValueExp");
				if (entityId != null) {
					param = "entityId=" + entityId.toString();
				} else if (entityValueExp != null) {
					FacesContext facesContext = getFacesContext();

					String elExpString = (String) entityValueExp;
					XcapeEntity lovEntity = (XcapeEntity) TagHandlerUtils.createValueExp(facesContext, elExpString, XcapeEntity.class).getValue(
							facesContext.getELContext());
					if (lovEntity != null) {
						param = "entityId=" + lovEntity.getPK().toString();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String navegacao = String.format("XcpMenu.popup('%s','%s')", acesso.getComponente(), param);
		RequestContext.getCurrentInstance().execute(navegacao);

	}

	public Map getFieldValue() {
		return new HashMap() {
			@Override
			public Object get(Object key) {
				final XcapeEntity entity;
				if (key instanceof XcapeEntity) {
					entity = (XcapeEntity) key;
				} else {
					entity = null;
				}
				return new HashMap() {
					@Override
					public Object get(Object key) {
						String campo;
						if (entity != null) {
							if ("code".equals(key)) {
								campo = entity.getCode();
							} else {
								campo = entity.getDescription();
							}
							Object value = Util.getValue(entity, campo);
							if (value instanceof BigDecimal) {
								value = Converter.toString((BigDecimal) value, 0);
							} else if (value instanceof Date) {
								value = Converter.toString((Date) value);
							}
							return value;
						}
						return null;
					};
				};
			}
		};
	}

	public Map getColumnValue() {
		return new HashMap() {
			@Override
			public Object get(Object key) {
				final Object obj = key;
				return new HashMap() {
					@Override
					public Object get(Object key) {
						Object value = Util.getValue(obj, (String) key);
						if (value instanceof BigDecimal) {
							value = Converter.toString((BigDecimal) value, 0);
						} else if (value instanceof Date) {
							value = Converter.toString((Date) value);
						}
						return value;
					}
				};
			}
		};
	}

	public Map getSortValue() {
		return new HashMap() {
			@Override
			public Object get(Object key) {
				final Object obj = key;
				return new HashMap() {
					@Override
					public Object get(Object key) {
						return Util.getValue(obj, (String) key);
					}
				};
			}
		};
	}

	public Integer getLovRowindex() {
		return this.lovRowindex;
	}

	public void setLovRowindex(Integer lovRowindex) {
		this.lovRowindex = lovRowindex;
	}

}
