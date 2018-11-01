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

public class Player {
    private static Player instance = null;
   
    private double accountBalance = 1000;
    
    
    /**
     * Private constructor due to the use of the singleton-pattern.
     */
    private Player() {
        
    }
    
    /**
     * If there is no player-object, it gets created, otherwise it just returns the aforementioned object.
     * @return Player object
     */
    public static Player getInstance() {
        if(instance == null) {
            instance = new Player();
        }
        return instance;
    }
    
   

    public double getAccountBalance() {
        return accountBalance;
    }
    
    /**
     * Adds the difference of balance to the player's account balance.
     * @param difference 
     */
    public void setAccountBalance(double difference) {
        this.accountBalance += difference;
    } 
    
    
    
    
}