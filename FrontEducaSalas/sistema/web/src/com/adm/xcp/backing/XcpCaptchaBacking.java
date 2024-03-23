package com.adm.xcp.backing;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.gimpy.RippleGimpyRenderer;

import org.primefaces.model.DefaultStreamedContent;

import com.adm.ejb.entity.XcpParametrosSistema;
import com.adm.util.Converter;
import com.adm.util.exceptions.ParametroNaoEncontradoException;

@ManagedBean
@SessionScoped
public class XcpCaptchaBacking extends XcpAbstractBacking {
	private Map<String, Captcha> mapCaptcha = new HashMap<String, Captcha>();

	private XcpParametrosSistema paramCaptcha;
	public static final Integer CAPTCHA_0_SIMPLECAPTCHA = 0;
	public static final Integer CAPTCHA_1_RECAPTCHA = 1;

	public CaptchaValidator getValidator(String key) {
		return new CaptchaValidator(key);
	}

	private Captcha getCaptcha(String key) {
		Captcha captcha = this.mapCaptcha.get(key);
		if (captcha == null) {
			Captcha.Builder builder = new Captcha.Builder(160, 50);
			builder.addText();
			builder.addBackground(new FlatColorBackgroundProducer(Color.WHITE));
			//builder.addNoise();
			builder.gimp(new RippleGimpyRenderer());
			builder.addBorder();
			captcha = builder.build();
			this.mapCaptcha.put(key, captcha);
		}
		return captcha;
	}

	public Map getImgCaptcha() {
		return new HashMap() {
			@Override
			public Object get(Object key) {

				try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
					Captcha captcha = XcpCaptchaBacking.this.getCaptcha((String) key);
					ImageIO.write(captcha.getImage(), "png", bao);

					return new DefaultStreamedContent(new ByteArrayInputStream(bao.toByteArray()), "image/png");

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public void actionTrocarCaptcha(String key) {
		this.mapCaptcha.remove(key);
	}

	public boolean isCorrect(String key, String value) {
		return XcpCaptchaBacking.this.mapCaptcha.get(key).isCorrect(value);
	}

	public class CaptchaValidator {

		private String key;

		public CaptchaValidator(String key) {
			this.key = key;
		}

		// LOGIN
		public void validate(FacesContext context, UIComponent componentToValidate, Object value) throws ValidatorException {

			// clear field
			((HtmlInputText) componentToValidate).setSubmittedValue("");
			((HtmlInputText) componentToValidate).setValue("");

			if (XcpCaptchaBacking.this.mapCaptcha.get(this.key).isCorrect(value.toString())) {
				return;
			}

			FacesMessage message = new FacesMessage(XcpCaptchaBacking.this.getTraducao("msg_captcha_invalido"));
			message.setSeverity(MSG_ERROR);
			XcpCaptchaBacking.this.mapCaptcha.remove(this.key);
			throw new ValidatorException(message);
		}
	}

	public Integer getTipoCaptcha() throws ParametroNaoEncontradoException {

		if (this.paramCaptcha == null) {
			this.paramCaptcha = this.xcpSession.findXcpParametrosSistema(this.getEjbVars(), null, "Captcha", 1);
		}

		if (this.paramCaptcha == null) {
			return CAPTCHA_0_SIMPLECAPTCHA;
		}

		Integer tipoCaptcha = Converter.toInteger(this.paramCaptcha.getVlrParametro());

		if (CAPTCHA_1_RECAPTCHA.equals(tipoCaptcha)) {
			return CAPTCHA_1_RECAPTCHA;
		} else {
			return CAPTCHA_0_SIMPLECAPTCHA;
		}
	}

}
