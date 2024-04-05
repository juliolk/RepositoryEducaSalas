package com.adm.xcp.listener;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

public class ICPBrasilCertReader {

	public static final Integer TIP_CERT_1_ECNPJ = 1;
	public static final Integer TIP_CERT_2_ECPF = 2;

	private static final DERObjectIdentifier OID_PESSOA_FISICA_DADOS_TITULAR = new ASN1ObjectIdentifier("2.16.76.1.3.1");
	private static final DERObjectIdentifier OID_PESSOA_JURIDICA_RESPONSAVEL = new ASN1ObjectIdentifier("2.16.76.1.3.2");
	private static final DERObjectIdentifier OID_PESSOA_JURIDICA_CNPJ = new ASN1ObjectIdentifier("2.16.76.1.3.3");
	private static final DERObjectIdentifier OID_PESSOA_JURIDICA_DADOS_RESPONSAVEL = new ASN1ObjectIdentifier("2.16.76.1.3.4");
	private static final DERObjectIdentifier OID_PESSOA_FISICA_ELEITORAL = new ASN1ObjectIdentifier("2.16.76.1.3.5");
	private static final DERObjectIdentifier OID_PESSOA_FISICA_INSS = new ASN1ObjectIdentifier("2.16.76.1.3.6");
	private static final DERObjectIdentifier OID_PESSOA_JURIDICA_INSS = new ASN1ObjectIdentifier("2.16.76.1.3.7");
	private static final DERObjectIdentifier OID_PESSOA_JURIDICA_NOME_EMPRESARIAL = new ASN1ObjectIdentifier("2.16.76.1.3.8");

	private String desCnpj;
	private String desCpf;
	private String desRg;
	private String desInfo;
	private Integer tipCert;

	public void read(X509Certificate cert) {
		StringBuilder sb = new StringBuilder();
		try {

			Collection col = X509ExtensionUtil.getSubjectAlternativeNames(cert);

			for (Object obj : col) {

				if (obj instanceof ArrayList) {

					ArrayList lst = (ArrayList) obj;

					Object value = lst.get(1);

					if (value instanceof DLSequence) {

						/**
						 * DER Sequence ObjectIdentifier Tagged DER Octet String
						 */
						DLSequence derSeq = (DLSequence) value;

						DERObjectIdentifier derOid = (DERObjectIdentifier) derSeq.getObjectAt(0);
						DERTaggedObject derTagged = (DERTaggedObject) derSeq.getObjectAt(1);
						String info = null;

						ASN1Primitive derObj = derTagged.getObject();

						if (derObj instanceof DEROctetString) {
							DEROctetString octet = (DEROctetString) derObj;
							info = new String(octet.getOctets());
						} else if (derObj instanceof DERPrintableString) {
							DERPrintableString octet = (DERPrintableString) derObj;
							info = new String(octet.getOctets());
						} else if (derObj instanceof DERUTF8String) {
							DERUTF8String str = (DERUTF8String) derObj;
							info = str.getString();
						}

						if (info != null && !info.isEmpty()) {
							if (derOid.equals(OID_PESSOA_FISICA_DADOS_TITULAR) || derOid.equals(OID_PESSOA_JURIDICA_DADOS_RESPONSAVEL)) {

								if (derOid.equals(OID_PESSOA_FISICA_DADOS_TITULAR)) {
									this.setTipCert(TIP_CERT_2_ECPF);
								} else {
									this.setTipCert(TIP_CERT_1_ECNPJ);
								}

								String nascimento = info.substring(0, 8);

								sb.append("Data Nascimento: " + new SimpleDateFormat("ddMMyyyy").parse(nascimento));
								sb.append("\n");

								this.setDesCpf(info.substring(8, 19));

								sb.append("CPF: " + this.getDesCpf());
								String nis = info.substring(19, 30);
								sb.append("\n");
								sb.append("NIS: " + nis);
								sb.append("\n");

								this.setDesRg(info.substring(30, 45));

								sb.append("RG: " + this.getDesRg());
								sb.append("\n");

								if (!this.getDesRg().equals("000000000000000")) {
									String ufExp = info.substring(45, 50);
									sb.append("Orgão Expedidor: " + ufExp);
									sb.append("\n");
								}
							} else if (derOid.equals(OID_PESSOA_FISICA_INSS)) {
								String inss = info.substring(0, 12);
								sb.append("INSS: " + inss);
								sb.append("\n");
							} else if (derOid.equals(OID_PESSOA_FISICA_ELEITORAL)) {
								String titulo = info.substring(0, 12);
								sb.append("Titulo de Eleitor: " + titulo);
								sb.append("\n");
								String zona = info.substring(12, 15);
								sb.append("Zona Eleitoral: " + zona);
								sb.append("\n");
								String secao = info.substring(15, 19);
								sb.append("Seção: " + secao);
								sb.append("\n");
								if (!titulo.equals("000000000000")) {
									String municipio = info.substring(19, 41);
									sb.append("Municipio: " + municipio);
									sb.append("\n");
								}
							} else if (derOid.equals(OID_PESSOA_JURIDICA_RESPONSAVEL)) {
								sb.append("Responsavél: " + info);
								sb.append("\n");
							} else if (derOid.equals(OID_PESSOA_JURIDICA_CNPJ)) {

								this.setDesCnpj(info);
								sb.append("CNPJ: " + this.getDesCnpj());
								sb.append("\n");
							} else if (derOid.equals(OID_PESSOA_JURIDICA_INSS)) {
								sb.append("INSS: " + info);
								sb.append("\n");
							} else if (derOid.equals(OID_PESSOA_JURIDICA_NOME_EMPRESARIAL)) {
								sb.append("NOME: " + info);
								sb.append("\n");
							}
						}
					} else {
						sb.append("Extra: " + value);
					}
				}
			}
			this.setDesInfo(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getDesCnpj() {
		return this.desCnpj;
	}

	public void setDesCnpj(String desCnpj) {
		this.desCnpj = desCnpj;
	}

	public String getDesCpf() {
		return this.desCpf;
	}

	public void setDesCpf(String desCpf) {
		this.desCpf = desCpf;
	}

	public String getDesRg() {
		return this.desRg;
	}

	public void setDesRg(String desRg) {
		this.desRg = desRg;
	}

	public String getDesInfo() {
		return this.desInfo;
	}

	public void setDesInfo(String desInfo) {
		this.desInfo = desInfo;
	}

	public Integer getTipCert() {
		return this.tipCert;
	}

	public void setTipCert(Integer tipCert) {
		this.tipCert = tipCert;
	}

}