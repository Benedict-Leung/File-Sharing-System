package sample;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
        protected Socket clientSocket = null;
        protected ServerSocket serverSocket = null;

        public static int SERVER_PORT = 16789;
        public static File DIRECTORY = new File("sharedFolder");

        public Server() {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                System.out.println("Listening to port: " + SERVER_PORT);

                while (true) {
                    clientSocket = serverSocket.accept();
                    ClientConnectionHandler client = new ClientConnectionHandler(clientSocket, DIRECTORY);
                    client.start();
                }
            } catch (IOException e) {
                System.err.println("IOException while creating server connection");
            }
        }

        public static void main(String[] args) {
            Server app = new Server();
        }
}
