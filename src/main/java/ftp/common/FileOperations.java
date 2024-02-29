package ftp.common;

import java.io.*;

import static ftp.common.Shared.handleException;

public interface FileOperations {
    int bufferSize = 4096;
    boolean sendFile(File file) throws IOException;
    boolean receiveFile(File file) throws IOException;
    boolean deleteFile(File file) throws IOException;
    void close() throws IOException;
    static boolean readFile(FileOutputStream fileOutputStream, DataInputStream dataInputStream) {
        try {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[bufferSize];

            int read;
            long totalRead = 0;
            while (totalRead < fileSize) {
                read = dataInputStream.read(buffer);
                totalRead += read;
                fileOutputStream.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            handleException(e);
            return false;
        }
    }
    static boolean writeToFile(DataOutputStream dataOutputStream, File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            dataOutputStream.writeLong(file.length());
            byte[] buffer = new byte[bufferSize];
            int read;
            while ((read = fileInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            handleException(e);
            return false;
        }
    }

    static void discardFileData(DataInputStream dataInputStream) {
        try {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[bufferSize];

            long totalRead = 0;
            while (totalRead < fileSize) {
                int read = dataInputStream.read(buffer);
                if (read == -1) {
                    throw new EOFException("Unexpected end of stream");
                }
                totalRead += read;
            }
        } catch (IOException e) {
            handleException(e);
        }
    }
}
