package ftp.server;

import ftp.common.FTPShared;
import ftp.common.FileOperations;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

public class ClientHandler extends Thread implements FileOperations {
    private final Socket clientSocket;
    private DataInputStream clientReader;
    private DataOutputStream clientWriter;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            clientReader = new DataInputStream(clientSocket.getInputStream());
            clientWriter = new DataOutputStream(clientSocket.getOutputStream());
        } catch (Exception e) {
            FTPShared.handleException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String clientCommand = clientReader.readUTF();
                File file = new File(FTPShared.STORAGE_DIRECTORY_PATH, getFileNameFromCommand(clientCommand));
                if (clientCommand.startsWith(FTPShared.RETRIEVE_COMMAND)) {
                    sendFile(file);
                } else if (clientCommand.startsWith(FTPShared.STORE_COMMAND)) {
                    clientWriter.writeUTF(String.valueOf(receiveFile(file)).toUpperCase());
                } else if (clientCommand.startsWith(FTPShared.DELETE_COMMAND)) {
                    clientWriter.writeUTF(String.valueOf(deleteFile(file)).toUpperCase());
                } else if (clientCommand.startsWith(FTPShared.LIST_COMMAND)) {
                    clientWriter.writeUTF(convertMapToString(Objects.requireNonNull(Server.storageContentsWithSizes())));
                } else {
                    throw new RuntimeException("Unknown action");
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            FTPShared.handleException(e);
        } finally {
            try {
                close();
            } catch (IOException e) {
                FTPShared.handleException(e);
            }
        }
    }

    private String convertMapToString(Map<String, Long> filesWithSizes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Long> entry : filesWithSizes.entrySet()) {
            stringBuilder.append(entry.getKey());
            stringBuilder.append(FTPShared.KEY_VALUE_PAIR_SEPARATOR);
            stringBuilder.append(entry.getValue());
            stringBuilder.append(FTPShared.ENTRY_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    private String getFileNameFromCommand(String command) {
        String[] commandAndFileName = command.split(" ", 2);
        return commandAndFileName.length > 1 ? commandAndFileName[1] : command;
    }

    @Override
    public boolean sendFile(File file) {
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getName());
            return false;
        }
        try {
            if (FileOperations.writeToFile(clientWriter, file)) {
                System.out.println("File " + file.getName() + " sent successfully.");
                return true;
            }
            return false;
        } catch (Exception e) {
            FTPShared.handleException(e);
            return false;
        }
    }

    @Override
    public boolean receiveFile(File file) {
        if (file.exists()) {
            System.out.println("File already stored.");
            FileOperations.discardFileData(clientReader);
            return false;
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            if (FileOperations.readFile(fileOutputStream, clientReader)) {
                System.out.println("File " + file.getName() + " received successfully.");
                return true;
            }
            return false;
        } catch (Exception e) {
            FTPShared.handleException(e);
            return false;
        }
    }

    @Override
    public boolean deleteFile(File file) {
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getName());
            return false;
        }
        if (!file.delete()) {
            System.err.println("Failed to delete the file: " + file.getName());
            return false;
        }
        System.out.println("File " + file.getName() + " deleted successfully.");
        return true;
    }

    @Override
    public void close() throws IOException {
            clientWriter.close();
            clientReader.close();
            clientSocket.close();
    }
}