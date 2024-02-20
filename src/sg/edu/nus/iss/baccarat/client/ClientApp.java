package sg.edu.nus.iss.baccarat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientApp {

    static String gameHistoryDBName = "game_history.csv";

    public static void main(String[] args) {

        InputStream is = null;
        DataInputStream dis = null;
        OutputStream os = null;
        DataOutputStream dos = null;

        String host = args[0].split(":")[0];
        int portNumber = Integer.parseInt(args[0].split(":")[1]);

        try {
            // Connect to client socket
            Socket socket = new Socket(host, portNumber);
            System.out.println("It's time to play Baccarat!");

            while (true) {
                // Input commands in Console
                Console console = System.console();
                String input = console.readLine("What would you like to do? ").toLowerCase();

                os = socket.getOutputStream();
                dos = new DataOutputStream(os);
                is = socket.getInputStream();
                dis = new DataInputStream(is);

                // MAY WANT TO ADD ON MORE INPUT VALIDATIONS (EG. NEGATIVE VALUES or 0)

                if (input.startsWith("login") || input.startsWith("bet")) {
                    String inputClean = input.replaceAll("\\s+", "|");
                    System.out.println(inputClean); // TO REMOVE - TESTING
                    dos.writeUTF(inputClean);
                    dos.flush();
                } else if (input.startsWith("deal")) {
                    String bet = input.split("\\s+")[1];
                    String inputClean = "deal|" + bet;
                    dos.writeUTF(inputClean);
                    dos.flush();
                } else if (input.equals("exit")) {
                    // Gives Client the choice to exit game
                    dos.writeUTF(input);
                    break;
                } else {
                    System.out.println("Please input a valid command");
                }

                // Read outcome from server side
                String outcome = dis.readUTF();
                System.out.println(outcome); // TO REMOVE

                if (outcome.equals("end game")) {
                    System.out.println("Insufficient cards, stopping game now...");
                    break;
                } else if (outcome.equals("continue")) {
                    System.out.println("Received Login or Bet");
                } else if (outcome.equals("insufficient amount")) {
                    System.out.println("Insufficient amount, please deposit more money or choose a smaller bet");
                } else {
                    String playerResult = outcome.split(",")[0];
                    String bankerResult = outcome.split(",")[1];

                    Integer playerSum = 0;
                    Integer bankerSum = 0;
                    Integer playerValue;
                    Integer bankerValue;
                    String gameResult;

                    Scanner scanPlayer = new Scanner(playerResult).useDelimiter("\\|");
                    scanPlayer.next(); // P
                    while (scanPlayer.hasNext()) {
                        playerSum = playerSum + scanPlayer.nextInt();
                    }
                    scanPlayer.close();
                    if (playerSum >= 10) {
                        playerValue = Character.getNumericValue(playerSum.toString().charAt(1));
                    } else {
                        playerValue = playerSum;
                    }
                    Scanner scanBanker = new Scanner(bankerResult).useDelimiter("\\|");
                    scanBanker.next(); // B
                    while (scanBanker.hasNext()) {
                        bankerSum = bankerSum + scanBanker.nextInt();
                    }
                    scanBanker.close();
                    if (bankerSum >= 10) {
                        bankerValue = Character.getNumericValue(bankerSum.toString().charAt(1));
                    } else {
                        bankerValue = bankerSum;
                    }

                    if (playerValue > bankerValue) {
                        gameResult = "P";
                        System.out.println("Player wins with " + playerValue + " points");
                    } else if (bankerValue > playerValue) {
                        gameResult = "B";
                        System.out.println("Banker wins with " + bankerValue + " points");
                    } else {
                        gameResult = "D";
                        System.out.println("Draw");
                    }

                    // Write to CSV file - gameResult
                    File gameHistoryDB = new File(gameHistoryDBName);
                    if (!gameHistoryDB.exists()) {
                        gameHistoryDB.createNewFile();
                    }

                    FileReader fr = new FileReader(gameHistoryDB);
                    BufferedReader br = new BufferedReader(fr);
                    List<String> gameHistoryResults = new ArrayList<>();

                    String line = br.readLine();
                    while (line != null) {
                        String[] values = line.split(",");
                        for (String value : values) {
                            gameHistoryResults.add(value);
                        }
                        line = br.readLine();
                    }
                    gameHistoryResults.add(gameResult);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(gameHistoryDB));

                    for (int i = 0; i < gameHistoryResults.size(); i++) {
                        System.out.println(i); // TO REMOVE
                        if (i == 0) {
                            System.out.println("i == 0"); // TO REMOVE
                            bw.write(gameHistoryResults.get(i));
                        } else if (i % 6 == 0) {
                            System.out.println("i % 6"); // TO REMOVE
                            bw.newLine();
                            bw.write(gameHistoryResults.get(i));
                        } else {
                            System.out.println("else"); // TO REMOVE
                            bw.write("," + gameHistoryResults.get(i));
                        }
                    }

                    br.close();
                    fr.close();
                    bw.flush();
                    bw.close();
                }

            }

            dis.close();
            is.close();
            dos.close();
            os.close();

            // Close client socket
            socket.close();

            System.out.println("Thank you for playing!");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
