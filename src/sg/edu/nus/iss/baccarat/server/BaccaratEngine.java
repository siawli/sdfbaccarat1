package sg.edu.nus.iss.baccarat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaccaratEngine {
    private File cardsDB;

    public BaccaratEngine(File cardsDB) {
        this.cardsDB = cardsDB;
    }

    public boolean checkCards() throws IOException {
        int lines = 0;

        FileReader fr = new FileReader(cardsDB);
        BufferedReader br = new BufferedReader(fr);

        while (br.readLine() != null) {
            lines++;
        }

        br.close();
        fr.close();

        System.out.println("Number of cards left: " + lines); // TO REMOVE

        if (lines > 6) {
            // Have enough cards
            return true;
        } else {
            // Not enough cards
            return false;
        }
    }

    public String login(String dataFromClient) throws IOException {

        String user = dataFromClient.split("\\|")[1];
        String balanceString = dataFromClient.split("\\|")[2];
        Integer balance = Integer.parseInt(balanceString);
        File userDB = new File("users/" + user + ".db");

        BufferedWriter bw = null;
        FileReader fr = null;
        BufferedReader br = null;

        if (!userDB.exists()) {
            // Create user file if currently does not exist and save balance
            userDB.createNewFile();
            bw = new BufferedWriter(new FileWriter(userDB));
            bw.write(balanceString);
            bw.flush();
            bw.close();
        } else {
            // Add onto existing balance and save new balance
            fr = new FileReader(userDB);
            br = new BufferedReader(fr);
            Integer existingBalance = Integer.parseInt(br.readLine());
            balance = existingBalance + balance;
            balanceString = balance.toString();
            bw = new BufferedWriter(new FileWriter(userDB));
            bw.write(balanceString);
            bw.flush();
            bw.close();
            br.close();
            fr.close();
        }
        return user;
    }

    public Integer bet(String dataFromClient) {
        // Bet method
        String betAmountString = dataFromClient.split("\\|")[1];
        Integer betAmount = Integer.parseInt(betAmountString);
        return betAmount;
    }

    public String deal(File userDB, Integer betAmount, String dataFromClient) throws IOException {
        // Deal method
        String gameResultsSummary = null;
        int playerBet;
        int bankerBet;
        int playerValue;
        int bankerValue;

        // Split the betting side into two categories
        String betSide = dataFromClient.split("\\|")[1];
        if (betSide.equals("B")) {
            bankerBet = betAmount;
            playerBet = 0;
        } else { // Equals to P
            playerBet = betAmount;
            bankerBet = 0;
        }

        // Draw cards and remove from pile
        FileReader fr = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        List<String> gameResults = new ArrayList<>();

        fr = new FileReader(cardsDB);
        br = new BufferedReader(fr);

        List<String> allCards = new ArrayList<>();
        String line = br.readLine();
        while (line != null) {
            allCards.add(line);
            line = br.readLine();
        }

        // Player draws first
        List<Integer> playerCards = new ArrayList<>();
        playerCards.add((int) Double.parseDouble(allCards.get(0)));
        allCards.remove(0);
        playerCards.add((int) Double.parseDouble(allCards.get(0)));
        allCards.remove(0);
        playerCards.replaceAll(e -> (e == 11 || e == 12 || e == 13) ? 10 : e);
        gameResults.add("P|" + playerCards.get(0) + "|" + playerCards.get(1));
        Integer playerSum = playerCards.get(0) + playerCards.get(1);
        if (playerSum >= 10) {
            playerValue = Character.getNumericValue(playerSum.toString().charAt(1));
        } else {
            playerValue = playerSum;
        }

        // Banker draws next
        List<Integer> bankerCards = new ArrayList<>();
        bankerCards.add((int) Double.parseDouble(allCards.get(0)));
        allCards.remove(0);
        bankerCards.add((int) Double.parseDouble(allCards.get(0)));
        allCards.remove(0);
        bankerCards.replaceAll(e -> (e == 11 || e == 12 || e == 13) ? 10 : e);
        gameResults.add("B|" + bankerCards.get(0) + "|" + bankerCards.get(1));
        Integer bankerSum = bankerCards.get(0) + bankerCards.get(1);
        if (bankerSum >= 10) {
            bankerValue = Character.getNumericValue(bankerSum.toString().charAt(1));
        } else {
            bankerValue = bankerSum;
        }

        if (playerValue <= 5 && (bankerValue != 8 || bankerValue != 9)) { // Take into account instant win
            int playerThirdCard = (int) Double.parseDouble(allCards.get(0));
            allCards.remove(0);
            if (playerThirdCard == 11 || playerThirdCard == 12 || playerThirdCard == 13) {
                playerThirdCard = 10;
            }
            gameResults.set(0, ("P|" + playerCards.get(0) + "|" + playerCards.get(1) + "|" + playerThirdCard));
            playerSum = playerSum + playerThirdCard;
            if (playerSum >= 10) {
                playerValue = Character.getNumericValue(playerSum.toString().charAt(1));
            } else {
                playerValue = playerSum;
            }
        }

        if (bankerValue <= 5 && (playerValue != 8 || playerValue != 9)) { // Take into account instant win
            int bankerThirdCard = (int) Double.parseDouble(allCards.get(0));
            allCards.remove(0);
            if (bankerThirdCard == 11 || bankerThirdCard == 12 || bankerThirdCard == 13) {
                bankerThirdCard = 10;
            }
            gameResults.set(1, ("B|" + bankerCards.get(0) + "|" + bankerCards.get(1) + "|" + bankerThirdCard));
            bankerSum = bankerSum + bankerThirdCard;
            if (bankerSum >= 10) {
                bankerValue = Character.getNumericValue(bankerSum.toString().charAt(1));
            } else {
                bankerValue = bankerSum;
            }
        }
        System.out.println(gameResults); // TO REMOVE

        // Remove cards from cardsDB by rewriting file
        bw = new BufferedWriter(new FileWriter(cardsDB));
        for (int i = 0; i < allCards.size(); i++) {
            bw.write(allCards.get(i).toString());
            if (i < (allCards.size() - 1)) {
                bw.newLine();
            }
        }
        bw.flush();
        bw.close();

        // Interpret game outcome
        fr = new FileReader(userDB);
        br = new BufferedReader(fr);

        Integer existingBalance = Integer.parseInt(br.readLine());
        Integer newBalance = existingBalance;

        if (playerValue > bankerValue) { // If player wins

            if (playerBet > 0) {
                // If bet on player, add
                newBalance = existingBalance + playerBet;
            } else {
                // If bet on banker, subtract if got sufficient amount
                if (betAmount > existingBalance) {
                    gameResultsSummary = "insufficient amount";
                } else {
                    newBalance = existingBalance - bankerBet;
                }
            }

        } else if (bankerValue > playerValue) { // If banker wins
            if (bankerBet > 0) {
                // If bet on banker, add
                if (bankerValue == 6) {
                    newBalance = existingBalance + (bankerBet / 2);
                } else {
                    newBalance = existingBalance + bankerBet;
                }
            } else {
                // If bet on player, subtract if got sufficient amount
                if (betAmount > existingBalance) {
                    gameResultsSummary = "insufficient amount";
                } else {
                    newBalance = existingBalance - playerBet;
                }
            }
        }

        bw = new BufferedWriter(new FileWriter(userDB));
        bw.write(newBalance.toString());
        bw.flush();

        br.close();
        fr.close();
        bw.close();

        // Send game outcome to client side
        if (gameResultsSummary != "insufficient amount") {
            gameResultsSummary = gameResults.toString().replace("[", "").replace("]", "").replace(" ", "");
            System.out.println(gameResultsSummary); // TO REMOVE
        }
        return gameResultsSummary;
    }
}
