package ftp.client;

import ftp.common.FileOperations;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static ftp.common.Command.*;
import static ftp.common.Shared.*;

public class Client implements FileOperations, AutoCloseable {
    private final Socket serverConnection;
    private final DataOutputStream serverWriter;
    private final DataInputStream serverReader;
    private boolean authSuccessful;
    private final String password;

    public Client(String serverName, int port, String password) throws IOException {
        this.password = password;
        this.serverConnection = new Socket(serverName, port);
        this.serverWriter = new DataOutputStream(serverConnection.getOutputStream());
        this.serverReader = new DataInputStream(serverConnection.getInputStream());
        sendPasswordToServer();
    }

    public Client(String password) throws IOException {
        this(DEFAULT_SERVER_NAME, DEFAULT_PORT, password);
    }

    public String getServerName() {
        return serverConnection.getInetAddress().getHostName();
    }

    public int getServerPort() {
        return serverConnection.getPort();
    }

    public boolean isAuthSuccessful() {
        return authSuccessful;
    }

    public boolean isConnected() {
        return serverConnection.isConnected() && authSuccessful;
    }

    private void sendPasswordToServer() throws IOException {
        serverWriter.writeUTF(password);

        String authResponse = serverReader.readUTF();
        if (authResponse.equals(AUTHENTICATION_FAILED)) {
            authSuccessful = false;
            close();
            return;
        }
        authSuccessful = true;
    }

    @Override
    public boolean sendFile(File file) throws IOException {
        sendCommandToServer(STORE.command, file);
        return FileOperations.writeToFile(serverWriter, file) && serverResponseIsOk();
    }

    @Override
    public boolean receiveFile(File file) throws IOException {
        sendCommandToServer(RETRIEVE.command, file);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            return FileOperations.readFile(fileOutputStream, serverReader);
        }
    }

    @Override
    public boolean deleteFile(File file) throws IOException {
        sendCommandToServer(DELETE.command, file);
        return serverResponseIsOk();
    }

    private boolean serverResponseIsOk() throws IOException {
        return SERVER_RESPONSE_OK.equals(serverReader.readUTF());
    }

    private void sendCommandToServer(String command, File file) throws IOException {
        serverWriter.writeUTF(command + " " + file.getName());
    }

    public Map<String, Long> requestServerFileList() throws IOException {
        serverWriter.writeUTF(LIST.command);
        String filesListAsString = serverReader.readUTF();
        return convertFilesList(filesListAsString);
    }

    private Map<String, Long> convertFilesList(String filesListAsString) {
        Map<String, Long> filesWithSizes = new HashMap<>();
        for (String entry : filesListAsString.split(ENTRY_SEPARATOR)) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] keyValue = entry.split(KEY_VALUE_PAIR_SEPARATOR);
            if (keyValue.length != 2) {
                continue;
            }
            try {
                filesWithSizes.put(keyValue[0], Long.parseLong(keyValue[1]));
            } catch (NumberFormatException e) {
                System.err.println("Badly formatted filesize for " + keyValue[0]);
            }
        }
        return filesWithSizes;
    }

    @Override
    public void close() throws IOException {
        serverWriter.close();
        serverReader.close();
        serverConnection.close();
    }
}
