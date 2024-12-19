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
import java.util.Scanner;

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
    public void readRols(Connection connection) throws ConnectException, SQLException {
        String query = "SELECT * FROM Rol";
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            ResultSet rset = prepstat.executeQuery();
            int colNum = getColumnNames(rset);
            if (colNum > 0) {
                recorrerRegistres(rset, colNum);
            }
        }
    }

    //Opció per veure tots els rols per el seu id que hi ha a la base de dades
    public void readRolById(Connection connection, String TableName, int id) throws ConnectException, SQLException{
        String query = "SELECT * FROM "+ TableName + "WHERE Id = ?";
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            prepstat.setInt(1, id);
            ResultSet rset = prepstat.executeQuery();
            int colNum = getColumnNames(rset);
            if (colNum > 0){
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
    public static void readRolsby10 (Connection connection) throws SQLException {
        String query = "SELECT * FROM Rol ORDER BY rolId ASC LIMIT 10 OFFSET 0";
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            ResultSet rset= prepstat.executeQuery();
            int colNum = getColumnNames(rset);
            if (colNum > 0 ) {
                recorrerRegistres(rset, colNum);
            }
        }
    }

    //Opció per inserir un rol a la base de dades
    public void inserirRol(Connection connection, int rolId, String nom) throws SQLException{
        String query = "INSERT INTO Rol (rolId, nom) VALUES (?,?)";
        try (PreparedStatement prepstat = connection.prepareStatement(query)) {
            prepstat.setInt(1, rolId);
            prepstat.setString(2, nom);
            int rowsAffected = prepstat.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Rol inserit correctament.");
            } else {
                System.out.println("No s'ha pogut inserir el rol.");
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
    public void modificarRol(Connection connection, int rolId) throws SQLException {
    // Consulta per obtenir el registre actual
    String selectQuery = "SELECT * FROM Rol WHERE rolId = ?";
    // Consulta per actualitzar el registre
    String updateQuery = "UPDATE Rol SET nom = ? WHERE rolId = ?";

    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
         PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

        // Establir el rolId per obtenir el registre
        selectStmt.setInt(1, rolId);
        ResultSet rs = selectStmt.executeQuery();

        // Comprovar si el registre existeix
        if (rs.next()) {
            // Mostrar l'actual valor del camp
            String nomActual = rs.getString("nom");
            System.out.println("Nom actual: " + nomActual);

            // Preguntar per l'usuari el nou valor
            Scanner scanner = new Scanner(System.in);
            System.out.print("Introdueix el nou nom: ");
            String nouNom = scanner.nextLine();

            // Preparar i executar la consulta d'actualització
            updateStmt.setString(1, nouNom);
            updateStmt.setInt(2, rolId);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("El camp s'ha actualitzat correctament.");
            } else {
                System.out.println("No s'ha pogut actualitzar el registre.");
            }
        } else {
            System.out.println("No existeix cap registre amb rolId = " + rolId);
        }}
    }

    //Opció per esborrar registre de taula per id
    public static void esborrarRol(Connection connection, int rolId) throws SQLException {
        String query = "DELETE FROM Rol WHERE rolId = ?";
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
}