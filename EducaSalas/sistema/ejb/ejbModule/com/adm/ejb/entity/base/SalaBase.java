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
public class SalaBase implements Serializable, XcapeEntity {

	@Transient
	private boolean novo = true;

	@Id
	@GeneratedValue(generator = "SALA_SEQ")
	@SequenceGenerator(name = "SALA_SEQ", sequenceName = "SALA_SEQ", allocationSize = 1)
	@Column(name = "ID")
	private Integer id;

	@Column(name = "NOME")
	private String nome;

	@Column(name = "TIPO_SALA")
	private Integer tipoSala;

	@Column(name = "LOCAL")
	private String local;

	/**
	 * C�digo <br>
	 * Tipo: NUMBER Tamanho: 6 Precis�o: 0
	 * 
	 * @return C�digo
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * C�digo <br>
	 * Tipo: NUMBER Tamanho: 6 Precis�o: 0
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Nome <br>
	 * Tipo: VARCHAR2 Tamanho: 50 Precis�o: 0
	 * 
	 * @return Nome
	 */
	public String getNome() {
		return this.nome;
	}

	/**
	 * Nome <br>
	 * Tipo: VARCHAR2 Tamanho: 50 Precis�o: 0
	 * 
	 * @param nome
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * FK TIPO_SALA <br>
	 * Tipo: NUMBER Tamanho: 3 Precis�o: 0
	 * 
	 * @return FK TIPO_SALA
	 */
	public Integer getTipoSala() {
		return this.tipoSala;
	}

	/**
	 * FK TIPO_SALA <br>
	 * Tipo: NUMBER Tamanho: 3 Precis�o: 0
	 * 
	 * @param tipoSala
	 */
	public void setTipoSala(Integer tipoSala) {
		this.tipoSala = tipoSala;
	}

	/**
	 * Localiza��o da Sala <br>
	 * Tipo: VARCHAR2 Tamanho: 100 Precis�o: 0
	 * 
	 * @return Localiza��o da Sala
	 */
	public String getLocal() {
		return this.local;
	}

	/**
	 * Localiza��o da Sala <br>
	 * Tipo: VARCHAR2 Tamanho: 100 Precis�o: 0
	 * 
	 * @param local
	 */
	public void setLocal(String local) {
		this.local = local;
	}

	/**
	 * Retorna a PK da entidade Sala <br>
	 * Formada por: <br>
	 * <ul>
	 * <li>getId()</li>
	 * </ul>
	 * 
	 * @return a PK da entidade Sala
	 */
	public Integer getPK() {
		return this.getId();
	}

	/**
	 * Seta a PK da entidade Sala<br>
	 * Integer
	 * 
	 * @param Id
	 */
	public void setPK(Object pk) {
		if (pk == null) {
			this.novo = true;
		}
		this.setId((Integer) pk);
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
		return "id";
	}

	/**
	 * Devolve o nome do campo que representa a descri��o no retorno da lov
	 * 
	 * @return nome do campo da descri��o
	 */
	public String getDescription() {
		return "nome";
	}
}
