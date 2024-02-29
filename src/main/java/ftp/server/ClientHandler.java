package ftp.server;

import ftp.common.FileOperations;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

import static ftp.common.Command.*;
import static ftp.common.Shared.*;

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
            handleException(e);
        }
    }

    @Override
    public void run() {
        try {
            if (!authenticateClient()) {
                return;
            }
            while (true) {
                String clientCommand = clientReader.readUTF();
                File file = new File(STORAGE_DIRECTORY_PATH, getFileNameFromCommand(clientCommand));
                if (clientCommand.startsWith(RETRIEVE.command)) {
                    sendFile(file);
                } else if (clientCommand.startsWith(STORE.command)) {
                    clientWriter.writeUTF(String.valueOf(receiveFile(file)).toUpperCase());
                } else if (clientCommand.startsWith(DELETE.command)) {
                    clientWriter.writeUTF(String.valueOf(deleteFile(file)).toUpperCase());
                } else if (clientCommand.startsWith(LIST.command)) {
                    clientWriter.writeUTF(convertMapToString(Objects.requireNonNull(Server.storageContentsWithSizes())));
                } else {
                    throw new RuntimeException("Unknown action");
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            handleException(e);
        } finally {
            try {
                close();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    private boolean authenticateClient() throws IOException {
        String passwordFromUser = DigestUtils.sha256Hex(clientReader.readUTF());
        String serverPassword = DigestUtils.sha256Hex(Dotenv.load().get("SERVER_PASSWORD"));
        boolean correctPassword = Objects.equals(passwordFromUser, serverPassword);
        if (!correctPassword) {
            clientWriter.writeUTF(AUTHENTICATION_FAILED);
            close();
            return false;
        }
        clientWriter.writeUTF(AUTHENTICATION_SUCCESS);
        System.out.println("Client " + clientSocket.getInetAddress() + " connected.");
        return true;
    }

    private String convertMapToString(Map<String, Long> filesWithSizes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Long> entry : filesWithSizes.entrySet()) {
            stringBuilder.append(entry.getKey());
            stringBuilder.append(KEY_VALUE_PAIR_SEPARATOR);
            stringBuilder.append(entry.getValue());
            stringBuilder.append(ENTRY_SEPARATOR);
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
            handleException(e);
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
            handleException(e);
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