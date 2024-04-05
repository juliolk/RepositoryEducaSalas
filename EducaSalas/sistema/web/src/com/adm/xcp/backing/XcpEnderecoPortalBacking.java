package com.adm.xcp.backing;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.adm.ejb.entity.extend.CepBuscaEnd;
import com.adm.util.MontaQuery;

@ManagedBean
@RequestScoped
public class XcpEnderecoPortalBacking extends XcpEnderecoBacking {
	
	@Override
	protected void buscaDadosCep(Integer cep) {
		StringBuilder sb = new StringBuilder();
		sb.append("select e from CepBuscaEnd e");
		sb.append("  left join fetch e.logradouroFk");
		sb.append("  left join fetch e.bairroFk");
		
		MontaQuery q = new MontaQuery(sb.toString());
		q.addWhere("cep", "=", cep);
		this.getEndereco().setBairroFk(null);
		this.getEndereco().setCidadeFk(null);
		this.getEndereco().setLogradouroFk(null);

		try {
			List<CepBuscaEnd> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			if (!list.isEmpty()) {
				CepBuscaEnd cepSelecionado = list.get(0);
				this.getEndereco().setBairroFk(cepSelecionado.getBairroFk());
				this.getEndereco().setCidadeFk(cepSelecionado.getCidadeFk());
				this.getEndereco().setLogradouroFk(cepSelecionado.getLogradouroFk());
				if (this.getEndereco().getNumero() != null) {
					Integer numero = this.getEndereco().getNumero();
					Integer nroInicial = cepSelecionado.getNrinicial();
					Integer nroFinal = cepSelecionado.getNrfinal();
					if (nroInicial != null) {
						if (!((numero.compareTo(nroFinal) <= 0 && numero.compareTo(nroInicial) >= 0))) {
							numero = nroInicial;
						}
					}
					this.getEndereco().setNumero(numero);
				} else {
					this.getEndereco().setNumero(cepSelecionado.getNrinicial());
				}
			} else {
				addFacesMessage(this.getTraducao("msg_cep_nao_locaizado"));
			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}
	
}
