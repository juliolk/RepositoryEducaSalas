package com.adm.xcp.util;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.facelets.FaceletContext;

public class TagHandlerUtils {

	public final static Class[] PARAM_ACTION_EVENT = new Class[] { ActionEvent.class };
	public final static Class[] PARAM_CHANGE_EVENT = new Class[] { ValueChangeEvent.class };

	public static ValueExpression createValueExp(FaceletContext ctx, String expression, Class type) {
		return ctx.getExpressionFactory().createValueExpression(ctx, expression, type);
	}

	public static ValueExpression createValueExp(FacesContext ctx, String expression, Class type) {
		return ctx.getApplication().getExpressionFactory().createValueExpression(ctx.getELContext(), expression, type);
	}

	public static ValueExpression createValueExpObj(FaceletContext ctx, Object obj, Class type) {
		return ctx.getExpressionFactory().createValueExpression(obj, type);
	}

	public static ValueExpression createValueExpObj(FacesContext ctx, Object obj, Class type) {
		return ctx.getApplication().getExpressionFactory().createValueExpression(obj, type);
	}

	public static MethodExpression createMethodExp(FacesContext ctx, String action) {
		return ctx.getApplication().getExpressionFactory().createMethodExpression(ctx.getELContext(), action, String.class, new Class<?>[0]);
	}

	public static MethodExpression createMethodExp(FaceletContext ctx, String action) {
		return ctx.getExpressionFactory().createMethodExpression(ctx, action, String.class, new Class<?>[0]);
	}

	public static MethodExpression createMethodExp(FacesContext ctx, String action, Class[] params) {
		return ctx.getApplication().getExpressionFactory().createMethodExpression(ctx.getELContext(), action, null, params);
	}

	public static MethodExpression createMethodExp(FaceletContext ctx, String action, Class[] params) {
		return ctx.getExpressionFactory().createMethodExpression(ctx, action, null, params);
	}

	public static MethodExpressionActionListener createActionListener(FacesContext ctx, String action) {
		return new MethodExpressionActionListener(createMethodExp(ctx, action, PARAM_ACTION_EVENT));
	}

	public static MethodExpressionActionListener createActionListener(FaceletContext ctx, String action) {
		return new MethodExpressionActionListener(createMethodExp(ctx, action, PARAM_ACTION_EVENT));
	}

	public static MethodExpressionValueChangeListener createChangeListener(FaceletContext ctx, String action) {
		return new MethodExpressionValueChangeListener(createMethodExp(ctx, action, PARAM_CHANGE_EVENT));
	}

}
