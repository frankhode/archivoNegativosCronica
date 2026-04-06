/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import javafx.application.Platform;
import javafx.concurrent.Task;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BackupBases class for performing database backups using JSch for SSH connectivity.
 */
public class BackupBases {

    //private static final String LOCAL_BACKUP_PATH = "U:/Fototeca 3/Catalogación/Crónica/bkp/";
    private static final String LOCAL_BACKUP_PATH = "C:\\Users\\francisco.ortiz\\Desktop\\bkp";
    private static final String REMOTE_BACKUP_PATH = "\\/root\\/bkp"; // Update with your remote path

    private static final String LOCAL_MYSQLDUMP_PATH = "C:/xampp/mysql/bin/mysqldump.exe";
    private static final String REMOTE_MYSQLDUMP_PATH = "/usr/bin/mysqldump"; // Update with your remote path

    private static final String DB_USER = "root";
    String vpsHost, vpsUsername, vpsPassword;
    Funciones cron;

    public BackupBases(Funciones cron) {
        this.cron = cron;
        // Your constructor can be empty or used for initializing any other variables.
        LocalDateTime now = LocalDateTime.now();
        String fechaDeHoy = Integer.toString(now.getYear()) +
                String.format("%02d", now.getMonthValue()) +
                String.format("%02d", now.getDayOfMonth());

        //if (this.cron.conectarA) {
            performBackup("archivocronica", LOCAL_BACKUP_PATH, generateArgs("archivocronica", LOCAL_MYSQLDUMP_PATH), fechaDeHoy);
            performBackup("tesaurocronica", LOCAL_BACKUP_PATH, generateArgs("tesaurocronica", LOCAL_MYSQLDUMP_PATH), fechaDeHoy);
        /*} else {
            performBackup("archivocronica", REMOTE_BACKUP_PATH, generateArgs("archivocronica", REMOTE_MYSQLDUMP_PATH), fechaDeHoy);
            performBackup("tesaurocronica", REMOTE_BACKUP_PATH, generateArgs("tesaurocronica", REMOTE_MYSQLDUMP_PATH), fechaDeHoy);
        }*/
    }

    private void performBackup(String dbName, String backupPath, List<String> args, String fechaDeHoy) {

        // Explicitly use "/" as the file path separator for remote paths
        String remotePath = backupPath.replace("\\", "/");
        String remoteFilePath = remotePath + "/" + dbName + "_" + fechaDeHoy + ".sql";

        File backupFile = new File(remoteFilePath);

        if (!backupFile.exists()) {
            System.out.println("Realizando bkp de la base " + dbName + ", por favor espere...");

            // Create a Task for the backup process
            Task<Void> backupTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                        try {
                            int exitCode;
                            if (cron.conectarA) {
                                // Perform local backup
                                ProcessBuilder processBuilder = new ProcessBuilder(args);
                                processBuilder.redirectErrorStream(true);
                                Process process = processBuilder.start();

                                // Save output to the backup file
                                try (InputStream inputStream = process.getInputStream();
                                     FileOutputStream fileOutputStream = new FileOutputStream(backupFile)) {
                                    byte[] buffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) > 0) {
                                        fileOutputStream.write(buffer, 0, bytesRead);
                                    }
                                }

                                // Wait for the process to complete
                                exitCode = process.waitFor();

                                if (exitCode == 0) {
                                    System.out.println("Local backup successful!");
                                } else {
                                    System.out.println("Executing mysqldump with args: " + String.join(" ", args));
                                    System.out.println("Error during local backup. Exit Code: " + exitCode);
                                }
                            } else {
                            // Perform remote backup
                            // Update with your VPS SSH details
                            vpsHost = cron.hostname;
                            vpsUsername = cron.username;
                            vpsPassword = "Lamaquinola10";

                            System.out.println("Attempting SSH connection to: " + vpsHost);

                            JSch jsch = new JSch();
                            Session session = jsch.getSession(vpsUsername, vpsHost, 22);
                            session.setPassword(vpsPassword);

                            // Avoid asking for key confirmation
                            java.util.Properties config = new java.util.Properties();
                            config.put("StrictHostKeyChecking", "no");
                            session.setConfig(config);

                            // Connect to the server
                            session.connect();

                            System.out.println("SSH connection successful.");

                            // Run mysqldump command remotely
                            String command = String.join(" ", args);
                            ChannelExec channel = (ChannelExec) session.openChannel("exec");
                            channel.setCommand(command);

                            // Get the output stream of the channel
                            try (InputStream in = channel.getInputStream(); FileOutputStream out = new FileOutputStream(backupFile)) {
                                // Connect and start the channel
                                channel.connect();

                                // Read the output stream and save it to the backup file
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, bytesRead);
                                }
                                // Update UI on the JavaFX Application Thread
                                Platform.runLater(() -> {
                                    if (channel.getExitStatus() == 0) {
                                        System.out.println("Bkp de la base " + dbName + " realizado con éxito!");
                                    } else {
                                        System.out.println("Error al realizar el bkp de la base " + dbName);
                                    }
                                });
                            }
                        }
                    } catch (JSchException | IOException | InterruptedException ex) {
                        System.out.println("Error during backup:"+ex.getMessage());
                        // Update UI on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            System.out.println("Error during backup: " + ex.getMessage());
                        });
                    }
                    return null;
                }
            };

            // Set up the alert to close when the task completes
            //backupTask.setOnSucceeded(event -> alert.close());
            backupTask.setOnFailed(event -> {
                System.out.println("Fallo " + event.getSource().getException().getMessage());
            });

            // Show the alert and start the backup task
            new Thread(backupTask).start();
        }
    }


    private List<String> generateArgs(String db, String mysqldumpPath) {
        List<String> args = new ArrayList<>();
        
        if (this.cron.conectarA) {
            String dbName = db;
            args.add(mysqldumpPath);
            args.add("-u");
            args.add(DB_USER);
            args.add(dbName);
        } else {
            String dbName = db;
            args.add(mysqldumpPath);
            args.add("-u");
            args.add(DB_USER);
            args.add("-h");
            args.add(vpsHost); // Add the remote server IP or hostname
            args.add(dbName);
        }
        return args;
    }
}
