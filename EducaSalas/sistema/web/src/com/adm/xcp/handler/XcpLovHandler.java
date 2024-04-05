package com.adm.xcp.handler;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;

import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.message.Message;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.outputpanel.OutputPanel;
import org.primefaces.component.overlaypanel.OverlayPanel;
import org.primefaces.component.tooltip.Tooltip;

import com.adm.xcp.backing.XcpViewUtilBacking;
import com.adm.xcp.util.TagHandlerUtils;

public class XcpLovHandler extends XcpLovAbstractHandler {

	private final TagAttribute _required;
	private final TagAttribute _requiredMessage;

	private final TagAttribute _size;
	private final TagAttribute _sizeDesc;
	private final TagAttribute _label;
	private final TagAttribute _rendered;
	private final TagAttribute _readonly;
	private final TagAttribute _simple;
	private final TagAttribute _help;
	private final TagAttribute _dynamicForm;
	private final TagAttribute _tabindex;
	private final TagAttribute _onlycodigo;
	private final TagAttribute _inputPanelClass;

	private static final String ID_BTN_BUSCAR = "_btn_buscar";
	private static final String ID_BTN_LIMPAR = "_btn_limpar";
	private static final String ID_BTN_NAVEGAR = "_btn_navegar";
	private static final String ID_BTN_HELP = "_btn_help";
	private static final String ID_TXT_CODIGO = "_txt_cod";
	private static final String ID_TXT_DESC = "_txt_desc";

	public XcpLovHandler(ComponentConfig config) {
		super(config);

		this._required = this.getAttribute("required");
		this._requiredMessage = this.getAttribute("requiredMessage");
		this._size = this.getAttribute("size");
		this._sizeDesc = this.getAttribute("sizeDesc");
		this._label = this.getAttribute("label");
		this._simple = this.getAttribute("simple");
		this._rendered = this.getAttribute("rendered");
		this._readonly = this.getAttribute("readonly");
		this._help = this.getAttribute("help");
		this._dynamicForm = this.getAttribute("dynamicForm");
		this._tabindex = this.getAttribute("tabindex");
		this._onlycodigo = this.getAttribute("onlyCodigo");
		this._inputPanelClass = this.getAttribute("inputPanelClass");		
	}

	@Override
	public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {

		UIComponent panLabel = parent.findComponent(c.getId() + "_l");
		HtmlOutputText outLoop = (HtmlOutputText) parent.findComponent(c.getId() + "_dfsep");

		if (!this.isSimple(ctx)) {
			if (panLabel == null) {
				panLabel = new OutputPanel();
				panLabel.setId(c.getId() + "_l");

				OutputLabel label = new OutputLabel();
				label.setFor(c.getId() + ID_TXT_CODIGO);

				if (this._label != null) {
					label.setValueExpression(
							"value",
							ctx.getExpressionFactory().createValueExpression(ctx, this._label.getValue() + "#{viewUtil.labelSufix}", Object.class));
				}

				if (this._rendered != null) {
					label.setValueExpression("rendered", this._rendered.getValueExpression(ctx, Boolean.class));
				}
				panLabel.getChildren().add(label);
				parent.getChildren().add(panLabel);
			} else {
				parent.getChildren().remove(panLabel);
				parent.getChildren().add(panLabel);
			}

			if (this._dynamicForm != null) {
				if (outLoop == null) {
					outLoop = new HtmlOutputText();
					outLoop.setId(c.getId() + "_dfsep");
					outLoop.setEscape(false);
					outLoop.setValue("</td><td class='form_content'>");
					if (this._rendered != null) {
						outLoop.setValueExpression("rendered", this._rendered.getValueExpression(ctx, Boolean.class));
					}

					parent.getChildren().add(outLoop);
				} else {
					parent.getChildren().remove(outLoop);
					parent.getChildren().add(outLoop);
				}
			}
		}

		HtmlPanelGroup panel = new HtmlPanelGroup();
		if(this._inputPanelClass != null){
			panel.setLayout("block");
			panel.setValueExpression("styleClass", this._inputPanelClass.getValueExpression(ctx, String.class));
		}
		panel.setStyle("white-space: nowrap;");
		boolean reload = false;
		if (this._rendered != null) {
			panel.setValueExpression("rendered", this._rendered.getValueExpression(ctx, Boolean.class));
		}

		panel.getChildren().add(this.createTxtCodigo(c.getId(), reload, ctx));
		panel.getChildren().add(this.createBtnBuscar(c.getId(), ctx));

		if (!this.isOnlyCodigo(ctx)) {
			panel.getChildren().add(this.createTxtDescricao(c.getId(), reload, ctx));
			panel.getChildren().add(this.createBtnLimpar(c.getId(), reload, ctx));
			panel.getChildren().add(this.createBtnNavegar(c.getId(), reload, ctx));
		}

		if (this._help != null) {
			this.createHelp(panel, c.getId(), ctx);
		}
		panel.getChildren().add(this.createMessage(c.getId(), reload, ctx));

		c.getChildren().add(panel);
	}

