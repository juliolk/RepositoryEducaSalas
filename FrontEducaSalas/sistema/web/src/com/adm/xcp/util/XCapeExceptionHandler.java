package com.adm.xcp.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.adm.util.Formatter;
import com.adm.util.exceptions.MensagemException;
import com.adm.xcp.backing.XcpViewUtilBacking;

public class XCapeExceptionHandler extends ExceptionHandlerWrapper {

	public static final String PG_ERRO = "/public/xcp/XcpErro.xhtml";

	public static final String ERROR_STACK = "XCapeExceptionHandler.errorStack";
	private static final Logger logger = Logger.getLogger(XCapeExceptionHandler.class);

	private final ExceptionHandler wrapped;

	public XCapeExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public void handle() throws FacesException {

		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx == null) {
			logger.warn("Faces context null. Impossivel tratar essa exception diretamente no handler. Passando adiante.");
			this.getWrapped().handle();
			return;
		}
		ExternalContext extCtx = ctx.getExternalContext();
		StringBuilder sb = null;

		extbreak: for (final Iterator<ExceptionQueuedEvent> it = this.getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();) {

			ExceptionQueuedEvent eqe = it.next();
			Throwable t = eqe.getContext().getException();

			//Sempre loga a exception que ocorreu.
			logger.error(t.getMessage(), t);

			t = trataException(t);
			if (t instanceof ViewExpiredException) {
				//Se tem uma view expired, nao importa o nivel, toca pro login
				try {
					ctx.getApplication().getNavigationHandler().handleNavigation(ctx, null, XcpViewConstants.PG_LOGIN_REDIRECT);
					ctx.renderResponse();
					break extbreak;
				} finally {
					it.remove();
				}
			} else if (t instanceof MensagemException) {
				PhaseId phaseId = ctx.getCurrentPhaseId();
				//Se a tela nao estiver pronta nao tem como disparar um faces message.
				if (phaseId != PhaseId.RENDER_RESPONSE) {
					try {
						XcpViewUtilBacking viewUtil = XcpViewUtilBacking.getInstance();
						viewUtil.addFacesMessage(t);
						break extbreak;
					} finally {
						it.remove();
					}
				}
			}

			//Realimenta a throwable principal
			t = eqe.getContext().getException();

			/**
			 * Se as exceptions nao sao conhecidas,
			 */
			if (sb == null) {
				sb = new StringBuilder();
			}

			sb.append(Formatter.getStackTrace(t));
			sb.append("---- \n");

		}

		try {
			if (sb != null) {

				PartialViewContext pvc = ctx.getPartialViewContext();
				String url = extCtx.encodeActionURL(ctx.getApplication().getViewHandler().getActionURL(ctx, PG_ERRO));
				extCtx.getSessionMap().put(ERROR_STACK, sb.toString());

				//Se e ajax manda um response diferente.
				HttpServletResponse resp = (HttpServletResponse) extCtx.getResponse();
				if (pvc.isAjaxRequest() || pvc.isPartialRequest()) {
					try {
						extCtx.responseReset();

						StringBuilder sba = new StringBuilder();
						sba.append("<?xml version='1.0' encoding='UTF-8'?>");
						sba.append("<partial-response><redirect url=\"").append(url).append("\"/></partial-response>");
						resp.getWriter().print(sba.toString());
						resp.flushBuffer();
					} catch (IOException e1) {
						throw new FacesException(e1);
					}
				} else {
					resp.sendRedirect(url);
				}
				ctx.responseComplete();
			}
		} catch (Exception e) {
			logger.error("Redirect para pagina de erro provavelmente tenha falhado", e);
		}

		this.getWrapped().handle();
	}

	public static Throwable trataException(Throwable throwableOrigem) {
		Throwable t = throwableOrigem;
		/**
		 * Tenta procurar uma Excecao conhecida em o maximo 30 niveis. Se
		 * encontrar, ela é tratada.
		 */
		int level = 0;
		while (t != null && level < 30) {
			if (t instanceof ViewExpiredException) {
				//Se tem uma view expired, nao importa o nivel, toca pro login
				return t;
			} else if (t instanceof MensagemException) {
				//Se for uma mensagem exception, quer dizer que e uma mensagem tratada. Avisa com o faces message
				return t;
			} else if (t instanceof SQLException) {
				SQLException sqlEx = (SQLException) t;
				String msg = null;
				if (sqlEx.getErrorCode() == 20003) {
					msg = sqlEx.getMessage();
					int posIni = msg.indexOf(" ");
					int posFim = msg.indexOf("\n");
					msg = msg.substring(posIni + 1, posFim);
				} else if (sqlEx.getErrorCode() == 2292) {
					msg = sqlEx.getMessage();
					int posIni = msg.indexOf(".");
					int posFim = msg.indexOf(")");
					msg = String.format("msg_ora_%05d", sqlEx.getErrorCode()) + msg.substring(posIni + 1, posFim);
				} else {
					msg = String.format("msg_ora_%05d", sqlEx.getErrorCode());
				}

				return new MensagemException(msg);
			}

			t = t.getCause();
			level++;
		}

		return throwableOrigem;
	}
}