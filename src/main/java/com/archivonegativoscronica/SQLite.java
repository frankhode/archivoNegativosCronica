/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class SQLite {

    public SQLite(Funciones cron) throws ClassNotFoundException, IOException {
        String url = "jdbc:sqlite:archivocronica.db"; // Path to your SQLite database file
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database.");
                Statement stmt = conn.createStatement()  ;
                // Read SQL script from file
                String path = "C:\\Users\\francisco.ortiz\\Downloads\\archivocronicaStructure.sql"; // Path to your SQL file
                String sqlScript = new String(Files.readAllBytes(Paths.get(path)));

                // Execute SQL script
                stmt.execute(sqlScript);
                System.out.println("Database schema created successfully.");
        
                ExportarParaAccess epa = new ExportarParaAccess(cron) ;
                for (String table : epa.getTables()) {
                    // Import CSV data to SQLite
                    //importCSV(conn, table, epa.getFolder()+table+".csv");
                    printDB(url);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void importCSV(Connection sqliteConn, String tableName, String csvFilePath) {
        System.out.println(csvFilePath);
        try {
            String insertSQL = generateInsertStatement(sqliteConn, tableName);
            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
                    
                 PreparedStatement pstmt = sqliteConn.prepareStatement(insertSQL)) {

                sqliteConn.setAutoCommit(false);
                String line;
                int batchSize = 1000;
                int count = 0;

                // Skip header
                br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] values = line.split("\\t");

                    for (int i = 0; i < values.length; i++) {
                        pstmt.setString(i + 1, values[i]);
                    }

                    pstmt.addBatch();

                    if (++count % batchSize == 0) {
                        pstmt.executeBatch();
                    }
                }

                pstmt.executeBatch();
                sqliteConn.commit();
                System.out.println("CSV data imported to SQLite database successfully.");
            } catch (IOException | SQLException e) {
                System.out.println(e);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    public static String generateInsertStatement(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getColumns(null, null, tableName, null);

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName);

        if (rs.next()) {
            sql.append(" (");
            List<String> columns = new ArrayList<>();

            do {
                columns.add(rs.getString("COLUMN_NAME"));
            } while (rs.next());

            sql.append(String.join(", ", columns));
            sql.append(") VALUES (");

            for (int i = 0; i < columns.size(); i++) {
                sql.append("?");
                if (i < columns.size() - 1) {
                    sql.append(", ");
                }
            }

            sql.append(")");
        } else {
            System.out.println(sql);
            throw new SQLException("No columns found for table " + tableName);
        }

        String insertSQL = sql.toString();
        System.out.println("Generated SQL: " + insertSQL); // Print generated SQL statement

        return insertSQL;
    }
    
    public void printDB(String dbFilePath) {
        try (Connection conn = DriverManager.getConnection(dbFilePath)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, null, new String[]{"TABLE"});
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("Table: " + tableName);
                    System.out.println("-------------");
                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int columnsNumber = rsmd.getColumnCount();
                    while (resultSet.next()) {
                        for (int i = 1; i <= columnsNumber; i++) {
                            if (i > 1) System.out.print(",  ");
                            String columnValue = resultSet.getString(i);
                            System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                        }
                        System.out.println("");
                    }
                    System.out.println("-------------");
                    statement.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
