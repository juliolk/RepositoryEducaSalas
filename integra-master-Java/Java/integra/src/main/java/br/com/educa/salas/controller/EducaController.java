package br.com.educa.salas.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@Operation(summary = "Retorna se a api está operacional.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retorna Sucesso."), })
	@GetMapping(value = "/isAlive", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> isAlive() {

		return ResponseEntity.ok("");
	}

	@Operation(summary = "Retorna consulta de teste.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retorna Sucesso."), })
	@GetMapping(value = "/retorna", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> Consulta() throws SQLException {
		
		return ResponseEntity.ok(service.Consulta());
	}
}

