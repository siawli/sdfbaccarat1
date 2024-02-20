package sg.edu.nus.iss.baccarat.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Decks {

    public static List<Double> getAllCards(int numCardDecks) {
        List<Double> allCards = new ArrayList<>();
        int i = 0;
        while (i < numCardDecks) {
            for (int j = 0; j < 13; j++) {
                for (int k = 0; k < 4; k++) {
                    allCards.add((j + 1) + ((double) (k + 1) / 10));
                }
            }
            i++;
        }
        return allCards;
    }

    public static void saveShuffledCards(List<Double> allCards, File cardsDB) throws IOException {

        Collections.shuffle(allCards); // Shuffle the decks of cards

        // Save values to file
        BufferedWriter bw = null;
        bw = new BufferedWriter(new FileWriter(cardsDB));

        for (int i = 0; i < allCards.size(); i++) {
            bw.write(allCards.get(i).toString());

            if (i < (allCards.size() - 1)) {
                bw.newLine();
            }
        }

        bw.flush();
        bw.close();

    }

}
