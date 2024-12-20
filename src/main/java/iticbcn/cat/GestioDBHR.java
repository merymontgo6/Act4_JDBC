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
    static boolean DispOptions = true;

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
        
                            String File_create_script = "db_scripts/DB_Schema_Rols.sql" ;
        
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

        message = "==================";
        printScreen(terminal, message);

        message = "OPCIONS";
        printScreen(terminal, message);

        message = "1. CARREGAR TAULA";
        printScreen(terminal, message);

        message = "2. CONSULTAR TOTES LES DADES";
        printScreen(terminal, message);

        message = "3. CONSULTAR PER ID";
        printScreen(terminal, message);

        message = "4. INSERIR VALORS";
        printScreen(terminal, message);

        message = "5. MOSTRAR VALORS 10";
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
                String File_data_script = "db_scripts/DB_Data_Rols.sql" ;
    
                InputStream input_data = GestioDBHR.class.getClassLoader().getResourceAsStream(File_data_script);

                if (crudbhr.CreateDatabase(connection,input_data) == true) {
                    System.out.println("Registres duplicats");
                } else {
                    System.out.println("Registres inserits amb éxit");
                }

                break;
            case 2:
                crudbhr.readRols(connection, "ROL");
                break;

            case 3:
                int rolId = demanarId(br);
                crudbhr.readRolById(connection, "ROL", rolId);
                break;

            case 4:
                String nom = demanarRolValors(br);
                crudbhr.inserirRol(connection, "ROL", nom);
                break;

            case 5:
                crudbhr.readRolsby10(connection, "ROL");
                break;
            
            case 6:
                // Demanem els valors per a la cerca amb LIKE
                String[] valorsLike = demanarValorsCercaLike(br);
                String fieldNameLike = valorsLike[0];  // Nom del camp per cercar
                String searchValueLike = valorsLike[1];  // Valor de cerca amb LIKE
            
                // Consulta amb LIKE
                crudbhr.readRolsByLike(connection, "ROL", fieldNameLike, searchValueLike);
                break;

            case 7:
                rolId = demanarId(br);
                crudbhr.esborrarRol(connection, "ROL", rolId);
                break;
            
            case 8:
                rolId = demanarId(br);
                crudbhr.modificarRol(connection, "ROL", rolId, br);
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

    //metode per obtenir id del rol
    public static int demanarId (BufferedReader br) throws SQLException, NumberFormatException, IOException {
        
            System.out.println("Introdueix el id: ");
            int rolId = Integer.parseInt(br.readLine());
        
        return rolId;
    }

    //metode per demanar el nom del rol
    public static String demanarRolValors (BufferedReader br) throws IOException {
        System.out.println("Introdueix el nom: ");
        String nom = br.readLine();

        return  nom;
    }

    // Demanem a l'usuari que introdueixi un valor per buscar
    public static String[] demanarValorsCercaLike(BufferedReader br) throws IOException {
        // Preguntem el nom del camp
        System.out.print("Introdueix el nom del camp per cercar (ex: NOM, rolId, etc.): ");
        String fieldName = br.readLine();
        
        // Preguntem el valor de cerca amb LIKE
        System.out.print("Introdueix el valor de cerca amb '%': ");
        String searchValue = br.readLine();
        return new String[] {fieldName, searchValue};
    }
}