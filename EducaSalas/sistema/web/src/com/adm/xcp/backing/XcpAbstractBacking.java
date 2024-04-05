package com.adm.xcp.backing;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.adm.ejb.vo.MensagemVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.vos.XcpAuditEntityVO;

public abstract class XcpAbstractBacking extends XcpAbstractBackingBeanParametrosSistema {
	//Backing com metodos basicos utilizados por todos backings.
	//Foi feita estrutura em "extends" para separar e ficar mais facil de encontrar os metodos relacionados a determinada operação
	public boolean comErro(List<MensagemVO> msgErros) {
		boolean comErro = false;
		if (!Util.isEmpty(msgErros)) {
			for (MensagemVO erro : msgErros) {
				if (erro.isAviso()) {
					addFacesMessage(this.getTraducao(erro), MSG_INFO);
				} else {
					comErro = true;
					addFacesMessage(this.getTraducao(erro));
				}
			}
			return comErro;
		}
		return comErro;
	}

	public boolean comErro(MensagemVO msgErro) {
		if (msgErro == null) {
			return false;
		}
		if (msgErro.isAviso()) {
			addFacesMessage(this.getTraducao(msgErro), MSG_INFO);
		} else {
			addFacesMessage(this.getTraducao(msgErro));
			return true;
		}
		return false;
	}

	public boolean existeObjeto(String desObjeto) {
		MontaQuery q = new MontaQuery("Select e from XcpObjeto e");
		q.addWhere("codObjeto", "=", desObjeto);
		List list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		if (!Util.isEmpty(list)) {
			try {
				String desRootDir = this.getParametroString("Sistema", 4);
				//				desRootDir = "/servers/wildfly-8.1.0.Final/relatorios";
				Path path = Paths.get(desRootDir, desObjeto);
				if (path.toFile().exists()) {
					File[] listFiles = path.toFile().listFiles();
					if (listFiles != null && listFiles.length > 0) {
						for (File file : listFiles) {
							if (file.getName().toLowerCase().endsWith(".jasper")) {
								return true;
							}
						}
					}
				}
			} catch (Exception e) {
				//ignora
			}
		}

		return false;
	}

	public List<XcpAuditEntityVO> getAuditEntities() {
		return null;
	}

	public boolean isManutencaoEsoc() {
		return Objects.equals("1", System.getProperty("com.xcape.MANUTENCAOESOC", "0"));
	}
}
