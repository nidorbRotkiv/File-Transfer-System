package ftp.client;

import ftp.common.FTPShared;
import ftp.common.FileOperations;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client implements FileOperations, AutoCloseable {
    private final Socket serverConnection;
    private final DataOutputStream serverWriter;
    private final DataInputStream serverReader;

    public Client(String serverName, int port) throws IOException {
        this.serverConnection = new Socket(serverName, port);
        this.serverWriter = new DataOutputStream(serverConnection.getOutputStream());
        this.serverReader = new DataInputStream(serverConnection.getInputStream());
    }

    public Client() throws IOException {
        this(FTPShared.DEFAULT_SERVER_NAME, FTPShared.DEFAULT_PORT);
    }

    public String getServerName() {
        return serverConnection.getInetAddress().getHostName();
    }

    public int getServerPort() {
        return serverConnection.getPort();
    }

    public boolean isConnected() {
        return serverConnection.isConnected();
    }

    @Override
    public boolean sendFile(File file) throws IOException {
        serverWriter.writeUTF(FTPShared.STORE_COMMAND + " " + file.getName());
        return FileOperations.writeToFile(serverWriter, file) &&
                FTPShared.SERVER_RESPONSE_OK.equals(serverReader.readUTF());
    }

    @Override
    public boolean receiveFile(File file) throws IOException {
        serverWriter.writeUTF(FTPShared.RETRIEVE_COMMAND + " " + file.getName());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            return FileOperations.readFile(fileOutputStream, serverReader);
        }
    }

    @Override
    public boolean deleteFile(File file) throws IOException {
        serverWriter.writeUTF(FTPShared.DELETE_COMMAND + " " + file.getName());
        return FTPShared.SERVER_RESPONSE_OK.equals(serverReader.readUTF());
    }

    public Map<String, Long> requestServerFileList() throws IOException {
        serverWriter.writeUTF(FTPShared.LIST_COMMAND);
        String filesListAsString = serverReader.readUTF();
        return convertFilesList(filesListAsString);
    }

    private Map<String, Long> convertFilesList(String filesListAsString) {
        Map<String, Long> filesWithSizes = new HashMap<>();
        for (String entry : filesListAsString.split(FTPShared.ENTRY_SEPARATOR)) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] keyValue = entry.split(FTPShared.KEY_VALUE_PAIR_SEPARATOR);
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