	private UIComponent createTxtCodigo(String id, boolean reload, FaceletContext ctx) {
		InputText input = this.createInputText(id, reload, ctx, "#{xcpLovBacking.valueChangeCodigo}", "code");

		input.setId(id + ID_TXT_CODIGO);

		if (this._size != null) {
			input.setValueExpression("size", this._size.getValueExpression(ctx, Integer.class));
		} else {
			input.setSize(10);
		}

		if (this._label != null) {
			input.setValueExpression("label", this._label.getValueExpression(ctx, Object.class));
		}

		if (this._required != null) {
			input.setValueExpression("required", this._required.getValueExpression(ctx, Boolean.class));
		}

		if (this._requiredMessage != null) {
			input.setValueExpression("requiredMessage", this._requiredMessage.getValueExpression(ctx, Object.class));
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("#{bundle.msg_campo_obrigatorio.replaceAll(\"%s\", \"");
			stringBuilder.append(this._label.getValue(ctx));
			stringBuilder.append("\")}");
			input.setValueExpression("requiredMessage", ctx.getExpressionFactory().createValueExpression(ctx, stringBuilder.toString(), Object.class));
		}

		this.createTooltip(ctx, input, id);
		return input;
	}

	private void createTooltip(FaceletContext ctx, InputText input, String id) {
		UIComponent cTooltip = input.findComponent(input.getId() + "_tooltip");
		if (cTooltip == null) {
			Tooltip tooltip = new Tooltip();
			tooltip.setFor(input.getId());
			tooltip.setId(input.getId() + "_tooltip");
			tooltip.setValueExpression("rendered", TagHandlerUtils.createValueExp(ctx, String.format("#{toolTip['%s']!=null}", id), Boolean.class));
			tooltip.setValueExpression("value", TagHandlerUtils.createValueExp(ctx, String.format("#{toolTip['%s']}", id), String.class));
			tooltip.setEscape(false);
			input.getChildren().add(tooltip);
		}
	}

	private UIComponent createTxtDescricao(String id, boolean reload, FaceletContext ctx) {
		InputText input = this.createInputText(id, reload, ctx, "#{xcpLovBacking.valueChangeDescricao}", "description");

		input.setId(id + ID_TXT_DESC);
		if (this._sizeDesc != null) {
			input.setValueExpression("size", this._sizeDesc.getValueExpression(ctx, Integer.class));
		} else {
			input.setSize(50);
		}

		this.createTooltip(ctx, input, id);
		return input;
	}

