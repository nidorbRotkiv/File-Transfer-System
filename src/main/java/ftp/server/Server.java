package ftp.server;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static ftp.common.Shared.*;

public class Server {
    private final int port;

    public Server() {
        this.port = DEFAULT_PORT;
        createStorageDirectory();
    }

    public static Map<String, Long> storageContentsWithSizes() {
        File directory = new File(STORAGE_DIRECTORY_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] contents = directory.listFiles();
            if (contents == null) {
                return null;
            }
            Map<String, Long> files = new HashMap<>();
            for (File file : contents) {
                files.put(file.getName(), file.length());
            }
            return files;
        } else {
            System.err.println("The directory does not exist or is not a directory.");
            return null;
        }
    }

    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening on port " + port + ".");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void createStorageDirectory() {
        File folder = new File(STORAGE_DIRECTORY_NAME);
        if (folder.exists()) {
            System.out.println("Folder already exists.");
            return;
        }
        if (folder.mkdir()) {
            System.out.println("Folder created successfully.");
        } else {
            System.err.println("Failed to create folder.");
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
