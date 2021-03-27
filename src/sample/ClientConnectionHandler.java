package sample;

import javafx.scene.control.TreeItem;

import java.io.*;
import java.net.Socket;

/**
 * The Client connection handler.
 */
public class ClientConnectionHandler extends Thread {
    protected Socket socket;
    protected File directory;
    protected ObjectOutputStream out = null;
    protected ObjectInputStream in = null;

    /**
     * Instantiates a new Client connection handler.
     *
     * @param socket    the socket
     * @param directory the root directory of server
     */
    public ClientConnectionHandler(Socket socket, File directory) {
        super();
        this.socket = socket;
        this.directory = directory;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
    }

    /**
     * Parses incoming commands
     */
    public void run() {
        String command = "";

        try {
            command = (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (command.toUpperCase().startsWith("DOWNLOAD")) {
                download(command.substring(9));
            } else if (command.toUpperCase().startsWith("UPLOAD")) {
                upload(command.substring(7));
            } else if (command.equalsIgnoreCase("DIR")) {
                dir();
            } else {
                System.out.println("No such command: " + command);
            }
            disconnect();
        }
    }

    /**
     * Download file.
     *
     * @param filePath the file path to the file
     */
    public void download(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            byte[] bytes = new byte[8192];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload file.
     *
     * @param fileName the file name
     */
    public void upload(String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(this.directory + "/" + fileName);
            byte[] bytes = new byte[8192];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends table view of the server directory to the client.
     */
    public void dir() {
        try {
            out.writeObject(new TreeItemSerialize<>(getFiles(directory)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets files from a directory.
     *
     * @param directory the directory
     * @return the root of the directory
     */
    public TreeItem<File> getFiles(File directory) {
        TreeItem<File> root = new TreeItem<>(directory);

        try {
            for(File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    root.getChildren().add(getFiles(file));
                } else {
                    root.getChildren().add(new TreeItem<>(file));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * Disconnect the client
     */
    private void disconnect() {
        try {
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
