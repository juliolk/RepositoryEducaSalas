package com.adm.app.resources;

import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import java.util.Arrays;
import javax.faces.application.Resource;

/**
 * ResourceHandler customizado para que ao entregar recursos
 * de algumas librarys (ex: css, js, img) se utilize de um 
 * Resource customizado que evita o cache do navegador.
 * Recursos de outras librarys serao entregues normalmente.
 * Para uso desta classe e preciso configurar o faces-config.xml
 * indicando o uso deste ResourceHandler customizado.
 * 
 */

public class CustomResourceHandler extends ResourceHandlerWrapper {

	private ResourceHandler wrapped;
	
	public CustomResourceHandler(ResourceHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ResourceHandler getWrapped() {
		return this.wrapped;
	}

	@Override
    public Resource createResource(String resourceName, String libraryName) {
        Resource resource = super.createResource(resourceName, libraryName);
 
        String [] libs = { "css", "js", "img", "lib", "nportal" };
        
        // Só retorna o resource customizado para as libs definidas
        if(Arrays.asList(libs).contains(libraryName)){
            return new CustomResource(resource);
        } else {
        	return resource;
        }
    }
	
}
