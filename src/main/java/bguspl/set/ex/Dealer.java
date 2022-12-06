package bguspl.set.ex;

import bguspl.set.Env;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;


    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer countdown times out (at which point he must collect the cards and reshuffle the deck).
     */
    private long countdownUntil;

    //Added

    AtomicInteger timer = new AtomicInteger(60);
    long lastTime = System.currentTimeMillis();
    AtomicBoolean tableIsFull = new AtomicBoolean(false);




    //TODO CHANGING CONSTRUCTOR NOT ALLOWED (If it changes the main initialization)
    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        tableIsFull.getAndSet(false);


        // Added

    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());

        //Added
        System.out.println("I'm the dealer. The cards in my deck are :");
        for(int card: deck) {
            System.out.print(card);
            if(card != 80)
                System.out.printf(", ");
        }
        System.out.println();
        System.out.println("CountDownUntil = " +countdownUntil);

        while (!shouldFinish()) {
            Collections.shuffle(deck);
            if(timer.get()==60)
                placeCardsOnTable();
            countdownLoop();
//            removeAllCardsFromTable();

            //Added
            System.out.println("CountDownUntil = " +countdownUntil);

        }
        announceWinners();
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void countdownLoop() {
        resetCountdown();
        while (!terminate && System.currentTimeMillis() < countdownUntil) {
            updateCountdown();
            try {
                sleepUntilWokenOrTimeout();
            }
            catch (InterruptedException e ) {
            }

            // Invoke only if needed. Sends dealer thread to sleep.
            removeCardsFromTable();
            // Invoke only if needed. Sends dealer thread to sleep
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks if any cards should be removed from the table and returns them to the deck.
     */
    private void removeCardsFromTable() {
        // TODO implement
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement

        //Added
        //TODO : Only place cards if needed! This will fix the timer.
        //TODO : Scenario 1 : timer ran out.
        //TODO : Scenario 2 : Player Chose 3 cards, and

        if(!tableIsFull.get()) {
            System.out.println("Dealer : Trying to place cards on table. ");
            for (int i = 0; i < 12 && i<deck.size(); i++) {  // Place cards until table is full or deck is empty
                table.placeCard(deck.indexOf(i), i);
            }
            tableIsFull.getAndSet(true);
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() throws InterruptedException {
        // TODO: is that the intent?
        synchronized (this){
            wait(200);
        }

    }

    /**
     * Update the countdown display.
     */
    private void updateCountdown() {
        // TODO implement
//        System.out.println(System.currentTimeMillis()-lastTime);
        if(System.currentTimeMillis() - lastTime > 999) {
            timer.decrementAndGet();
            boolean warn = timer.get() <= 10;
            table.env.ui.setCountdown(timer.get()*1000, warn);
            lastTime = System.currentTimeMillis();
            if(timer.get() == 0 ){ timer.set(60); tableIsFull.set(false);}
        }
    }

    /**
     * Reset the countdown timer and update the countdown display.
     */
    private void resetCountdown() {
        if (env.config.turnTimeoutMillis > 0) {
            countdownUntil = System.currentTimeMillis() + env.config.turnTimeoutMillis;
            updateCountdown();
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
    }

    public void placeToken(int player, int slot){
        table.placeToken(player,slot);
    }
}
