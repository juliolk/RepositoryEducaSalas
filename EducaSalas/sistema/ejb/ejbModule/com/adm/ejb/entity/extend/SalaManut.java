package com.adm.ejb.entity.extend;

import java.io.Serializable;
import java.math.BigDecimal;

import com.adm.ejb.entity.TipoSala;
import com.adm.ejb.entity.base.SalaBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;

import java.util.List;

@Entity
@Table(name = "SALA")
public class SalaManut extends SalaBase implements Serializable {

	// Entidade para manutenção da tabela SALA

	@ManyToOne
	@JoinColumn(name = "TIPO_SALA", insertable = false, updatable = false)
	private TipoSala tipoSalaFk;

	/**
	 * Dados da fk da tabela: TIPO_SALA<br>
	 * Tipo: NUMBER Tamanho: 3 Precisão: 0
	 * 
	 * @return TipoSala
	 */
	public TipoSala getTipoSalaFk() {
		return this.tipoSalaFk;
	}

	/**
	 * fk Tabela: TIPO_SALA<br>
	 * Tipo: NUMBER Tamanho: 3 Precisão: 0
	 * 
	 * @param TipoSala
	 */
	public void setTipoSalaFk(TipoSala tipoSalaFk) {
		if (tipoSalaFk == null) {
			setTipoSala(null);
		} else {
			setTipoSala(tipoSalaFk.getTipoSala());
		}
		this.tipoSalaFk = tipoSalaFk;
	}

}
