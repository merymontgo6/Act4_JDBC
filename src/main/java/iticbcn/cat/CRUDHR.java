package iticbcn.cat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class CRUDHR {
    //crea i modifica info
    public boolean CreateDatabase(Connection connection, InputStream input) 
    throws IOException, ConnectException, SQLException {

        boolean dupRecord = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder sqlStatement = new StringBuilder();
            String line;

            try (Statement statement = connection.createStatement()) {
                while ((line = br.readLine()) != null) {
                    line = line.trim(); // Ignorar comentaris i línies buides
                        
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("/*")) {
                            continue;
                    }
                    sqlStatement.append(line); // Acumular la línea al buffer
                    
                    if (line.endsWith(";")) { // el caràcter ";" es considera terminació de sentència SQL
                        String sql = sqlStatement.toString().replace(";", "").trim();// Eliminar el ";" i executar la instrucción
                        statement.execute(sql);
                        sqlStatement.setLength(0);// Reiniciar el buffer para la siguiente instrucción
                    }
                }
            } catch (SQLException sqle) {
                if (!sqle.getMessage().contains("Duplicate entry")) {
                    System.err.println(sqle.getMessage());
                } else {
                    dupRecord = true;
                    br.readLine();
                }
            }
        }
        return dupRecord;
    }

    //Opció per llegir tots els rols que hi ha a la base de dades
    public void readRols(Connection connection, String TableName) throws ConnectException, SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM " + TableName + ";";
            ResultSet rset = statement.executeQuery(query);
            int colNum = getColumnNames(rset);
            if (colNum > 0) {
                recorrerRegistres(rset, colNum);
            }
        }
    }

    //Opció per veure tots els rols per el seu id que hi ha a la base de dades
    public void readRolById(Connection connection, String TableName, int id) throws ConnectException, SQLException { 
        String query = "SELECT * FROM " + TableName + " WHERE ROLD_ID = ?";  // Canvia rolId a ROLD_ID
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            prepstat.setInt(1, id);
            ResultSet rset = prepstat.executeQuery();
            int colNum = getColumnNames(rset);
            if (colNum > 0) {
                recorrerRegistres(rset, colNum);
            }
        }
    }        

    public static void recorrerRegistres(ResultSet rs, int ColNum) throws SQLException {
        while(rs.next()) {
            for(int i =0; i<ColNum; i++) {
                if(i+1 == ColNum) {
                    System.out.println(rs.getString(i+1));
                } else {
            
                System.out.print(rs.getString(i+1)+ ", ");
                }
            } 
        }   
    }

    //Opció per llegir de 10 en 10 els registres de la taula
    public void readRolsby10 (Connection connection, String TableName) throws SQLException {
        int offset = 0;
        boolean mesValors = true;

        while (mesValors){
            String query = "SELECT * FROM " + TableName + " ORDER BY rolId ASC LIMIT 10 OFFSET " + offset;
            try (PreparedStatement prepstat = connection.prepareStatement(query)) {
                ResultSet rset= prepstat.executeQuery();

                if (rset.next()) { //comprova si hi ha regustres
                    do { 
                        int colNum = getColumnNames(rset);
                        recorrerRegistres(rset, colNum);
                    } while (rset.next());
                    offset += 10; //afegeix 10 mes
                    } else {
                    mesValors = false;
                }
            }
        }
    }

    //Opció per inserir un rol a la base de dades
    public void inserirRol(Connection connection, String TableName, String nom) throws SQLException {
        String query = "INSERT INTO " + TableName + "(NOM) VALUES (?)";
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            prepstat.setString(1, nom);
            int rowsAffected = prepstat.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Dada inserida correctament a la taula: " + TableName);
            } else {
                System.out.println("No s'ha pogut inserir la dada a la taula: " + TableName);
            }
        }
    }
    
    //Aquest mètode auxiliar podria ser utileria del READ, mostra el nom de les columnes i quantes n'hi ha
    public static int getColumnNames(ResultSet rs) throws SQLException {
        
        int numberOfColumns = 0;
        if (rs != null) {   
            ResultSetMetaData rsMetaData = rs.getMetaData();
            numberOfColumns = rsMetaData.getColumnCount();   
        
            for (int i = 1; i < numberOfColumns + 1; i++) {  
                String columnName = rsMetaData.getColumnName(i);
                System.out.print(columnName + ", ");
            }
        }
        System.out.println();
        return numberOfColumns;
    }
    
    //Opció per modificar els camps de la taula Rol
    public void modificarRol(Connection connection, String TableName, int rolId, BufferedReader br) throws SQLException, IOException {
        // Consulta per obtenir el registre actual
        String selectQuery = "SELECT * FROM " + TableName + " WHERE rolId = ?";
        // Consulta per actualitzar el registre
        String updateQuery = "UPDATE " + TableName + " SET nom = ? WHERE rolId = ?";
    
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
    
            // Establir el rolId per obtenir el registre
            selectStmt.setInt(1, rolId);
            ResultSet rs = selectStmt.executeQuery();
    
            // Comprovar si el registre existeix
            if (rs.next()) {
                // Mostrar l'actual valor del camp 'nom'
                String nomActual = rs.getString("nom");
                System.out.println("Nom actual: " + nomActual);
    
                // Demanar per l'usuari el nou valor mitjançant BufferedReader
                System.out.print("Introdueix el nou nom: ");
                String nouNom = br.readLine();  // Llegeix el valor introduït per l'usuari
    
                // Preparar i executar la consulta d'actualització
                updateStmt.setString(1, nouNom);
                updateStmt.setInt(2, rolId);
                int rowsAffected = updateStmt.executeUpdate();
    
                // Comprovar si l'actualització va tenir èxit
                if (rowsAffected > 0) {
                    System.out.println("El camp s'ha actualitzat correctament.");
                } else {
                    System.out.println("No s'ha pogut actualitzar el registre.");
                }
            } else {
                System.out.println("No existeix cap registre amb rolId = " + rolId);
            }
        }
    }
    
    //Opció per esborrar registre de taula per id
    public void esborrarRol(Connection connection, String TableName, int rolId) throws SQLException {
        String query = "DELETE FROM " + TableName + " WHERE rolId = ?";
        try (PreparedStatement prepstat = connection.prepareStatement(query)){
            prepstat.setInt(1, rolId);
            int rowsAffected = prepstat.executeUpdate();
    
            // Confirmació
            if (rowsAffected > 0) {
                System.out.println("El registre amb rolId = " + rolId + " s'ha esborrat correctament.");
            } else {
                System.out.println("No s'ha trobat cap registre amb rolId = " + rolId + ".");
            }
        }
    }    

    //Opció de cerca per LIKE
    public void readRolsByLike(Connection connection, String tableName, String fieldName, String searchValue) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE " + fieldName + " LIKE ?";
        
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            // Afegim el valor de la cerca amb '%' per a la cerca parcial
            prepstat.setString(1, "%" + searchValue + "%");
            
            ResultSet rset = prepstat.executeQuery();
            
            if (rset.next()) {
                int colNum = getColumnNames(rset);
                recorrerRegistres(rset, colNum);    // mostrar els registres
            } else {
                System.out.println("No s'han trobat registres amb aquest valor.");
            }
        }
    }    
    
}