	private InputText createInputText(String id, boolean reload, FaceletContext ctx, String valueChangeListener, String campo) {
		InputText input = new InputText();

		if (this._tabindex != null) {
			input.setValueExpression("tabindex", this._tabindex.getValueExpression(ctx, String.class));
		}

		this.setLovAttributes(input, ctx);

		input.setAutocomplete("off");
		Boolean bloqueado = XcpViewUtilBacking.getInstance().isBloqueado(id);
		if (Boolean.TRUE.equals(bloqueado)) {
			input.setReadonly(true);
		} else {
			if (this._readonly != null) {
				input.setValueExpression("readonly", this._readonly.getValueExpression(ctx, Boolean.class));
			}
		}

		input.addValueChangeListener(TagHandlerUtils.createChangeListener(ctx, valueChangeListener));

		AjaxBehavior behavior = new AjaxBehavior();
		behavior.setProcess("@this");
		behavior.setValueExpression("update", ctx.getExpressionFactory().createValueExpression(ctx, this.getUpdateTarget(ctx, id, reload), Object.class));

		/**
		 * substituido blur por valueChange para evitar ajax (e as validacoes de
		 * tela) quando o campo esta vazio (primeira vez)
		 */
		input.addClientBehavior("valueChange", behavior);// valueChange

		input.setOnkeyup(String.format("XCLov.inputKeyPress(event,this,'%s','%s','%s');", (reload ? "form" : id), id, id + ID_BTN_BUSCAR));

		String valExp = this._value.getValue();
		valExp = valExp.substring(2, valExp.length() - 1);

		input.setValueExpression("value", TagHandlerUtils.createValueExp(
				ctx,
				String.format("%s{xcpLovBacking.fieldValue[%s == null ? 'nulo' : %s]['%s']}", "#", valExp, valExp, campo),
				Object.class));

		return input;
	}

	private String getUpdateTarget(FaceletContext ctx, String id, boolean reload) {
		if (reload) {
			return "@form";
		}

		StringBuilder update = new StringBuilder();
		update.append(id);
		update.append(",");
		if (!this.isSimple(ctx)) {
			update.append(id);
			update.append("_l");
		}
		if (this._update != null) {
			update.append(",");
			update.append(this._update.getValue());
		}
		return update.toString();
	}

	private UIComponent createBtnBuscar(String id, FaceletContext ctx) {
		CommandButton btnBuscar = new CommandButton();
		btnBuscar.setId(id + ID_BTN_BUSCAR);
		btnBuscar.setIcon("icon-pesquisar");
		btnBuscar.setStyleClass("lov_btnBuscar");
		btnBuscar.setTabindex("-1");

		btnBuscar.setValueExpression("title", ctx.getExpressionFactory().createValueExpression(ctx, "#{bundle.hint_btnLovBuscar}", Object.class));

		btnBuscar.addActionListener(TagHandlerUtils.createActionListener(ctx, "#{xcpLovBacking.actionBuscar}"));

		this.setLovAttributes(btnBuscar, ctx);

		Boolean bloqueado = XcpViewUtilBacking.getInstance().isBloqueado(id);
		if (Boolean.TRUE.equals(bloqueado)) {
			btnBuscar.setDisabled(true);
		} else {
			if (this._readonly != null) {
				btnBuscar.setValueExpression("disabled", this._readonly.getValueExpression(ctx, Boolean.class));
			}
		}

		btnBuscar.setProcess("@this");

		return btnBuscar;
	}

	private UIComponent createBtnLimpar(String id, boolean reload, FaceletContext ctx) {
		CommandButton button = new CommandButton();
		button.setId(id + ID_BTN_LIMPAR);

		button.setStyleClass("lov_btnLimpar");
		button.setIcon("icon-limpar");
		button.setTabindex("-1");

		button.setValueExpression("title", ctx.getExpressionFactory().createValueExpression(ctx, "#{bundle.hint_btnLovLimpar}", Object.class));

		button.addActionListener(TagHandlerUtils.createActionListener(ctx, "#{xcpLovBacking.actionLimpar}"));

		this.setLovAttributes(button, ctx);

		button.setValueExpression("update", ctx.getExpressionFactory().createValueExpression(ctx, this.getUpdateTarget(ctx, id, reload), Object.class));

		button.setProcess("@this");

		Boolean bloqueado = XcpViewUtilBacking.getInstance().isBloqueado(id);
		if (Boolean.TRUE.equals(bloqueado)) {
			button.setDisabled(true);
		} else {
			if (this._readonly != null) {
				button.setValueExpression("disabled", this._readonly.getValueExpression(ctx, Boolean.class));
			}
		}

		return button;
	}

