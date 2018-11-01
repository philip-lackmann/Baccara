/*  
 *  © Copyright Philip Lackmann
 *
 *  [Project Title]     Baccara
 *  [Description]       The standalone version of Baccara which I originally made for a bigger 
                        group project consisting of a whole casino with 5 games.
 *  [Authors]           Philip Lackmann
 *  [Version]           Version 1.0      
 */

package ch.bbbaden.baccara.model;

import ch.bbbaden.baccara.controller.BaccaraMainViewController;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

//sources for game rules: https://www.casino-games-online.biz/cards/chemin-de-fer.html
//                        http://gambling-baccarat.com/rules/drawing-rules

public class Baccara {
    
    private double betsTie;
    private double betsBanker;
    private double betsPlayer;

    private BaccaraMainViewController controller;

    private int playerScore;
    private int bankerScore;

    private final ArrayList<String> deck = new ArrayList<>();
    
    private boolean thirdCardDrawn = false;
    private int playersThirdCardValue;

    public Baccara()
    {
        //Baccara (Chemin de Fer) is commonly played with 6 decks
        setupDeck(6);
        Collections.shuffle(deck);
    }

    /**
     * Handles everything to play one whole round and reset everything
     */
    public void play()
    {
        //place first two cards for player and banker
        for (int i = 0; i < 4; i++)
        {
            drawCard(i);
            if (i <= 1)
            {
                controller.flipCard(i);
            }
        }
        updateScores(); 
        drawOrStandPlayer();
        if (thirdCardDrawn)
        {
            drawOrStandBanker();
            updateScores();
        } 
        int[] slots = new int[]{2, 3, 5};
        Timeline pause1 = new Timeline(new KeyFrame(
                Duration.millis(1000),
                ae -> controller.flipCards(slots)));
        pause1.play();  
        
        Timeline pause = new Timeline(new KeyFrame(
                Duration.millis(2000),
                ae -> determineWinner()));
        pause.play();          
    }
    
    /**
     * Determines the winner and calls showWinner() from the controller
     */
    private void determineWinner()
    {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        String alertTxt;
        
        BigDecimal payP = new BigDecimal(betsPlayer).multiply(new BigDecimal(2));
        BigDecimal payB = new BigDecimal(betsBanker).multiply(new BigDecimal(2));
        //tie is usually paid 8:1 or 9:1
        BigDecimal payT = new BigDecimal(betsTie).multiply(new BigDecimal(8));
        
        if (playerScore > bankerScore)
        {
            //player wins
            alertTxt = "The player wins!";
            alertTxt += "\nYou win " + payP + "$" + "\nand lose " + (betsBanker + betsTie) + "$";
            Player.getInstance().setAccountBalance(payP.doubleValue());
        }
        else if (playerScore < bankerScore)
        {
            //banker wins
            alertTxt = "The banker wins!";
            alertTxt += "\nYou win " + payB + "$" + "\nand lose " + (betsPlayer + betsTie) + "$";
            Player.getInstance().setAccountBalance(payB.doubleValue());
        }
        else
        {
            //tie
            alertTxt = "It's a tie!";
            alertTxt += "\nYou win " + payT + "$" + "\nand lose " + (betsBanker + betsPlayer) + "$";
            Player.getInstance().setAccountBalance(payT.doubleValue());
        }
        controller.showWinner(alertTxt);
    }
    
