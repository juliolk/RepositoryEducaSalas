package br.com.educa.salas.service;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import br.com.educa.salas.model.Usuario;
import br.com.educa.salas.model.Login;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.internal.OraclePreparedStatement;
import oracle.jdbc.internal.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;

@Configuration
public class EducaDb {
	
	private String vTexto;
	private ResponseEntity validaLogin;
	
	@Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    @Value("${spring.datasource.driver-class-name}")
    private String dbClassName;
	
	@Bean
	public DataSource dataSource() throws SQLException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(dbClassName);
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUsername);
		dataSource.setPassword(dbPassword);
		
		try (CallableStatement cs = dataSource.getConnection().prepareCall("ALTER SESSION SET NLS_NUMERIC_CHARACTERS = '.,'");){			
			cs.executeUpdate();
		}
	
		try (CallableStatement cs = dataSource.getConnection().prepareCall("ALTER SESSION SET NLS_DATE_FORMAT = 'DD/MM/YYYY'");){			
			cs.executeUpdate();
		}
		
		return dataSource;
	}
		

//	private DataSource dataSource;

//	
//	@Autowired
//	public void setDataSource(OracleDataSource dataSource) throws SQLException {
//		System.out.println("teste jdbc");
//		this.dataSource = dataSource;
//		System.out.println(dataSource.getDatabaseName());
//		System.out.println(dataSource.getDriverType());
//		System.out.println(dataSource.getServiceName());
//		connection = dataSource.getConnection();
//		

//		try (CallableStatement cs = connection.prepareCall("CALL SYS.DBMS_APPLICATION_INFO.SET_MODULE('RBLUESERVER','''')");){			
//			cs.executeUpdate();
//		};
		
//		try (CallableStatement cs = connection.prepareCall("ALTER SESSION SET NLS_NUMERIC_CHARACTERS = '.,'");){			
//			cs.executeUpdate();
//		}
//		
//		try (CallableStatement cs = connection.prepareCall("ALTER SESSION SET NLS_DATE_FORMAT = 'DD/MM/YYYY'");){			
//			cs.executeUpdate();
//		}
		
//	}
	
	@SuppressWarnings("rawtypes")
	public ResponseEntity Login(Login login) throws SQLException, Exception {

		SimpleJdbcCall call = 
				 new SimpleJdbcCall(dataSource())
				 .withSchemaName("EDUCACAO")
				 .withCatalogName("PKG_USUARIO")
				 .withProcedureName("VALIDA_LOGIN")				 
              .declareParameters(
               new SqlOutParameter("PRETORNO", Types.VARCHAR));
		
		System.out.println(login.toString());
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("PNOME",     login.getNome());
		parametros.put("PSENHA", login.getSenha());

		try {
			vTexto = call.executeObject(String.class, parametros);
			System.out.println(vTexto);
			
			if (vTexto != null) {
				validaLogin = ResponseEntity.ok("Bem vindo "+vTexto);
			}else {
				validaLogin = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos");;
			}
				
			return validaLogin;
			
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}  
	}
	
	
}
