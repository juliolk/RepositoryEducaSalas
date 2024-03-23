package com.adm.ejb.entity.base;

import com.adm.util.ejb.XcapeEntity;
import com.adm.ejb.entity.pk.*;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class TipoSalaBase implements Serializable, XcapeEntity {

	@Transient
	private boolean novo = true;

	@Id
	@GeneratedValue(generator = "TIPO_SALA_SEQ")
	@SequenceGenerator(name = "TIPO_SALA_SEQ", sequenceName = "TIPO_SALA_SEQ", allocationSize = 1)
	@Column(name = "TIPO_SALA")
	private Integer tipoSala;

	@Column(name = "DESCRICAO")
	private String descricao;

	/**
	 * C�digo <br>
	 * Tipo: NUMBER Tamanho: 3 Precis�o: 0
	 * 
	 * @return C�digo
	 */
	public Integer getTipoSala() {
		return this.tipoSala;
	}

	/**
	 * C�digo <br>
	 * Tipo: NUMBER Tamanho: 3 Precis�o: 0
	 * 
	 * @param tipoSala
	 */
	public void setTipoSala(Integer tipoSala) {
		this.tipoSala = tipoSala;
	}

	/**
	 * Descri��o <br>
	 * Tipo: VARCHAR2 Tamanho: 20 Precis�o: 0
	 * 
	 * @return Descri��o
	 */
	public String getDescricao() {
		return this.descricao;
	}

	/**
	 * Descri��o <br>
	 * Tipo: VARCHAR2 Tamanho: 20 Precis�o: 0
	 * 
	 * @param descricao
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna a PK da entidade TipoSala <br>
	 * Formada por: <br>
	 * <ul>
	 * <li>getTipoSala()</li>
	 * </ul>
	 * 
	 * @return a PK da entidade TipoSala
	 */
	public Integer getPK() {
		return this.getTipoSala();
	}

	/**
	 * Seta a PK da entidade TipoSala<br>
	 * Integer
	 * 
	 * @param TipoSala
	 */
	public void setPK(Object pk) {
		if (pk == null) {
			this.novo = true;
		}
		this.setTipoSala((Integer) pk);
	}

	/**
	 * M�todo executado quando � feita a carga da entidade atrav�s do banco <br>
	 * e seta o atributo novo como falso
	 */
	@Override
	@PostLoad
	@PostPersist
	@PostUpdate
	public void load() {
		this.novo = false;
	}

	/**
	 * M�todo que indica se a entidade foi instanciada sem ser pelo EJB
	 * 
	 * @return se a entidade � nova ou se j� existe no banco de dados
	 */
	public boolean isNovo() {
		return this.novo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		XcapeEntity other = (XcapeEntity) obj;
		if (this.getPK() == null) {
			return other.getPK() == null;
		} else {
			return this.getPK().equals(other.getPK());
		}
	}

	/**
	 * Devolve o nome do campo que representa o c�digo que ser� apresentado no
	 * retorno da lov
	 * 
	 * @return nome do campo do c�digo
	 */
	public String getCode() {
		return "tipoSala";
	}

	/**
	 * Devolve o nome do campo que representa a descri��o no retorno da lov
	 * 
	 * @return nome do campo da descri��o
	 */
	public String getDescription() {
		return "descricao";
	}
}
