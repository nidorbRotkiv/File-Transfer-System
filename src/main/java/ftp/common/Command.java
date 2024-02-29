package ftp.common;

public enum Command {
    RETRIEVE("RETR"),
    STORE("STOR"),
    DELETE("DELE"),
    LIST("LIST");

    public final String command;

    private Command(String command) {
        this.command = command;
    }
}

