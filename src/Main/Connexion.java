/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

/*
 *
 * Librairies importées
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Connexion a votre BDD locale ou à distance sur le serveur de l'ECE via le tunnel SSH
 *
 * @author segado
 */
public class Connexion extends JFrame{

    /**
     * Attributs prives : connexion JDBC, statement, ordre requete et resultat
     * requete
     */
    private Connection conn;
    private Statement stmt;
    private ResultSet rset;
    private ResultSetMetaData rsetMeta;
    private boolean coco;
    private CheckboxGroup values;
    private String [] myColomuns, nameOfColonnes, nameOfTables;
    private int i;
    private JTable myResults;


    /**
     * ArrayList public pour les tables
     */
    public ArrayList<String> tables = new ArrayList<>();
    /**
     * ArrayList public pour les requêtes de sélection
     */
    public ArrayList<String> requetes = new ArrayList<>();
    /**
     * ArrayList public pour les requêtes de MAJ
     */
    public ArrayList<String> requetesMaj = new ArrayList<>();

    /**
     * Constructeur avec 3 paramètres : nom, login et password de la BDD locale
     *
     * @param nameDatabase
     * @param loginDatabase
     * @param passwordDatabase
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Connexion(String nameDatabase, String loginDatabase, String passwordDatabase) throws SQLException, ClassNotFoundException {
        // chargement driver "com.mysql.jdbc.Driver"
        Class.forName("com.mysql.jdbc.Driver");

        // url de connexion "jdbc:mysql://localhost:3305/usernameECE"
        String urlDatabase = "jdbc:mysql://localhost/" + nameDatabase;
       // String urlDatabase = "jdbc:mysql://127.0.0.1:8889/" + nameDatabase;

        //création d'une connexion JDBC à la base
        conn = DriverManager.getConnection(urlDatabase, loginDatabase, passwordDatabase);

        /** création d'un ordre SQL (statement)*/
        stmt = conn.createStatement();

