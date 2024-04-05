package com.adm.xcp.backing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.event.AjaxBehaviorEvent;

import com.adm.ejb.entity.Bairro;
import com.adm.ejb.entity.Cep;
import com.adm.ejb.entity.Cidades;
import com.adm.ejb.entity.Logradouro;
import com.adm.ejb.entity.extend.CepBuscaEnd;
import com.adm.ejb.vo.XcpEnderecoVO;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.xcp.event.XcpLovEvent;

@ManagedBean
@RequestScoped
public class XcpEnderecoBacking extends XcpAbstractBacking {

	public XcpEnderecoVO endereco;

	public XcpEnderecoVO getEndereco() {
		return this.endereco;
	}

	public void setEndereco(XcpEnderecoVO endereco) {
		this.endereco = endereco;
	}

	protected void buscaDadosCep(Integer cep) {
		MontaQuery q = new MontaQuery("select e from CepBuscaEnd e");
		q.addWhere("cep", "=", cep);

		try {
			List<CepBuscaEnd> list = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
			if (!list.isEmpty()) {
				this.getEndereco().setBairroFk(list.get(0).getBairroFk());
				this.getEndereco().setCidadeFk(list.get(0).getCidadeFk());
				this.getEndereco().setLogradouroFk(list.get(0).getLogradouroFk());
				if (this.getEndereco().getNumero() != null) {
					Integer numero = this.getEndereco().getNumero();
					Integer nroInicial = list.get(0).getNrinicial();
					Integer nroFinal = list.get(0).getNrfinal();
					if (nroInicial != null) {
						if (!((numero.compareTo(nroFinal) <= 0 && numero.compareTo(nroInicial) >= 0))) {
							numero = nroInicial;
						}
					}
					this.getEndereco().setNumero(numero);
				} else {
					this.getEndereco().setNumero(list.get(0).getNrinicial());
				}
			} else {
				addFacesMessage(this.getTraducao("msg_cep_nao_locaizado"));
			}
		} catch (Exception e) {
			this.addFacesMessage(e);
		}
	}

	// Carrega o cep na sequencia até encontrar o que mais se enquadra
	private Integer carregaCep(Integer cidade, Integer bairro, Integer logradouro, Integer numero) {
		Integer cep = this.getEndereco().getCep();
		MontaQuery q = new MontaQuery("select e from Cep e");
		q.addWhere("cidade", "=", cidade);
		if (bairro != null) {
			q.addWhere("bairro", "=", bairro);
		}
		if (logradouro == null) {
			q.addWhere("logradouro is null");
		} else {
			q.addWhere("logradouro", "=", logradouro);
		}
		q.addWhere("nrinicial is null");
		List<Cep> listaCep = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		if (!Util.isEmpty(listaCep)) {
			cep = listaCep.get(0).getCep();
		}
		if (numero == null) {
			return cep;
		}
		Boolean par = false;
		if ((numero % 2) == 0) {
			par = true;
		}
		q = new MontaQuery("select e from Cep e");
		q.addWhere("cidade", "=", cidade);
		if (bairro != null) {
			q.addWhere("bairro", "=", bairro);
		}
		if (logradouro == null) {
			q.addWhere("logradouro is null");
		} else {
			q.addWhere("logradouro", "=", logradouro);
		}
		q.addWhere("nrinicial", "<=", numero);
		q.addWhere("nrfinal", ">=", numero);
		q.setOrderBy("tipLado");
		listaCep = this.xcpQuerySession.executeQueryList(this.getEjbVars(), q);
		if (Util.isEmpty(listaCep) && bairro == null) {
			// Alterado para não trocar o CEP quando não tem cadastro por número de rua.
			return cep;
		}
		if (!Util.isEmpty(listaCep)) {
			for (Cep cepLido : listaCep) {
				if (cepLido.getTipLado() == null) {
					cep = cepLido.getCep();
				} else {
					if (par && cepLido.getTipLado().equals(Cep.TIP_LADO_1_PAR)) {
						cep = cepLido.getCep();
						break;
					}
					if (!par && cepLido.getTipLado().equals(Cep.TIP_LADO_2_IMPAR)) {
						cep = cepLido.getCep();
						break;
					}
				}
			}
		}
		// Alterado dia 29/11/2017. Caso nao encontre um cep condizente com as informacoes
		// da tela, nao recarrega todo o cadastro pelo cep novamente.
		//	this.buscaDadosCep(cep);
		return cep;
	}