	private UIComponent createBtnNavegar(String id, boolean reload, FaceletContext ctx) {
		CommandButton button = new CommandButton();
		button.setId(id + ID_BTN_NAVEGAR);

		button.setStyleClass("lov_btnNavegar");
		button.setIcon("icon-navegar");
		button.setTabindex("-1");

		button.setValueExpression("title", ctx.getExpressionFactory().createValueExpression(ctx, "#{bundle.hint_btnLovNavegar}", Object.class));

		button.addActionListener(TagHandlerUtils.createActionListener(ctx, "#{xcpLovBacking.actionNavegar}"));

		this.setLovAttributes(button, ctx);

		button.setProcess("@this");

		Boolean bloqueado = XcpViewUtilBacking.getInstance().isBloqueado(id);
		if (Boolean.TRUE.equals(bloqueado)) {
			button.setDisabled(true);
		} else {
			if (this._readonly != null) {
				button.setValueExpression("disabled", this._readonly.getValueExpression(ctx, Boolean.class));
			}
		}
		
		// Para permitir a edição direta do registro selecionado. 
		// Por enquanto, utilizado apenas nas LOVs de Documento (todas da rotina 407)
		if (this._value != null) {
			String valExp = this._value.getValue();
			button.getAttributes().put("entityValueExp", valExp);
		}

		return button;
	}

	private void createHelp(UIComponent panel, String id, FaceletContext ctx) {
		CommandButton button = new CommandButton();
		button.setId(id + ID_BTN_HELP);

		button.setIcon("icon-help");
		button.setTabindex("-1");
		button.setType("button");
		button.setStyleClass("lov_btnHelp");

		button.setValueExpression("title", TagHandlerUtils.createValueExp(ctx, "#{bundle.hint_btnLovHelp}", Object.class));

		button.setValueExpression(
				"rendered",
				TagHandlerUtils.createValueExp(ctx, this._help.getValue().substring(0, 2) + "not empty " + this._help.getValue().substring(2), Boolean.class));

		OverlayPanel overlay = new OverlayPanel();
		overlay.setFor(id + ID_BTN_HELP);

		HtmlOutputText output = new HtmlOutputText();
		output.setValueExpression("value", this._help.getValueExpression(ctx, Object.class));
		overlay.getChildren().add(output);

		panel.getChildren().add(button);
		panel.getChildren().add(overlay);
	}

	private UIComponent createMessage(String id, boolean reload, FaceletContext ctx) {
		Message message = new Message();
		message.setFor(id + ID_TXT_CODIGO);
		return message;
	}

	@Override
	public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {

		if (!isNew(c)) {
			UIComponent label = c.getParent().findComponent(c.getId() + "_l");

			if (label != null) {
				c.getParent().getChildren().remove(label);
				c.getParent().getChildren().add(label);
			}

			UIComponent outLoop = c.getParent().findComponent(c.getId() + "_dfsep");
			if (outLoop != null) {
				c.getParent().getChildren().remove(outLoop);
				c.getParent().getChildren().add(outLoop);
			}
		}
		super.applyNextHandler(ctx, c.getChildren().get(0));
	}

	@Override
	protected MetaRuleset createMetaRuleset(Class type) {
		return super.createMetaRuleset(type).ignore("rendered");
	}

	private boolean isSimple(FaceletContext ctx) {
		String simple = "FALSE";
		if (this._simple != null) {
			simple = this._simple.getValue(ctx).toUpperCase();
		}
		return Boolean.valueOf(simple);
	}

	private boolean isOnlyCodigo(FaceletContext ctx) {
		String only = "FALSE";
		if (this._onlycodigo != null) {
			only = this._onlycodigo.getValue(ctx).toUpperCase();
		}
		return Boolean.valueOf(only);
	}

}