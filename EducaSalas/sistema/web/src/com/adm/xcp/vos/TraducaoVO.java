package com.adm.xcp.vos;

public class TraducaoVO {

	private String desChave;
	private String desTraducao;
	private boolean existe = false;

	public TraducaoVO(String desChave, boolean existe) {
		this.desChave = desChave;
		this.existe = existe;
	}

	public String getDesChave() {
		return this.desChave;
	}

	public void setDesChave(String desChave) {
		this.desChave = desChave;
	}

	public String getDesTraducao() {
		if (!this.existe) {
			if (this.desChave.startsWith("hlp_")) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			//			sb.append("!!!-");
			sb.append(this.desChave);
			//			sb.append("-!!!");
			this.desTraducao = sb.toString();
		}
		return this.desTraducao;
	}

	public void setDesTraducao(String desTraducao) {
		this.desTraducao = desTraducao;
	}

}
