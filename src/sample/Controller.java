package sample;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * The Controller class for application.
 */
public class Controller {
    @FXML public GridPane container;
    @FXML public TreeTableView<File> clientFolder;
    @FXML public TreeTableView<File> serverFolder;
    @FXML public Button download;
    @FXML public Button upload;
    @FXML public Label select;
    protected String computerName;
    protected File folder;
    protected File clientSelected = null;
    protected File serverSelected = null;

    private Socket socket = null;
    private ObjectOutputStream networkOut = null;
    private ObjectInputStream networkIn = null;

    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 16789;


    /**
     * Initialize client.
     *
     * @param computerName the computer name
     * @param path         the path of the client folder
     */
    public void init(String computerName, String path) {
        this.computerName = computerName;
        this.folder = new File(path);

        // Download click function
        download.setOnAction(event -> {
            try {
                if (serverSelected != null) {
                    connect();
                    networkOut.writeObject("DOWNLOAD " + serverSelected.getAbsolutePath());
                    File file = new File(this.folder + "/" + serverSelected.getName());
                    serverSelected = null;
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] bytes = new byte[8192];

                    int count;
                    while ((count = networkIn.read(bytes)) > 0) {
                        out.write(bytes, 0, count);
                    }
                    out.close();
                    networkIn.close();
                    updateDirectories();
                } else {
                    select.setText("Please select a file from server side");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });

        // Upload click function
        upload.setOnAction(event -> {
            try {
                if (clientSelected != null) {
                    connect();
                    networkOut.writeObject("UPLOAD " + clientSelected.getName());
                    File file = new File(clientSelected.getAbsolutePath());
                    clientSelected = null;
                    FileInputStream in = new FileInputStream(file);
                    byte[] bytes = new byte[8192];

                    int count;
                    while ((count = in.read(bytes)) > 0) {
                        networkOut.write(bytes, 0, count);
                    }
                    in.close();
                    networkOut.close();
                    updateDirectories();
                } else {
                    select.setText("Please select a file from client side");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });
        // Initialize columns for client directories
        TreeTableColumn<File, String> clientFileNameCol = new TreeTableColumn<>("File Name");
        TreeTableColumn<File, Long> clientSizeCol = new TreeTableColumn<>("Size");

        // Set graphic and values for filename column
        clientFileNameCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getName()));
        clientFileNameCol.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<File> cell = super.getTreeTableRow().getTreeItem();
                ImageView graphic = null;

                if (cell != null) {
                    File file = cell.getValue();

                    if (file.isDirectory()) {
                        graphic = new ImageView(new Image("sample/folder.png"));
                    } else {
                        graphic = new ImageView(new Image("sample/file.png"));
                    }
                    graphic.setFitWidth(20);
                    graphic.setFitHeight(20);
                }
                setText(empty ? null : item);
                setGraphic(empty ? null : graphic);
            }
        });
        clientFileNameCol.setPrefWidth(200);
        clientFileNameCol.setResizable(false);

        // Set values for size column
        clientSizeCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().length()));
        clientSizeCol.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<File> cell = super.getTreeTableRow().getTreeItem();
                String bytes = null;

                if (cell != null && item != null) {
                    File file = cell.getValue();

                    if (file.isFile()) {
                        bytes = parseBytes(item);
                    }
                }
                setText(empty ? null : bytes);

            }
        });
        clientSizeCol.setPrefWidth(98);
        clientSizeCol.setResizable(false);

        clientFolder.getColumns().setAll(clientFileNameCol, clientSizeCol);

        // Clone columns to server directories
        TreeTableColumn<File, String> serverFileNameCol = new TreeTableColumn<>("File Name");
        serverFileNameCol.setCellValueFactory(clientFileNameCol.getCellValueFactory());
        serverFileNameCol.setCellFactory(clientFileNameCol.getCellFactory());
        serverFileNameCol.setPrefWidth(200);
        serverFileNameCol.setResizable(false);

        TreeTableColumn<File, Long> serverSizeCol = new TreeTableColumn<>("Size");
        serverSizeCol.setCellValueFactory(clientSizeCol.getCellValueFactory());
        serverSizeCol.setCellFactory(clientSizeCol.getCellFactory());
        serverSizeCol.setPrefWidth(98);
        serverSizeCol.setResizable(false);

        serverFolder.getColumns().setAll(serverFileNameCol, serverSizeCol);

        // Tracks selected file
        clientFolder.setOnMouseClicked(event -> {
            TreeItem<File> selected = clientFolder.getSelectionModel().getSelectedItem();

            if (selected != null && selected.getValue().isFile()) {
                clientSelected = selected.getValue();
                serverSelected = null;
                select.setText("Selected: " + clientSelected.getName());
            }
        });

        // Tracks selected file
        serverFolder.setOnMouseClicked(event -> {
            TreeItem<File> selected = serverFolder.getSelectionModel().getSelectedItem();

            if (selected != null && selected.getValue().isFile()) {
                serverSelected = selected.getValue();
                clientSelected = null;
                select.setText("Selected: " + serverSelected.getName());
            }
        });
        updateDirectories();
    }

    /**
     * Update directories for client and server side
     */
    private void updateDirectories() {
        connect();
        TreeItem<File> serverF = null;

        try {
            networkOut.writeObject("DIR");
            serverF = (TreeItem<File>) networkIn.readObject();
            serverF.setExpanded(true);
            disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverFolder.setRoot(serverF);
        updateFolder();
        disconnect();
    }

    /**
     * Update client folder
     */
    public void updateFolder() {
        if (folder != null && folder.isDirectory()) {
            TreeItem<File> root = getFiles(folder);
            root.setExpanded(true);
            clientFolder.setRoot(root);
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
                    TreeItem<File> item = new TreeItem<>(file);
                    root.getChildren().add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * Connect to the server.
     */
    public void connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("IOException while connecting to server: " + SERVER_ADDRESS);
        }

        if (socket == null) {
            System.err.println("Socket is null");
        }

        try {
            networkOut = new ObjectOutputStream(socket.getOutputStream());
            networkIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
    }

    /**
     * Disconnect the server.
     */
    public void disconnect() {
        try {
            networkOut.close();
            networkIn.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse bytes into string.
     *
     * @param bytes the bytes
     * @return the string representation of the bytes
     */
    public static String parseBytes(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}