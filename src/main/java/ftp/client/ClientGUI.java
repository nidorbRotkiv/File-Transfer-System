package ftp.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientGUI {
    private Client client;
    private JTextArea textArea;
    private JTextField serverNameField;
    private JTextField portField;
    private JButton sendButton;
    private JButton receiveButton;
    private JButton deleteButton;

    public ClientGUI() {
        try {
            this.client = new Client();
        } catch (IOException e) {
            showErrorDialog(e, "Failed to initialize client");
        }
        initialize();
    }

    private void initialize() {
        JFrame frame = new JFrame("FTP Client");
        frame.setBounds(150, 150, 800, 500);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                try {
                    if (client != null) client.close();
                } catch (IOException e) {
                    showErrorDialog(e, "Error closing client");
                }
                System.exit(0);
            }
        });

        frame.setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        frame.add(panel, BorderLayout.SOUTH);

        sendButton = createButton("Send File", e -> sendFile(), panel);
        receiveButton = createButton("Receive File", e -> receiveFile(), panel);
        deleteButton = createButton("Delete File", e -> deleteFile(), panel);

        serverNameField = new JTextField();
        portField = new JTextField();
        JButton connectButton = new JButton("Connect");

        List<JComponent> connectionPanelComponents = List.of(serverNameField, portField, connectButton);

        JPanel connectionPanel = new JPanel(new GridLayout(1, connectionPanelComponents.size() + 1));
        frame.add(connectionPanel, BorderLayout.NORTH);

        for (JComponent component : connectionPanelComponents) {
            connectionPanel.add(component);
        }

        connectButton.addActionListener(e -> connectToServer());

        updateConnectionStatus();

        frame.setVisible(true);
    }

    private void updateConnectionStatus() {
        if (client != null && client.isConnected()) {
            textArea.append("Connected to " + client.getServerName() + " on port " + client.getServerPort() + "\n");
            sendButton.setEnabled(true);
            receiveButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            textArea.append("Not connected\n");
            sendButton.setEnabled(false);
            receiveButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void sendFile() {
        if (!client.isConnected()) {
            showNotConnectedDialog();
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) { // client did not choose a file.
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        try {
            if (!selectedFile.exists()) {
                textArea.append("File does not exist\n");
            } else if (client.sendFile(selectedFile)) {
                textArea.append("Sent file: " + selectedFile.getName() + "\n");
            } else {
                textArea.append("Failed to send file: " + selectedFile.getName() + "\n");
            }
        } catch (IOException e) {
            showErrorDialog(e, "Error sending file");
        }
    }

    private void receiveFile() {
        if (!client.isConnected()) {
            showNotConnectedDialog();
            return;
        }
        File selectedFile = serverFileSelector();
        if (selectedFile != null) {
            try {
                if (client.receiveFile(selectedFile)) {
                    textArea.append("Received file: " + selectedFile.getName() + "\n");
                } else {
                    textArea.append("Failed to receive file: " + selectedFile.getName() + "\n");
                }
            } catch (IOException e) {
                showErrorDialog(e, "Error receiving file");
            }
        }
    }

    private void deleteFile() {
        if (!client.isConnected()) {
            showNotConnectedDialog();
            return;
        }
        File selectedFile = serverFileSelector();
        if (selectedFile != null) {
            try {
                if (client.deleteFile(selectedFile)) {
                    textArea.append("Deleted file: " + selectedFile.getName() + "\n");
                } else {
                    textArea.append("Failed to delete file: " + selectedFile.getName() + "\n");
                }
            } catch (IOException e) {
                showErrorDialog(e, "Error deleting file");
            }
        }
    }

    private File serverFileSelector() {
        if (!client.isConnected()) {
            showNotConnectedDialog();
            return null;
        }
        JList<String> fileList = fetchFileListFromServer();
        if (fileList == null) {
            return null;
        }
        if (JOptionPane.showConfirmDialog(null, new JScrollPane(fileList), "Select a File", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String selectedFileName = fileList.getSelectedValue().split(" ")[0];
            return new File(selectedFileName);
        }
        return null;
    }

    private JList<String> fetchFileListFromServer() {
        try {
            Map<String, Long> filesWithSizes = client.requestServerFileList();

            List<String> list = displayList(filesWithSizes);

            String[] arrayRepresentation = list.toArray(new String[0]);
            return new JList<>(arrayRepresentation);
        } catch (IOException e) {
            showErrorDialog(e, "Error fetching file list");
            return null;
        }
    }

    private List<String> displayList(Map<String, Long> filesWithSizes) {
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Long> entry : filesWithSizes.entrySet()) {
            displayList.add(formatToMB(entry.getKey(), entry.getValue()));
        }
        return displayList;
    }

    private String formatToMB(String fileName, Long fileSize) {
        double sizeInMB = fileSize / (1024.0 * 1024.0);
        return String.format("%s (%.2f MB)", fileName, sizeInMB);
    }

    private void connectToServer() {
        try {
            String serverName = serverNameField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            if (this.client != null) this.client.close();
            this.client = new Client(serverName, port);
            updateConnectionStatus();
        } catch (NumberFormatException e) {
            showErrorDialog(e, "Invalid port number");
        } catch (IOException e) {
            showErrorDialog(e, "Failed to connect");
        }
    }

    private void showErrorDialog(Exception e, String message) {
        JOptionPane.showMessageDialog(null, message + ": "
                + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showNotConnectedDialog() {
        JOptionPane.showMessageDialog(null, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton createButton(String buttonText, ActionListener action, JPanel panel) {
        JButton button = new JButton(buttonText);
        button.addActionListener(action);
        panel.add(button);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
