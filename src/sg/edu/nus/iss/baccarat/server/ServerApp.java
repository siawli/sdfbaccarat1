package sg.edu.nus.iss.baccarat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerApp {

    static String cardsDBName = "cards.db";
    static File userDB;
    static Integer betAmount;
    static String gameResultsSummary;

    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);
        int numCardDecks = Integer.parseInt(args[1]);

        List<Double> allCards = Decks.getAllCards(numCardDecks); // Get all cards based on the number of decks specified

        File cardsDB = new File(cardsDBName);

        // Create file and save shuffled cards if currently does not exist
        if (!cardsDB.exists()) {
            try {
                cardsDB.createNewFile();
                Decks.saveShuffledCards(allCards, cardsDB);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // Connect to server socket
            ServerSocket server = new ServerSocket(portNumber);
            System.out.println("Waiting for client on port " + portNumber + " ...");

            Socket socket = server.accept();

            InputStream is = null;
            DataInputStream dis = null;
            OutputStream os = null;
            DataOutputStream dos = null;

            while (true) {

                os = socket.getOutputStream();
                dos = new DataOutputStream(os);
                is = socket.getInputStream();
                dis = new DataInputStream(is);

                // Read data from client
                String dataFromClient = dis.readUTF();

                if (dataFromClient.equals("exit")) {
                    System.out.println("Client exited, server exiting too");
                    break;
                } else {
                    BaccaratEngine baccaratEngine = new BaccaratEngine(cardsDB);

                    if (baccaratEngine.checkCards() == true) {
                        String prefix = dataFromClient.split("\\|")[0];

                        // LOGIN COMMAND
                        if (prefix.equals("login")) {
                            String user = baccaratEngine.login(dataFromClient);
                            userDB = new File("users/" + user + ".db");
                            dos.writeUTF("continue");
                            dos.flush();
                        }

                        // BET COMMAND
                        if (prefix.equals("bet")) {
                            betAmount = baccaratEngine.bet(dataFromClient);
                            dos.writeUTF("continue");
                            dos.flush();
                        }

                        // DEAL COMMAND
                        if (prefix.equals("deal")) {
                            gameResultsSummary = baccaratEngine.deal(userDB, betAmount, dataFromClient);
                            dos.writeUTF(gameResultsSummary);
                            dos.flush();
                        }

                    } else {
                        System.out.println("Insufficient cards, stopping game now...");

                        dos.writeUTF("end game");
                        dos.flush();
                        cardsDB.delete();
                        break;
                    }
                }
            }

            // Close socket
            dis.close();
            dos.close();
            socket.close();
            server.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
