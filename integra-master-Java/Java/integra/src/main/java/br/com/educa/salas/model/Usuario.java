package br.com.educa.salas.model;


public class Usuario {
	
    String nome;
   	String endereco;                                                                                                                                                        
   	String telefone;
    String email;
    String gestor;
     
public String getEndereco() {
	return endereco;
}
public void setEndereco(String endereco) {
	this.endereco = endereco;
}
public String getNome() {
	return nome;
}
public void setNome(String nome) {
	this.nome = nome;
}
public String getTelefone() {
	return telefone;
}
public void setTelefone(String telefone) {
	this.telefone = telefone;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public String getGestor() {
	return gestor;
}
public void setGestor(String gestor) {
	this.gestor = gestor;
}
	
}