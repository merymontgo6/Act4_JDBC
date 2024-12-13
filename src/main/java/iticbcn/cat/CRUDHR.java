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

    //Read sense prepared statements, mostra tots els registres
    /*public void ReadAllDatabase(Connection connection, String TableName) throws ConnectException, SQLException {
        try (Statement statement = connection.createStatement()) {
            
            String query = "SELECT * FROM " + TableName + ";";

            ResultSet rset = statement.executeQuery(query);
            //obtenim numero de columnes i nom
            int colNum = getColumnNames(rset);
            //Si el nombre de columnes és >0 procedim a llegir i mostrar els registres
            if (colNum > 0) {
                recorrerRegistres(rset,colNum);
            }
        }
    }*/

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

    //Opció per veure tots els rols que hi ha a la base de dades
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

    public void recorrerRegistres(ResultSet rs, int ColNum) throws SQLException {
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
}
