package br.com.educa.salas.service;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import oracle.jdbc.OracleTypes;
import oracle.jdbc.internal.OraclePreparedStatement;
import oracle.jdbc.internal.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;

@Configuration
public class EducaDb {
	
	private String vTexto;
	
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
	
	public String Consulta() throws SQLException {
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT           "); 
			sql.append("   TEXTO         ");
			sql.append("FROM TESTE_JULIO ");	
			try (OraclePreparedStatement statement = (OraclePreparedStatement) dataSource().getConnection()
					.prepareStatement(sql.toString());) {
				try (OracleResultSet rs = (OracleResultSet) statement.executeQuery();) {
					while (rs.next()) {
						vTexto = rs.getString("TEXTO");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vTexto;
		
	}
	
}
