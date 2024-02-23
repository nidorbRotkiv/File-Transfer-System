package ftp.common;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;

public class FTPShared {
    private static final Dotenv dotenv = Dotenv.load();
    public static final int DEFAULT_PORT = Integer.parseInt(dotenv.get("DEFAULT_PORT"));
    public static final String DEFAULT_SERVER_NAME = dotenv.get("DEFAULT_SERVER_NAME");
    public static final String STORAGE_DIRECTORY_NAME = dotenv.get("STORAGE_DIRECTORY_NAME");
    public static final String STORAGE_DIRECTORY_PATH = "./" + STORAGE_DIRECTORY_NAME + "/";
    public final static String RETRIEVE_COMMAND = "RETR";
    public final static String STORE_COMMAND = "STOR";
    public static final String DELETE_COMMAND = "DELE";
    public static final String LIST_COMMAND = "LIST";
    public static final String SERVER_RESPONSE_OK = "TRUE";
    public static final String AUTHENTICATION_SUCCESS = "AUTH_SUCCESS";
    public static final String AUTHENTICATION_FAILED = "AUTH_FAILED";
    public static final String KEY_VALUE_PAIR_SEPARATOR = ":";
    public static final String ENTRY_SEPARATOR = ";";

    public static void handleException(Exception e) {
        if (e instanceof FileNotFoundException) {
            System.err.println("File not found: " + e.getMessage());
        } else if (e instanceof ConnectException) {
            System.err.println("Connection failed: " + e.getMessage());
        } else if (e instanceof IOException) {
            System.err.println("IOException: " + e.getMessage());
        } else {
            System.err.println("An error occurred: " + e.getMessage());
        }
        e.printStackTrace();
    }
}
