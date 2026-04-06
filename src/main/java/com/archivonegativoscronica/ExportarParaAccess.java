/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author francisco.ortiz
 */
class ExportarParaAccess {
    private final Funciones cron ;
    private String folder ;
    private String[] tables ;
    
    ExportarParaAccess(Funciones cron) throws IOException {
        this.cron = cron ;
        // List of tables you want to export
        tables = new String[]{"areas","colecciones","conjuntos","descriptoresimagenes","digitales",
            "edicionimpresa","indizimagenes","intervenciones","inventario","items","mapaareas",
            "materias","partidos","recortes","registros","relaciones","terminos","titulos",
            "usuarios","vistoimagenes"};
        for (String table : tables) {
            exportTableToCSV(cron.conn, table);
        }
    }
    
    private void exportTableToCSV(Connection conn, String tableName) throws IOException {        
        String query = "SELECT * FROM " + tableName;
        folder = "U:\\Fototeca 3\\Catalogación\\Crónica\\Tablas\\" ;
        try (Statement stmt = conn.createStatement();                
            ResultSet rs = stmt.executeQuery(query);
                
            FileWriter writer = new FileWriter(folder+tableName + ".csv")) {

            ResultSetMetaData metaData = rs.getMetaData();            
            int columnCount = metaData.getColumnCount();

            // Write column headers to CSV
            for (int i = 1; i <= columnCount; i++) {
                writer.append(metaData.getColumnLabel(i));
                if (i < columnCount) {
                    writer.append("\t");
                } else {
                    writer.append("\n");
                }
            }

            // Write rows to CSV
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    writer.append(value != null ? value.toString() : "");
                    if (i < columnCount) {
                        writer.append("\t");
                    } else {
                        writer.append("\n");
                    }
                }
            }

            System.out.println("Exported table " + tableName + " to CSV successfully.");

        } catch (SQLException | IOException e) {  
            System.out.println(e);
        }
    }
    
    public void AccessQueryExample() {
        // Database URL, username, and password
        String url = "jdbc:ucanaccess://U:\\Fototeca 3\\Catalogación\\Crónica\\"
                + "afdc.accdb";
        String user = "";
        String password = "";

        // SQL query
        String sql = "SELECT * FROM areas";

        try {
            // Establish connection to the Access database
            cron.conn = DriverManager.getConnection(url, user, password);

            // Create a statement
            Statement stmt = cron.conn.createStatement();

            // Execute the query
            ResultSet rs = stmt.executeQuery(sql);

            // Process the results
            while (rs.next()) {
                // Retrieve data from the result set
                int id = rs.getInt("ID");
                String name = rs.getString("Name");

                // Do something with the data (print it, for example)
                System.out.println("ID: " + id + ", Name: " + name);
            }

            // Close the resources
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    public String[] getTables() {
        return tables ;
    }
    
    public String getFolder() {
        return folder ;
    }
    
}
