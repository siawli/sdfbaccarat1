package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import sg.edu.nus.iss.baccarat.server.BaccaratEngine; //Even if test file is outside of src directory

public class AppTest {

    /* Test 1 */
    @Test
    public void testLogin() {
        File cardsDB = new File("cards.db");
        BaccaratEngine baccaratEngine = new BaccaratEngine(cardsDB);
        try {
            String user = baccaratEngine.login("login|isis|100");
            assertTrue(user.equals("isis"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Test 2 */
    @Test
    public void testBet() {
        File cardsDB = new File("cards.db");
        BaccaratEngine baccaratEngine = new BaccaratEngine(cardsDB);
        Integer betAmount = baccaratEngine.bet("bet|50");
        assertTrue(betAmount == 50);
    }

    /* Test 3 */
    @Test
    public void testDeal() {
        File userDB = new File("users/isis.db");
        File cardsDB = new File("cards.db");
        BaccaratEngine baccaratEngine = new BaccaratEngine(cardsDB);
        try {
            String gameResultsSummary = baccaratEngine.deal(userDB, 50, "deal|P");
            assertTrue(!gameResultsSummary.isBlank());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