    /**
     * Handles the next action after drawing the first two cards 
     * based on wether the player can draw, stand or decide between both
     */
    private void drawOrStandPlayer()
    {
        playersThirdCardValue = getCardValue(deck.get(0));
        boolean naturalWin = false;
        if (playerScore >= 8 || bankerScore >= 8)
        {
            naturalWin = true;
        }
        else if (!naturalWin && playerScore <= 4)
        {
            //draw third card for player and get it's value       
            drawCard(4);
            controller.flipCard(4);
            thirdCardDrawn = true;
        }
        else if (!naturalWin && playerScore == 5)
        {
            //source: http://code.makery.ch/blog/javafx-dialogs-official/
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Third card");
            alert.setHeaderText("Third card");
            alert.setContentText("Your total points are 5, do you want to draw a third card?");

            ButtonType buttonTypeYes = new ButtonType("Yes");
            ButtonType buttonTypeNo = new ButtonType("No");
            
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeYes)
            {
                //user chose yes
                drawCard(4);
                controller.flipCard(4);
                thirdCardDrawn = true;
            }
        }
        //6 or 7 --> stand
    }
    
    /**
     * Handles the next action after drawing the first two cards 
     * based on wether the banker has to draw or stand
     */
    private void drawOrStandBanker()
    {
        switch (bankerScore)
        {
            case 3:
                if (playersThirdCardValue != 8)
                {
                    //draw card for banker
                    drawCard(5);
                }
                break;
            case 4:
                if (playersThirdCardValue >= 2 && playersThirdCardValue <= 7)
                {
                    //draw card for banker
                    drawCard(5);
                }
                break;
            case 5:
                if (playersThirdCardValue >= 4 && playersThirdCardValue <= 7)
                {
                    //draw card for banker
                    drawCard(5);
                }
                break;
            case 6:
                if (playersThirdCardValue == 6 || playersThirdCardValue == 7)
                {
                    //draw card for banker
                    drawCard(5);
                }
                break;
            /*case 7:
                //stand
                break;*/
            default:
                //draw card  for banker
                drawCard(5);
                break;
        }
    }
    
    /**
     * draws the first card from the deck, places it and sets the scores
     * @param slot is the slot where the card should be placed on
     */
    private void drawCard(int slot)
    {
        String c = deck.get(0);
        controller.placeCard(c, slot);
        deck.remove(deck.get(0));
        if (slot <= 1 || slot == 4)
        {
            playerScore += getCardValue(c);
            System.out.println(getCardValue(c));
        }
        else
        {
            bankerScore += getCardValue(c);
        }
    }

    /**
     * take the rightmost digit of the score (according to baccarat rules, e.g 6 + 7 = 13 --> 3)
     * and adjust it.
     */
    private void adjustScores()
    {
        String p = String.valueOf(playerScore);
        playerScore = Integer.parseInt(p.substring(p.length() - 1));
        String b = String.valueOf(bankerScore);
        bankerScore = Integer.parseInt(b.substring(b.length() - 1));
    }
    
   /**
    * Updates the player's score and set the label's texts in the controller
    * @param updateBankerScore only updates the player score label when false
    *                          and updates the both the banker and player score label when true
    */
    private void updateScores()
    {
        adjustScores();
        controller.setLblScorePText("POINTS: " + String.valueOf(playerScore));
        controller.setLblScoreBText("POINTS: " + String.valueOf(bankerScore));
    }

    //ace = 1 point, cards 2-9 = face value, image cards = 0 points
    /**
     * Gets the value of a card according to baccara's rules
     * @param c is the card's abbreviation
     * @return returns the card's value adjusted to baccara's rules
     */
    private int getCardValue(String c)
    {
        int i = Integer.parseInt(c.substring(0, c.length() - 1));
        if (i <= 9)
        {
            return i;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Sets up the deck of cards
     * @param decks determines how many decks should be to create one large deck
     */
    private void setupDeck(int decks)
    {
        for (int i = 0; i < decks; i++)
        {
            setupSingleDeck("D");
            setupSingleDeck("C");
            setupSingleDeck("H");
            setupSingleDeck("S");
        }
    }

    /**
     * Sets up one single deck with 52 cards
     * @param color 
     */
    private void setupSingleDeck(String color)
    {
        for (int i = 1; i <= 13; i++)
        {
            deck.add(i + color);
        }
    }
    
    /**
     * Resets everything of Baccara to replay the game
     */
    public void resetBaccara()
    {
        betsTie = 0;
        betsBanker = 0;
        betsPlayer = 0;
        playerScore = 0;
        bankerScore = 0;
        thirdCardDrawn = false;
        if (deck.size() < 6)
        {
            setupDeck(6);
            Collections.shuffle(deck);
        }
        updateScores();   
    }

    public void setBetsTie(double difference)
    {
        this.betsTie += difference;
    }

    public void setBetsBanker(double difference)
    {
        this.betsBanker += difference;
    }

    public void setBetsPlayer(double difference)
    {
        this.betsPlayer += difference;
    }

    public double getBetsTie()
    {
        return betsTie;
    }

    public double getBetsBanker()
    {
        return betsBanker;
    }

    public double getBetsPlayer()
    {
        return betsPlayer;
    }

    public void setController(BaccaraMainViewController controller)
    {
        this.controller = controller;
    }
}
