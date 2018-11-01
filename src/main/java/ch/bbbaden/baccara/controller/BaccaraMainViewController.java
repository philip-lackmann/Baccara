/*  
 *  © Copyright Philip Lackmann
 *
 *  [Project Title]     Baccara
 *  [Description]       The standalone version of Baccara which I originally made for a bigger 
                        group project consisting of a whole casino with 5 games.
 *  [Authors]           Philip Lackmann
 *  [Version]           Version 1.0      
 */

package ch.bbbaden.baccara.controller;

import ch.bbbaden.baccara.MainApp;
import ch.bbbaden.baccara.model.Baccara;
import ch.bbbaden.baccara.model.Player;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class BaccaraMainViewController implements Initializable {

    private Baccara baccara;

    private Stage stage;
    
    @FXML
    private AnchorPane anchorPane;

    //Coordinates for the jeton or stage when dragging one of them around
    //Used for both the jeton AND stage, because you can't drag both at the same time anyways
    private double x;
    private double y;

    @FXML
    private Pane jeton;
    @FXML
    private Button btnJeton;
    @FXML
    private Label lblJeton, lblBalance, lblBet, lblScoreP, lblScoreB;
    @FXML
    private TextField txtFieldJeton;
    @FXML
    private double jetonOriginX, jetonOriginY;
    private ArrayList<Pane> placedJetons = new ArrayList<>();
    @FXML
    private ImageView imgBetField, imgJeton, imgPlay;
    @FXML
    private Rectangle rectTie, rectBanker, rectPlayer, rectResetBets;

    private enum BetType {
        TIE, BANKER, PLAYER, NONE
    }
    private BetType betType = BetType.NONE;

    @FXML
    private Pane cardSlotP1, cardSlotP2, cardSlotP3, cardSlotB1, cardSlotB2, cardSlotB3;
    private final Pane[] cardSlots = new Pane[6];
    private final ImageView[] cards = new ImageView[6];
    
    private boolean roundPlayed;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        this.stage = MainApp.getStage();
        jetonOriginX = jeton.getLayoutX();
        jetonOriginY = jeton.getLayoutY();
        baccara = new Baccara();
        baccara.setController(this);
        updateView();

        //Define the cardSlots
        cardSlots[0] = cardSlotP1;
        cardSlots[1] = cardSlotP2;
        cardSlots[2] = cardSlotB1;
        cardSlots[3] = cardSlotB2;
        cardSlots[4] = cardSlotP3;
        cardSlots[5] = cardSlotB3;
        
        // force the field to be numeric only
        txtFieldJeton.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if (!newValue.matches("\\d*"))
            {
                txtFieldJeton.setText(newValue.replaceAll("[^\\d]", ""));
            }
            else if (txtFieldJeton.getText().length() > 4)
            {
                txtFieldJeton.setText(oldValue);
            }       
        });
    }


    /**
     * onMouseClick
     * Handles a click on the ImageView functioning as a button to play a round
     * @param event 
     */
    @FXML
    private void onImgPlay(MouseEvent event)
    {
        if (placedJetons.size() > 0)
        {
            imgPlay.setDisable(true);
            imgPlay.setOpacity(0.5);
            rectResetBets.setDisable(true);
            rectResetBets.setOpacity(0.5);
            roundPlayed = true;
            baccara.play();
        }
        else
        {
            //No bet placed
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No bet");
            alert.setContentText("You have to place at least one bet before playing.");
            alert.showAndWait();
        }
    }

    
    //-------------------- DRAG AND DROP WINDOW --------------------

     //Drag and drop for stage. Since we're using an undecorated stage, we had
     //to do the window dragging manually using the following two methods.
    
    /**
     * onMousePressed
     * Handles a mouse press on the top of the stage
     * @param event 
     */
    @FXML
    private void onPanePressed(MouseEvent event)
    {
        x = stage.getX() - event.getScreenX();
        y = stage.getY() - event.getScreenY();
    }

    /**
     * onMouseDragged
     * Moves the stage along the mouse when dragged
     * @param event 
     */
    @FXML
    private void onPaneDragged(MouseEvent event)
    {
        stage.setX(event.getScreenX() + x);
        stage.setY(event.getScreenY() + y);
    }

    
    //-------------------- JETON --------------------
    /**
     * onMousePressed
     * Handles a press on the jeton
     * @param event 
     */
    @FXML
    private void onJetonPressed(MouseEvent event)
    {
        x = jeton.getLayoutX() - event.getScreenX();
        y = jeton.getLayoutY() - event.getScreenY();
        //Make jeton smaller to fit onto the casino table better and make it more reactive.
        jeton.setScaleX(0.8);
        jeton.setScaleY(0.8);
    }

    /**
     * onMouseDragged
     * drags the jeton along the mouse position
     * @param event 
     */
    @FXML
    private void onJetonDragged(MouseEvent event)
    {
        jeton.setLayoutX(event.getScreenX() + x);
        jeton.setLayoutY(event.getScreenY() + y);
        updateBetType();
    }

    /**
     * onMouseReleased
     * Handles the release of the jeton, when dragged and dropped
     * @param event 
     */
    @FXML
    private void onJetonReleased(MouseEvent event)
    {
        if (betType != BetType.NONE)
        {
            System.out.println("You bet on: " + betType.toString());
            typeInJetonVal();
        }
        else
        {
            resetJeton();
        }
    }

    /**
     * Resets the current jeton
     * Is used before when the jeton isn't confirmed
     */
    private void resetJeton()
    {
        Timeline timeline = new Timeline();
        timeline.setOnFinished((ActionEvent actionEvent) ->
        {
            updateBetType();
        });
        moveNodeAnim(jeton, jetonOriginX, jetonOriginY, 400, timeline);
        jeton.setScaleX(1);
        jeton.setScaleY(1);
        txtFieldJeton.setVisible(false);
        btnJeton.setVisible(false);
        lblJeton.setVisible(false);
        txtFieldJeton.setDisable(true);
        btnJeton.setDisable(true);
        lblJeton.setDisable(true);
        lblJeton.setText("");
        txtFieldJeton.setText("");
    }
    
    /**
     * shows the hidden elements of the jeton to type in it's value
     */
    private void typeInJetonVal()
    {
        txtFieldJeton.setVisible(true);
        btnJeton.setVisible(true);
        txtFieldJeton.setDisable(false);
        btnJeton.setDisable(false);
        txtFieldJeton.requestFocus();
    }

    /**
     * confirm the placement of the current jeton and update everything related
     */
    private void confirmJeton()
    {
        //Copy the jeton's image, label and pane, then reset the original jeton
        ImageView tmpImg = new ImageView(imgJeton.getImage());
        tmpImg.setFitWidth(imgJeton.getFitWidth());
        tmpImg.setFitHeight(imgJeton.getFitHeight());

        Label tmpLbl = new Label(txtFieldJeton.getText());
        tmpLbl.setFont(lblJeton.getFont());
        tmpLbl.setPrefWidth(lblJeton.getPrefWidth());
        tmpLbl.setAlignment(lblJeton.getAlignment());
        tmpLbl.setLayoutX(lblJeton.getLayoutX());
        tmpLbl.setLayoutY(lblJeton.getLayoutY());
        double bet = Double.parseDouble(tmpLbl.getText());

        Pane tmpJeton = new Pane(tmpImg, tmpLbl);
        tmpJeton.setScaleX(jeton.getScaleX());
        tmpJeton.setScaleY(jeton.getScaleY());
        tmpJeton.setLayoutX(jeton.getLayoutX());
        tmpJeton.setLayoutY(jeton.getLayoutY());

        anchorPane.getChildren().add(tmpJeton);
        placedJetons.add(tmpJeton);

        resetJeton();

        //set the bet values in the Baccara class
        switch (betType)
        {
            case TIE:
                baccara.setBetsTie(bet);
                break;
            case BANKER:
                baccara.setBetsBanker(bet);
                break;
            case PLAYER:
                baccara.setBetsPlayer(bet);
                break;
        }
        Player.getInstance().setAccountBalance(-bet);
        updateView();
    }

    /**
     * handles the OK button on the jeton, when placing a bet
     */
    @FXML
    private void btnJetonAction()
    {
        btnJeton.setVisible(false);
        txtFieldJeton.setVisible(false);
        //TODO: Further validation needed e.g numeric, only 4 digits
        if (!validateJetonInput())
        {
            //TODO: Some sort of error notification for user
            resetJeton();
        }
        else
        {
            confirmJeton();
        }
        imgBetField.setImage(new Image("images/baccara/baccara_betfield.png"));
    }
    
    /**
     * Validates the input of the jeton when clicking on OK
     * @return false if input is invalid and true if input is valid
     */
    private boolean validateJetonInput()
    {
        return !(txtFieldJeton.getText().isEmpty() || Double.parseDouble(txtFieldJeton.getText()) > Player.getInstance().getAccountBalance());
    }

    /**
     * Checks where the player puts his bet and sets the image for the bet field accordingly, to mark it green
     */
    private void updateBetType()
    {
        if (jeton.getBoundsInParent().intersects(rectTie.getBoundsInParent()))
        {
            if (betType != BetType.TIE)
            {
                imgBetField.setImage(new Image("images/baccara/baccara_betfield_tie.png"));
            }
            betType = BetType.TIE;
        }
        else
        {
            if (jeton.getBoundsInParent().intersects(rectBanker.getBoundsInParent()))
            {
                if (betType != BetType.BANKER)
                {
                    imgBetField.setImage(new Image("images/baccara/baccara_betfield_banker.png"));
                }
                betType = BetType.BANKER;
            }
            else
            {
                if (jeton.getBoundsInParent().intersects(rectPlayer.getBoundsInParent()))
                {
                    if (betType != BetType.PLAYER)
                    {
                        imgBetField.setImage(new Image("images/baccara/baccara_betfield_player.png"));
                    }
                    betType = BetType.PLAYER;
                }
                else
                {
                    if (betType != BetType.NONE)
                    {
                        imgBetField.setImage(new Image("images/baccara/baccara_betfield.png"));
                    }
                    betType = BetType.NONE;
                }
            }
        }
    }
    
    /**
     * removes all placed jetons from the game field
     */
    private void resetPlacedJetons()
    {
        for(Pane j : placedJetons)
        {
            Timeline timeline = new Timeline();
            moveNodeAnim(j, jetonOriginX, jetonOriginY, 300, timeline);
            
            timeline.setOnFinished((ActionEvent actionEvent) ->
            {
                anchorPane.getChildren().remove(j);
            });
        }
        placedJetons.clear();
    }
    
    /**
     * Resets your current bets
     */
    @FXML
    private void onResetBet()
    {
        resetPlacedJetons();
        resetBet();
        updateView();
    }
    
    private void resetBet()
    {
        Player.getInstance().setAccountBalance(baccara.getBetsBanker() + baccara.getBetsPlayer() + baccara.getBetsTie());
        baccara.setBetsBanker(-baccara.getBetsBanker());
        baccara.setBetsPlayer(-baccara.getBetsPlayer());
        baccara.setBetsTie(-baccara.getBetsTie());
    }


    //-------------------- PLAY CARDS --------------------
    /**
     * Playcards
     * @param cardID is the abbrevation of a card's name in the image. E.g 9 Club --> 9C
     *               Cards above 10 (Jack, Queen, King) will continue with 11, 12, 13 and 1 is an ace
     * @param slot 0, 1 = player slots for 1st two cards, 4 = player slot for 3rd card 
     *             2, 3 = banker slots for 1st two cards, 5 = banker slot for 3rd card
     *
     */
    public void placeCard(String cardID, int slot)
    {
        if (cardSlots[slot].getChildren().isEmpty())
        {
            Timeline timeline = new Timeline();
            ImageView c = new ImageView("/images/cards/red_back.png");
            Pane cs = cardSlots[slot];
            c.setFitWidth(cs.getWidth());
            c.setFitHeight(cs.getHeight());
            if (slot <= 1 || slot == 4) //place card from left side
            {
                c.setLayoutX(-c.getFitHeight());
                /*timeline.setOnFinished((ActionEvent actionEvent) ->
                {
                    Timeline pause = new Timeline(new KeyFrame(
                        Duration.millis(500),
                        ae -> flipCard(slot)));
                        pause.play();
                });*/
            }
            else //place card from right side
            {
                c.setLayoutX(c.getFitWidth() + stage.getWidth());
            }
            c.setLayoutY(cs.getLayoutY());
            anchorPane.getChildren().add(c);

            /*Weird positional behaviour
            Wanted to get the card out of a slot by accessing it's children
            Now using the array cards instead*/
            //cs.getChildren().add(c);
            /*Misusing the id for the card's ID, because I don't want to make an additional
            child class extending ImageView only for one attribute (cardID).*/
            c.setId(cardID);
            cards[slot] = c;
            moveNodeAnim(c, cs.getLayoutX(), cs.getLayoutY(), 300, timeline);
        }
    }
    
    /**
     * drops and deletes all cards in the game
     */
    public void dropCards()
    {
        for (ImageView card : cards)
        {
            if (card != null)
            {
                double x1 = card.getLayoutX();
                double y1 = card.getFitHeight() + stage.getHeight();
                Timeline timeline = new Timeline();

                timeline.setOnFinished((ActionEvent actionEvent) ->
                {
                    anchorPane.getChildren().remove(card);
                });

                moveNodeAnim(card, x1, y1, 500, timeline);
            }
        }
    }

    /**
     * flips the card on a specific slot
     * @param slot is the card's slot it is sitting on
     */
    public void flipCard(int slot)
    {
        ImageView c = cards[slot];
        c.setImage(new Image("/images/cards/" + c.getId() + ".png"));
    }

    public void flipCards(int[] slots)
    {
        for (int slot : slots)
        {
            ImageView c = cards[slot];
            c.setImage(new Image("/images/cards/" + c.getId() + ".png"));
        }
    }
    
    // -------------------- ANIMATION -------------------- 
    /**
     * Moves any node linearly from it's position to another position
     * @param n is the node that should be moved
     * @param timeline added to control the timeline from outside
     * @param x is the target x coordinate
     * @param y is the target y coordinate
     * @param ms determines how long the node needs from A to B in milliseconds
     */
    private void moveNodeAnim(Node n, double x, double y, int ms, Timeline timeline)
    {
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(n.layoutXProperty(), x, Interpolator.EASE_OUT);
        final KeyFrame kf = new KeyFrame(Duration.millis(ms), kv);

        final KeyValue kv2 = new KeyValue(n.layoutYProperty(), y, Interpolator.EASE_OUT);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(ms), kv2);
        timeline.getKeyFrames().addAll(kf, kf2);
        timeline.play();
    }
    
    
    // -------------------- UPDATE -------------------- 
    /**
     * updates the view, respectively the balance and bet label
     */
    public void updateView()
    {
        double balance = Player.getInstance().getAccountBalance();
        lblBalance.setText("Balance: " + Double.toString(balance) + "CHF");

        double bets = baccara.getBetsTie() + baccara.getBetsBanker() + baccara.getBetsPlayer();
        lblBet.setText("Bet: " + Double.toString(bets) + "CHF");
    }
    
    
    // -------------------- WINNER STAGE -------------------- 
    /**
     * Opens a stage over the main stage, like a popup, showing the winner
     * @param msg is the message the stage should show
     */
    public void showWinner(String msg)
    {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setResizable(false);
        dialog.initStyle(StageStyle.UNDECORATED);
        
        AnchorPane dialogAnchorPane = new AnchorPane();     
        Scene dialogScene = new Scene(dialogAnchorPane, 300, 200);
        dialog.setScene(dialogScene);
        
        Label lbl = new Label(msg);
        lbl.setPrefWidth(dialogAnchorPane.getWidth());
        lbl.setAlignment(Pos.CENTER);
        
        lbl.setLayoutY(50);
        dialogAnchorPane.getChildren().add(lbl);
        
        Button btn = new Button();
        btn.setText("OK");
        btn.setPrefWidth(50);
        btn.setLayoutX(dialogAnchorPane.getWidth() / 2 - btn.getPrefWidth() / 2);
        btn.setLayoutY(100);
        btn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                dropCards();
                resetPlacedJetons();
                baccara.resetBaccara();
                updateView();
                imgPlay.setDisable(false);
                imgPlay.setOpacity(1);
                rectResetBets.setDisable(false);
                rectResetBets.setOpacity(1);
                roundPlayed = false;
                dialog.close();
            }
        });
        dialogAnchorPane.getChildren().add(btn);
        
        dialog.show();
    }
    
    
    // -------------------- HOVER EFFECT FOR "BUTTONS" -------------------- 
    /**
     * onMouseEntered
     * Is triggered when hovering over an ImageView functioning as a button, with a banner as image 
     * Highlights the "button", respectively ImageView, by changing it's image
     * @param event 
     */
    @FXML
    private void highlightBannerButton(MouseEvent event)
    {
        ((ImageView) event.getSource()).setImage(new Image("images/btn_highlighted.png"));
    }

    /**
     * onMouseExited
     * Is triggered when not hovering over an ImageView functioning as a button, with a banner as image anymore
     * Unhighlights the "button", respectively ImageView, by changing it's image
     * @param event 
     */
    @FXML
    private void unHighlightBannerButton(MouseEvent event)
    {
        ((ImageView) event.getSource()).setImage(new Image("images/btn.png"));
    }
    
    /**
     * onMouseEntered
     * Is triggered when hovering over an ImageView functioning as a button, with a round icon as image 
     * Highlights the "button", respectively ImageView, by changing it's image
     * Used for the round window buttons
     * @param event 
     */
    @FXML
    private void highlightRoundButton(MouseEvent event)
    {
        ((ImageView) event.getSource()).setImage(new Image("images/btn_toolbar_highlighted.png"));
    }

    /**
     * onMouseExited
     * Is triggered when not hovering over an ImageView functioning as a button, with a banner as image anymore
     * Unhighlights the "button", respectively ImageView, by changing it's image
     * Used for the round window buttons
     * @param event 
     */
    @FXML
    private void unHighlightRoundButton(MouseEvent event)
    {
        ((ImageView) event.getSource()).setImage(new Image("images/btn_toolbar.png"));
    }

    @FXML
    private void highlightResetBtn(MouseEvent event)
    {
        ((Rectangle) event.getSource()).setFill(Color.rgb(177, 36, 25));
    }
    
    @FXML
    private void unhighlightResetBtn(MouseEvent event)
    {
        ((Rectangle) event.getSource()).setFill(Color.rgb(210, 52, 31));
    }
    

    //-------------------- HANDLE WINDOW BUTTONS --------------------
    /**
     * Handles the exit window button at the top right
     * Exits the application
     * @param event 
     */
    @FXML
    private void exit(MouseEvent event)
    {
        stage.close();
    }

    /**
     * Handles the minimize window button at the top right
     * Minimizes the stage
     * @param event 
     */
    @FXML
    private void minimize(MouseEvent event)
    {
        stage.setIconified(true);
    }

    /**
     * Handles the info window button at the top right
     * Shows the game's rules
     * @param event 
     */
    @FXML
    private void info(MouseEvent event)
    {
        //Show game rules
        Stage popup = new Stage();
        popup.initOwner(stage);
        popup.initStyle(StageStyle.UTILITY);
        popup.setResizable(true);
        
        AnchorPane popupAnchorPane = new AnchorPane();     
        Scene scene = new Scene(popupAnchorPane, stage.getWidth() - 100, stage.getHeight() - 100);
        popup.setScene(scene);
        
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        webEngine.load("https://www.casino-games-online.biz/cards/chemin-de-fer.html");
        browser.setPrefWidth(scene.getWidth());
        browser.setPrefHeight(scene.getHeight());
        
        popupAnchorPane.getChildren().add(browser);
        
        popup.show();
    }

    
    //-------------------- SETTERS FOR SCORE --------------------
    public void setLblScorePText(String text)
    {
        lblScoreP.setText(text);
    }

    public void setLblScoreBText(String text)
    {
        lblScoreB.setText(text);
    }
}