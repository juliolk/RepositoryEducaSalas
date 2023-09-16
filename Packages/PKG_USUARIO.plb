CREATE OR REPLACE PACKAGE BODY PKG_USUARIO IS

    PROCEDURE CRIAR_USUARIO (
        PNOME     USUARIO.NOME%TYPE,
        PENDERECO USUARIO.ENDERECO%TYPE,
        PTELEFONE USUARIO.TELEFONE%TYPE,
        PEMAIL    USUARIO.EMAIL%TYPE,
        PGESTOR   USUARIO.GESTOR%TYPE,
        PRETORNO  OUT VARCHAR2)
    IS
        CVERIFICA VARCHAR2(50);
    BEGIN
    
        CVERIFICA := VERIFICA_USUARIO_CADASTRADO(PEMAIL);
        
        IF CVERIFICA IS NULL THEN
        
            INSERT INTO USUARIO (
                ID,
                NOME,
                ENDERECO,
                TELEFONE,
                EMAIL,
                GESTOR )
            VALUES (
                SQID_USUARIO.NEXTVAL,
                PNOME,
                PENDERECO,
                PTELEFONE,
                PEMAIL,
                PGESTOR );
                
            PRETORNO := 'Usuário cadastrado com sucesso.';    
                
        ELSE       
            PRETORNO := 'Usuário já cadastrado. Nome: '||CVERIFICA; 
        END IF;

    END;
    
    FUNCTION VERIFICA_USUARIO_CADASTRADO (
        PEMAIL USUARIO.EMAIL%TYPE ) RETURN VARCHAR2
    IS
        CNOME_USUARIO VARCHAR2(50);
    BEGIN
        CNOME_USUARIO := NULL;
        FOR R IN 
            (
                SELECT USU.NOME
                FROM USUARIO USU
                WHERE
                    USU.EMAIL = PEMAIL
            )
        LOOP
            CNOME_USUARIO := R.NOME;
        END LOOP;
        
       RETURN CNOME_USUARIO;     
        
    END;
    
END;
/