	private void carregar(UIComponent component) {
		if (component != null) {
			if (component.getAttributes().get("xcpEndValue") instanceof XcpEnderecoVO) {
				this.setEndereco((XcpEnderecoVO) component.getAttributes().get("xcpEndValue"));
			} else if (component.getChildren() != null) {
				for (UIComponent c : component.getChildren()) {
					if (c instanceof UIParameter) {
						this.setEndereco((XcpEnderecoVO) ((UIParameter) c).getValue());
					}
				}
			}
		}
	}

	public void changeCep(AjaxBehaviorEvent event) {
		this.carregar(event.getComponent());
		if (this.getEndereco() == null) {
			return;
		}

		if (this.getEndereco().getCep() != null) {
			this.buscaDadosCep(this.getEndereco().getCep());
		}
	}

	public Map getParamsCidade(XcpEnderecoVO endereco) {
		Map p = new HashMap();
		if (endereco != null && endereco.getCidadeFk() != null) {
			p.put("cidade", endereco.getCidadeFk().getCidade());
		} else {
			p.put("cidade", null);
		}
		return p;
	}

	public void selectCidade(XcpLovEvent event) {
		this.carregar(event.getComponent());
		if (this.getEndereco() == null) {
			return;
		}
		this.getEndereco().setBairroFk(null);
		this.getEndereco().setLogradouroFk(null);
		this.getEndereco().setNumero(null);
		Cidades cidade = (Cidades) event.getNewValue();
		if (cidade != null && cidade.getCidade() != null) {
			this.getEndereco().setCep(this.carregaCep(cidade.getCidade(), null, null, null));
			return;
		}
	}

	public void selectBairro(XcpLovEvent event) {
		this.carregar(event.getComponent());
		if (this.getEndereco() == null) {
			return;
		}
		Bairro bairro = (Bairro) event.getNewValue();
		if (bairro == null || bairro.getBairro() == null) {
			return;
		}
		Integer numCidade = bairro.getCidade();
		Integer numBairro = bairro.getBairro();
		Integer numero = this.getEndereco().getNumero();
		Integer numLogradouro = null;

		if (this.getEndereco().getLogradouroFk() != null) {
			numLogradouro = this.getEndereco().getLogradouroFk().getLogradouro();
		}
		this.getEndereco().setCep(this.carregaCep(numCidade, numBairro, numLogradouro, numero));

	}

	public void selectLogradouro(XcpLovEvent event) {
		this.carregar(event.getComponent());
		if (this.getEndereco() == null) {
			return;
		}
		Logradouro logradouro = (Logradouro) event.getNewValue();
		if (logradouro == null || logradouro.getLogradouro() == null) {
			return;
		}
		Integer numCidade = logradouro.getCidade();
		Integer numBairro = null;
		Integer numLogradouro = logradouro.getLogradouro();
		Integer numero = this.getEndereco().getNumero();
		if (this.getEndereco().getBairroFk() != null) {
			numBairro = this.getEndereco().getBairroFk().getBairro();
		}
		this.getEndereco().setCep(this.carregaCep(numCidade, numBairro, numLogradouro, numero));
	}

	public void changeNumero(AjaxBehaviorEvent event) {
		this.carregar(event.getComponent());
		if (this.getEndereco() == null) {
			return;
		}
		Integer numCidade = null;
		Integer numLogradouro = null;
		Integer numero = this.getEndereco().getNumero();
		if (this.getEndereco().getCidadeFk() != null) {
			numCidade = this.getEndereco().getCidadeFk().getCidade();
		}
		if (this.getEndereco().getLogradouroFk() != null) {
			numLogradouro = this.getEndereco().getLogradouroFk().getLogradouro();
		}
		// a troca de número faz ignorar o bairro pois, uma rua pode passar por vários bairros
		this.getEndereco().setCep(this.carregaCep(numCidade, null, numLogradouro, numero));
	}
}
