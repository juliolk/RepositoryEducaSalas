package com.adm.app.resources;

import java.io.File;
import java.text.SimpleDateFormat;

import javax.faces.application.Resource;
import javax.faces.application.ResourceWrapper;

/**
 * Resource customizado para acrescentar no RequestPath
 * a data e hora da ultima modificacao do arquivo para evitar
 * o cache do navegador no client.
 * Importante para arquivos .css e .js que sao modificados
 * para garantir que a versao do servidor esteja rodando
 * no navegador.
 */
public class CustomResource extends ResourceWrapper {

	private Resource resource;

	public CustomResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getWrapped() {
		return this.resource;
	}

	@Override
	public String getRequestPath() {

		if (this.resource == null) {
			return null;
		}

		String requestPath = this.resource.getRequestPath();

		File res = new File(this.resource.getURL().getFile());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String mdt = sdf.format(res.lastModified());

		if (requestPath.contains("?"))
			requestPath = requestPath + "&mdt=" + mdt;
		else
			requestPath = requestPath + "?mdt=" + mdt;

		return requestPath;
	}
}
