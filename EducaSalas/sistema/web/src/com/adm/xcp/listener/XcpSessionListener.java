package com.adm.xcp.listener;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.adm.ejb.XcpEjbConstants;
import com.adm.ejb.entity.Acessos;
import com.adm.ejb.session.remote.OperadoresSession;
import com.adm.ejb.util.LoginLogoutDAO;
import com.adm.ejb.vo.LoginLogoutVO;
import com.adm.xcp.backing.XcpApplicationBacking;
import com.adm.xcp.backing.XcpSessionBacking;
import com.adm.xcp.util.XcpPermissaoAcaoCache;

public class XcpSessionListener implements HttpSessionListener, XcpEjbConstants {

	private static final Logger logger = Logger.getLogger(XcpSessionListener.class);

	@Resource(mappedName = RESOURCE_DATA_SOURCE)
	private DataSource datasource;

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		//nada
	}

	@EJB
	private OperadoresSession session;

	@PersistenceContext
	protected EntityManager em;

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		XcpSessionBacking session = (XcpSessionBacking) httpSession.getAttribute(XcpSessionBacking.ID);

		//Nao identificado onde que esta sendo criado uma sessao extra. Entao so ignora.
		if (session == null) {
			return;
		}

		//Remove a sessão do usuário logado
		XcpApplicationBacking xcpApplicationBacking = (XcpApplicationBacking) httpSession.getServletContext().getAttribute("xcpApplicationBacking");
		if (xcpApplicationBacking != null) {
			xcpApplicationBacking.finalizarSessao(session.getSessao());
		}

		if (session.getUsuario() != null && session.getUsuario().getOperador() != null) {
			XcpPermissaoAcaoCache.getInstance().clear(session.getUsuario().getOperador());
		}

		LoginLogoutVO loginLogoutVO = session.getLoginLogoutVO();
		if (loginLogoutVO != null) {
			loginLogoutVO.setTipOperacao(Acessos.TIP_ACESSO_2_LOGOUT);
			try {
				LoginLogoutDAO.grava(loginLogoutVO, this.datasource.getConnection());
			} catch (Exception e) {
				logger.error("Erro ao tentar marcar a sessao como encerrada", e);
			}

		}
	}
}
