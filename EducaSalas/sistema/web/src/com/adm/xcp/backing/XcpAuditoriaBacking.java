package com.adm.xcp.backing;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.primefaces.event.TabChangeEvent;

import com.adm.ejb.entity.extend.AuditoriaManut;
import com.adm.util.Converter;
import com.adm.util.MontaQuery;
import com.adm.util.Util;
import com.adm.util.ejb.XcapeEntity;
import com.adm.util.ejb.XcapePK;
import com.adm.xcp.vos.XcpAuditEntityVO;
import com.adm.xcp.vos.XcpAuditoriaTabVO;
import com.adm.xcp.vos.XcpAuditoriaTableVO;

@ManagedBean
@ViewScoped
public class XcpAuditoriaBacking extends XcpAbstractBacking {

	private List<XcpAuditoriaTabVO> list;
	private List<XcpAuditEntityVO> letts;
	private Integer activeIndex;
	private Map<String, Boolean> mapTab = new HashMap<String, Boolean>();

	public void actionAuditoria(XcpAbstractBacking back) throws Exception {
		this.setActiveIndex(0);
		this.setList(new ArrayList<XcpAuditoriaTabVO>());

		String desTituloPrinc = this.getTraducao("title_consultalog");

		if (back instanceof XcpManutencaoBacking) {
			this.getList().add(this.auditoria(((XcpManutencaoBacking) back).getEntity(), desTituloPrinc, true));
		}

		this.letts = back.getAuditEntities();
		if (this.letts != null) {
			for (XcpAuditEntityVO a : this.letts) {
				String desTitulo = a.getDesLabel();
				List<XcapeEntity> listett = (List<XcapeEntity>) a.getList();
				for (XcapeEntity e : listett) {
					XcpAuditoriaTabVO tab = this.auditoria(e, desTituloPrinc.concat(" - ").concat(desTitulo), a.isLoad());
					if (!this.getList().contains(tab)) {
						this.getList().add(tab);
					}
				}
			}
		}

		getRequestContext().execute("PF('w_dlgAudit').show()");
		getRequestContext().update("formAudit");
	}

	private String getDesTabela(XcapeEntity entity) {
		Table tann = entity.getClass().getAnnotation(Table.class);
		return tann.name().toUpperCase();
	}

