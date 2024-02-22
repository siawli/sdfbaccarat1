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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientApp {

    static String gameHistoryDBName = "game_history.csv";
    
    public static InputStream is = null;
    public static DataInputStream dis = null;
    public static OutputStream os = null;
    public static DataOutputStream dos = null;

    public static void main(String[] args) throws IOException {
        
        String host = args[0].split(":")[0];
        int portNumber = Integer.parseInt(args[0].split(":")[1]);

        Socket socket = null;

        try {
            socket = new Socket(host, portNumber);
            System.out.println("It's time to play Baccarat!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to host. Please try again later.");
            System.exit(1);
        }

        while (true) {
            Console console = System.console();
            String input = console.readLine("What would you like to do? ").toLowerCase();

            os = socket.getOutputStream();
            is = socket.getInputStream();
            dos = new DataOutputStream(os);
            dis = new DataInputStream(is);
            
            if (!InputValidation(input) || !ReadOutCome()) {
                break;
            };
        }

        dis.close();
        is.close();
        dos.close();
        os.close();

        // Close client socket
        socket.close();

        System.out.println("Thank you for playing!");
    }

    private static boolean InputValidation(String input) throws IOException {
        String inputClean = null;

        if (input.startsWith("login") || input.startsWith("bet")) {
            inputClean = input.replaceAll("\\s+", "|");
            System.out.println(inputClean); // TO REMOVE - TESTING
            dos.writeUTF(inputClean);
            dos.flush();
        } else if (input.startsWith("deal")) {
            String bet = input.split("\\s+")[1];
            inputClean = "deal|" + bet;
            dos.writeUTF(inputClean);
            dos.flush();
        } else if (input.equals("exit")) {
            // Gives Client the choice to exit game
            dos.writeUTF(input);
            System.exit(0);;
            return false;
        } else {
            System.out.println("Please input a valid command");
        }

        return true;
    }

    private static boolean ReadOutCome() throws IOException {
        String outcome = dis.readUTF();
        System.out.println(outcome);

        if (outcome.equals("end game")) {
            System.out.println("Insufficient cards, stopping game now...");
            return false;
        } else if (outcome.equals("continue")) {
            System.out.println("Insufficient amount, please deposit more money or choose a smaller bet");
        } else if (outcome.equals("insufficient amount")) {
            System.out.println("Insufficient amount, please deposit more money or choose a smaller bet");
        } else {
            String gameResult = CalculateResult(outcome);
            ReadToCSV(gameResult);
        }

        return true;
    }

    private static void ReadToCSV(String gameResult) throws IOException {

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

        System.out.println("Thank you for playing!");

    }

    private static String CalculateResult(String outcome) {        
        String gameResult = null;
        
        String playerResult = outcome.split(",")[0];
        String bankerResult = outcome.split(",")[1];
        
        // I think we normally use int and not Integer. Actually not sure... do check
        Integer playerValue = GetValue(playerResult);
        Integer bankerValue = GetValue(bankerResult);
        
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

        return gameResult;
    }

    private static Integer GetValue(String result) {
        int value = 0;

        try (Scanner scanPlayer = new Scanner(result).useDelimiter("\\|")) {
            scanPlayer.next(); // P
            while (scanPlayer.hasNext()) {
                value = value + scanPlayer.nextInt();
            }
            scanPlayer.close();
        }
        if (value >= 10) {
            return Character.getNumericValue(result.toString().charAt(1));
        } else {
            return value;
        }
    }
}