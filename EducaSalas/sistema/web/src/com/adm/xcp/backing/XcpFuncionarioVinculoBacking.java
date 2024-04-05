package com.adm.xcp.backing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adm.util.Converter;

public abstract class XcpFuncionarioVinculoBacking extends XcpFuncionarioBacking {

	private boolean lancVinculos;
	private boolean lancVinculosCID;
	private List<BigDecimal> listVinc;

	public List<BigDecimal> getListaVinculos() {
		if (this.getFuncionario() == null) {
			return null;
		}

		if (this.getFuncionario().getNumCpf() != null && this.getFuncionario().getNumCpf() > 0L) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cpf", this.getFuncionario().getNumCpf());
			map.put("empresa", this.getFuncionario().getCodEmpresa());

			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT f.matricula ");
			sb.append("  FROM funcionarios    f ");
			sb.append("      ,vinculos        v ");
			sb.append("      ,regime_juridico r ");
			sb.append("      ,tipomovtos      t ");
			sb.append(" WHERE f.vinculo = v.vinculo ");
			sb.append("   AND v.regime = r.codigo ");
			sb.append("   and t.tipmov = f.tipmov ");
			sb.append("   and r.tipo not in (4,5,7,99) ");
			sb.append("   and t.classificacao not in (1,5,12) ");
			sb.append("   and f.empresa = :empresa ");
			sb.append("   and f.cpf = :cpf ");

			if (this.getFuncionario().getNumMatricula() != null) {
				map.put("matricula", this.getFuncionario().getNumMatricula());
				sb.append(" and f.matricula <> :matricula ");
			}
			sb.append(" order by matricula desc ");

			return this.xcpQuerySession.executeNativeQueryList(this.getEjbVars(), sb.toString(), map);

		}
		return null;
	}

	public boolean isRenderedCheckVinc() {
		if (!this.getListVinc().isEmpty()) {
			return true;
		}

		return false;
	}

	public boolean isRenderedCheckVincCID() {
		if (!this.getListVinc().isEmpty() && !this.getEntity().isNovo()) {
			return true;
		}

		return false;
	}

	public String getMatsDuploVinc() {
		List<BigDecimal> listVinc = this.getListVinc();
		if (listVinc.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (BigDecimal mat : listVinc) {
			sb.append(Converter.toInteger(mat));
			sb.append(",");
		}

		String mats = sb.toString();
		return mats.substring(0, mats.length() - 1);
	}

	public boolean isLancVinculos() {
		return this.lancVinculos;
	}

	public void setLancVinculos(boolean lancVinculos) {
		this.lancVinculos = lancVinculos;
	}

	public boolean isLancVinculosCID() {
		return this.lancVinculosCID;
	}

	public void setLancVinculosCID(boolean lancVinculosCID) {
		this.lancVinculosCID = lancVinculosCID;
	}

	public List<BigDecimal> getListVinc() {
		if (this.listVinc == null) {
			this.listVinc = new ArrayList<BigDecimal>();
		}
		return this.listVinc;
	}

	public void setListVinc(List<BigDecimal> listVinc) {
		this.listVinc = listVinc;
	}
}