        coco = true;
    }

    /**
     * Constructeur avec 4 paramètres : username et password ECE, login et
     * password de la BDD à distance sur le serveur de l'ECE
     * @param usernameECE
     * @param passwordECE
     * @param loginDatabase
     * @param passwordDatabase
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Connexion(String usernameECE, String passwordECE, String loginDatabase, String passwordDatabase) throws SQLException, ClassNotFoundException {
        // chargement driver "com.mysql.jdbc.Driver"
        Class.forName("com.mysql.jdbc.Driver");

        // Connexion via le tunnel SSH avec le username et le password ECE
        SSHTunnel ssh = new SSHTunnel(usernameECE, passwordECE);

        if (ssh.connect()) {
            coco = true;
            System.out.println("Connexion reussie");

            // url de connexion "jdbc:mysql://localhost:3305/usernameECE"
            String urlDatabase = "jdbc:mysql://localhost:3305/" + usernameECE;

            //création d'une connexion JDBC à la base
            conn = DriverManager.getConnection(urlDatabase, loginDatabase, passwordDatabase);

            // création d'un ordre SQL (statement)
            stmt = conn.createStatement();

        }
    }

    public boolean coco(){//return true or false depending on the connexion statement for SSH config
        return coco;
    }

    /**
     * Méthode qui ajoute la table en parametre dans son ArrayList
     *
     * @param table
     */
    public void ajouterTable(String table) {
        tables.add(table);
    }

    /**
     * Méthode qui ajoute la requete de selection en parametre dans son
     * ArrayList
     *
     * @param requete
     */
    public void ajouterRequete(String requete) {
        requetes.add(requete);
    }

    /**
     * Méthode qui ajoute la requete de MAJ en parametre dans son
     * ArrayList
     *
     * @param requete
     */
    public void ajouterRequeteMaj(String requete) {
        requetesMaj.add(requete);
    }

    /**
     * Méthode qui retourne l'ArrayList des champs de la table en parametre
     *
     * @param table
     * @return
     * @throws SQLException
     */
    public ArrayList remplirChampsTable(String table) throws SQLException {
        // récupération de l'ordre de la requete
        rset = stmt.executeQuery("select * from " + table);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();

        // calcul du nombre de colonnes du resultat
        int nbColonne = rsetMeta.getColumnCount();

        // creation d'une ArrayList de String
        ArrayList<String> liste;
        liste = new ArrayList<>();
        String champs = "";
        // Ajouter tous les champs du resultat dans l'ArrayList
        for (int i = 0; i < nbColonne; i++) {
            champs = champs + " " + rsetMeta.getColumnLabel(i + 1);
        }

        // ajouter un "\n" à la ligne des champs
        champs = champs + "\n";

        // ajouter les champs de la ligne dans l'ArrayList
        liste.add(champs);

        // Retourner l'ArrayList
        return liste;
    }

    /**
     * Methode qui retourne l'ArrayList des champs de la requete en parametre
     * @param requete
     * @return
     * @throws SQLException
     */
    public ArrayList remplirChampsRequete(String requete) throws SQLException {
        // récupération de l'ordre de la requete
        rset = stmt.executeQuery(requete);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();

        // calcul du nombre de colonnes du resultat
        int nbColonne = rsetMeta.getColumnCount();

        // creation d'une ArrayList de String
        ArrayList<String> liste;
        liste = new ArrayList<String>();

        // tant qu'il reste une ligne 
        while (rset.next()) {
            String champs;
            champs = rset.getString(1); // ajouter premier champ

            // Concatener les champs de la ligne separes par ,
            for (int i = 1; i < nbColonne; i++) {
                champs = champs + "," + rset.getString(i + 1);
            }

            // ajouter un "\n" à la ligne des champs
            champs = champs + "\n";

            // ajouter les champs de la ligne dans l'ArrayList
            liste.add(champs);
        }

        // Retourner l'ArrayList
        return liste;
    }

    /**
     * Méthode qui execute une requete de MAJ en parametre
     * @param requeteMaj
     * @throws SQLException
     */
    public void executeUpdate(String requeteMaj) throws SQLException {
        stmt.executeUpdate(requeteMaj);
    }

    /**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */

    /**
     * Nos Queries SQL
     */
    public String[] getTablesNames(){
        int i = 1;

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet resultat = md.getTables(null, null, "%", null);

            nameOfTables = new String[12];//jusqu'a 12 tables (arbitraire)

            while (resultat.next()) {
                System.out.println(i + " " + resultat.getString(3));
                nameOfTables[i-1] = resultat.getString(3);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nameOfTables;
    }

    public String[] getColumnValues(String myChoice){

        try {
            ResultSet resultat = stmt.executeQuery( "select column_name from information_schema.columns where table_name='" + myChoice + "';");

            int nbColonnes = 0;

            while ( resultat.next() ) {
                nbColonnes++;
            }

            myColomuns = new String[nbColonnes];

            int i = 0;

            while ( resultat.previous() ) {
                myColomuns[i] = resultat.getString(1);
                i++;
            }

        } catch (Exception  ex){
            System.out.println(ex.getMessage());
        }

        return myColomuns;
    }

    public JTable readDTB(String table, String colonne, int nbElem, boolean graphismes){
        try {
            /* Exécution d'une requête de lecture */
           ResultSet resultat = stmt.executeQuery( "SELECT " +  colonne + " FROM " + table +";");

           nameOfColonnes = colonne.split(",");

           /* Récupération des données du résultat de la requête de lecture */
            int nbRes = 1;
            DefaultTableModel model = new DefaultTableModel();
            myResults = new JTable(model);
            add(new JScrollPane(myResults));
            while ( resultat.next() ) {

                System.out.println("Résultat " + nbRes + " :\n");

                for (int i = 1; i <= nbElem; i++){
                    System.out.println(nameOfColonnes[i-1].trim() + " : " + resultat.getString(i));

                    }

                System.out.println("\n");


                if(graphismes){

                    // Create a couple of columns
                    for (int i = 1; i <= nbElem; i++) {
                        model.addColumn(nameOfColonnes[i-1].trim());
                        model.addRow(new Object[]{resultat.getString(i)});
                    }
                }

                nbRes++;
            }
        } catch (Exception  ex){
            System.out.println(ex.getMessage());
        }
        return myResults;
    }
}