	private XcpAuditoriaTabVO auditoria(XcapeEntity entity, String desTitulo, boolean load) throws Exception {
		if (entity == null) {
			return null;
		}

		String desTabela = this.getDesTabela(entity);
		//Quando abre a dialog com tabelas filhas, nao executa a query, somente 
		if (!load) {
			XcpAuditoriaTabVO vo = new XcpAuditoriaTabVO();
			vo.setDesTitulo(desTitulo);
			vo.setDesTabela(desTabela);
			return vo;
		}

		Map<String, String> mapPk = new HashMap<String, String>();

		if (entity.getPK() instanceof XcapePK) {
			XcapePK pk = (XcapePK) entity.getPK();
			List<Field> fields = this.getFields(pk);
			for (Field field : fields) {
				Column ann = field.getAnnotation(Column.class);
				if (ann != null) {
					mapPk.put(ann.name(), this.montaPk(field, pk));
				}
			}

		} else {
			List<Field> fields = this.getFields(entity);
			for (Field field : fields) {
				Column ann = field.getAnnotation(Column.class);
				Id iann = field.getAnnotation(Id.class);
				if (ann != null && iann != null) {
					mapPk.put(ann.name(), this.montaPk(field, entity));
				}
			}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("desTabela", desTabela);

		List<String> list = this.xcpQuerySession.executeNativeQueryList(
				this.getEjbVars(),
				"SELECT DES_COLUNA FROM VIEW_AUDITORIA_PK WHERE DES_TABELA = :desTabela ORDER BY NUM_ORDEM",
				map);

		ArrayList<String> listReg = new ArrayList<String>();

		for (String coluna : list) {
			String value = mapPk.get(coluna);
			if (!Util.isEmpty(value)) {
				listReg.add(value);
			} else {
				listReg.add(coluna + "=;");
			}
		}

		StringBuilder sbfinal = new StringBuilder();
		sbfinal.append("Select e from AuditoriaManut e");
		sbfinal.append(" where (e.registro = '");

		for (String string : listReg) {
			sbfinal.append(string);
			sbfinal.append("''");
		}

		sbfinal.setLength(sbfinal.length() - 2);
		sbfinal.append("'");
		sbfinal.append(" or ");
		sbfinal.append(" (campo = ");
		sbfinal.append("'");
		sbfinal.append(desTabela);
		sbfinal.append(".INCLUSAO");
		sbfinal.append("'");

		for (String string : listReg) {
			sbfinal.append(" and e.registro like '%");
			sbfinal.append(string);
			sbfinal.append("%'");
		}

		sbfinal.append("))");

		sbfinal.append(" and campo like '");
		sbfinal.append(desTabela);
		sbfinal.append(".%'");
		sbfinal.append(" order by e.numlog desc ");

		List<AuditoriaManut> lista = this.xcpQuerySession.executeQueryList(this.getEjbVars(), sbfinal.toString(), new HashMap<String, Object>());
		for (AuditoriaManut obj : lista) {
			String campo = obj.getCampo();
			campo = campo.substring(campo.indexOf(".") + 1, campo.length());
			obj.setCampo(campo);
		}

		XcpAuditoriaTabVO vo = new XcpAuditoriaTabVO();
		vo.setDesTitulo(desTitulo);
		vo.setDesTabela(desTabela);

		String description = entity.getDescription();
		Object value = Util.getValue(entity, description);
		if (!Util.isEmpty(lista)) {
			vo.setLista(new ArrayList<XcpAuditoriaTableVO>());
			vo.getLista().add(new XcpAuditoriaTableVO(Converter.toString(value), lista));
		}
		return vo;
	}

	private <T> List<Field> getFields(T t) {
		List<Field> fields = new ArrayList<>();
		Class clazz = t.getClass();
		while (clazz != Object.class) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	private String montaPk(Field field, Object pk) throws Exception {
		field.setAccessible(true);

		StringBuilder sb = new StringBuilder();
		sb.append(field.getName().toUpperCase());
		sb.append("=");

		if (field.getType().equals(Date.class)) {
			Temporal t = field.getAnnotation(Temporal.class);
			SimpleDateFormat sdf = null;
			if (t != null && TemporalType.TIMESTAMP == t.value()) {
				sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			} else {
				sdf = new SimpleDateFormat("dd/MM/yyyy");
			}
			sb.append(sdf.format(field.get(pk)));
		} else {
			sb.append(field.get(pk));
		}
		sb.append(";");
		return sb.toString();
	}

	public List<XcpAuditoriaTabVO> getList() {
		return this.list;
	}

	public void setList(List<XcpAuditoriaTabVO> list) {
		this.list = list;
	}

	public void onTabChange(TabChangeEvent event) throws Exception {

		if (event.getTab() == null) {
			return;
		}

		String title = event.getTab().getTitle();

		boolean atualizou = false;
		for (XcpAuditoriaTabVO tab : this.getList()) {
			if (!tab.getDesTitulo().equals(title)) {
				continue;
			}

			if (tab.getLista() != null) {
				//se ja carregou nao carrega denovo
				continue;
			}
			atualizou = true;
			for (XcpAuditEntityVO vo : this.letts) {
				String desTitulo = vo.getDesLabel();
				for (Object e : vo.getList()) {
					String desTabela = this.getDesTabela((XcapeEntity) e);

					if (Objects.equals(tab.getDesTabela(), desTabela)) {

						int indexOf = this.getList().indexOf(tab);
						XcpAuditoriaTabVO tabvo = this.getList().get(indexOf);

						if (tabvo.getLista() == null) {
							tabvo.setLista(new ArrayList<XcpAuditoriaTableVO>());
						}

						XcpAuditoriaTabVO t = this.auditoria((XcapeEntity) e, desTitulo, true);
						if (t.getLista() != null) {

							tabvo.getLista().addAll(t.getLista());
						}
					}
				}
			}
		}

		if (atualizou) {
			getRequestContext().update(":formAudit:tabView");
		}
	}

	public AuditoriaManut getDetalhesRow() {
		return this.getList().get(this.getActiveIndex()).getSelectedRow();
	}

	public boolean isAtivo(XcapeEntity ett) {

		if (ett == null) {
			return false;
		}

		boolean acessivel = this.getSession().isAcessivel("/secure/ConsultaAuditoriaForm.xhtml", null);
		if (!acessivel) {
			return false;
		}

		String desTabela = this.getDesTabela(ett);

		if (!this.mapTab.containsKey(desTabela)) {
			MontaQuery q = new MontaQuery("Select e from AuditoriaTrigger e");
			q.addWhere("tabela", "like", desTabela);
			this.mapTab.put(desTabela, !Util.isEmpty(this.xcpQuerySession.executeQueryList(this.getEjbVars(), q)));
		}

		return this.mapTab.get(desTabela);
	}

	public Integer getActiveIndex() {
		return this.activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}

}
