
package com.adm.ejb.entity.extend;

import java.io.Serializable;
import java.math.BigDecimal;

import com.adm.ejb.entity.base.TipoSalaBase;

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
@Table(name = "TIPO_SALA")
public class TipoSalaManut extends TipoSalaBase implements Serializable {
	
	//Entidade para manutenção da tabela TIPO_SALA
	
		



  
  

}
