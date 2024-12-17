package iticbcn.cat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class GestioDBHR {
    static boolean sortirapp = false;
        
    public static void main(String[] args) {
    
            try (BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in))) {
                try {
                    Properties properties = new Properties(); // Carregar propietats des de l'arxiu
                    try (InputStream input = GestioDBHR.class.getClassLoader().getResourceAsStream("config.properties")) {
                        properties.load(input);
        
                        // Obtenir les credencials com a part del fitxer de propietats
                        String dbUrl = properties.getProperty("db.url");
                        String dbUser = properties.getProperty("db.username");
                        String dbPassword = properties.getProperty("db.password");
        
                        // Conectar amb MariaDB
                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                            System.out.println("Conexió exitosa");
        
                            String File_create_script = "db_scripts/DB_Schema_HR.sql" ;
        
                            InputStream input_sch = GestioDBHR.class.getClassLoader().getResourceAsStream(File_create_script);
        
                            CRUDHR crudbhr = new CRUDHR();
                            //Aquí farem la creació de la database i de les taules, a més d'inserir dades
                            crudbhr.CreateDatabase(connection,input_sch);
                            while (sortirapp == false) {
                                MenuOptions(br1,crudbhr,connection);
                            }
                    } catch (Exception e) {
                        System.err.println("Error al conectar: " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Error al carregar fitxer de propietats: " + e.getMessage());
                }
            } finally {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void MenuOptions(BufferedReader br, CRUDHR crudbhr, Connection connection) 
    throws NumberFormatException, IOException, SQLException, InterruptedException {

        Terminal terminal = TerminalBuilder.builder().system(true).build();

        String message = "";

        message = "OPCIONS";
        printScreen(terminal, message);

        message = "1. CARREGAR TAULA";
        printScreen(terminal, message);

        message = "2. CONSULTAR TOTES LES DADES";
        printScreen(terminal, message);

        message = "3. CONSULTAR PER ID";
        printScreen(terminal, message);

        message = "0. SORTIR";
        printScreen(terminal, message);


        message = "Opció: ";
        for (char c : message.toCharArray()) {
            terminal.writer().print(c);
            terminal.flush();
            Thread.sleep(10);
        }

        int opcio = Integer.parseInt(br.readLine());

        switch(opcio) {
            case 1:
                String File_data_script = "db_scripts/DB_Data_HR.sql" ;
    
                InputStream input_data = GestioDBHR.class.getClassLoader().getResourceAsStream(File_data_script);

                if (crudbhr.CreateDatabase(connection,input_data) == true) {
                    System.out.println("Registres duplicats");
                } else {
                    System.out.println("Registres inserits amb éxit");
                }

                break;
            case 2:
                crudbhr.readRols(connection);
                break;

            case 3:
                crudbhr.readRolById(connection, message, opcio);
                break;

            case 9:
                sortirapp = true;
                break;

            default:
                System.out.print("Opcio no vàlida");
                MenuOptions(br,crudbhr,connection);
        }
    }

    private static void printScreen(Terminal terminal, String message) throws InterruptedException {
        for (char c : message.toCharArray()) {
            terminal.writer().print(c);
            terminal.flush();
            Thread.sleep(10);
        }
        System.out.println();
    }
}