package br.com.educa.salas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.educa.salas.model.Usuario;
import br.com.educa.salas.model.Login;
import br.com.educa.salas.service.EducaDb;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@Repository
@RequestMapping("/educa")
@ApiResponses(value = {
		@ApiResponse(responseCode = "500", description = "Foi gerada uma exceção", 
				content = @Content(schema = @Schema(implementation = EducaController.class))) })
public class EducaController {
	
	@Autowired
	private EducaDb service;
	//private Usuario usuario;

	@Operation(summary = "Retorna se a api está operacional.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retorna Sucesso."), })
	@GetMapping(value = "/isAlive", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> isAlive() {

		return ResponseEntity.ok("");
	}

	@SuppressWarnings("unchecked")
	@Operation(summary = "Login.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retorna Sucesso."), })
//	@PostMapping(value = "/cadastraUsuario",params = nome, produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(value = "/Login",method = RequestMethod.POST,  produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> Login(
			@RequestParam(value="nome")     String nome ,
			@RequestParam(value="senha") String senha) throws Exception {
		
		Login login = new Login();
		login.setNome(nome);
		login.setSenha(senha);
				
		return service.Login(login); 
		
	}
}

