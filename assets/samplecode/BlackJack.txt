/** CMPT 201, Fall 2005
  * Blackjack.Java
  * Program made by: Jordan Arnold.
  A class representing a blackjack game without gambling.
  Programmer's notes: This program, as said in the previous line,
  represents blackjack without any gambling in it.  This allows the user to
  hit, or stay.  This program will tell what kinds of cards player will receive
  and will end if there is already a blackjack.
  */

import java.util.Scanner;

public class BlackJack {
  private Player player;
  private Dealer dealer = new Dealer();
  private Scanner keyboard = new Scanner(System.in);
  private int current = 2;
  private Card temp;
  private boolean gameOver;      // set to false when a blackjack game begins
  // set to true when a blackjack game ends
  
  // write a default constructor
  
  // prompt the user for the player's name and set it
  private void getName() {
    player.setName();
  }
  
  // deals two cards to the player and dealer.  
  // handles blackjack if it occurs for either the player, the dealer, or both.
  private void startRound() {
    gameOver = false;
    current = 2;
    dealer.newGame();
    player.newGame();
    dealer.theHand[0] = dealer.dealCard();
    dealer.theHand[1] = dealer.dealCard();
    player.theHand[0] = dealer.dealCard();
    player.theHand[1] = dealer.dealCard();
    for(int i = 0; i < 2; i++){
      //System.out.println(player.theHand[i] + ":" + i);
      temp = player.theHand[i];
      player.addCard(temp);
      temp = dealer.theHand[i];
      dealer.insertCard(temp);
    }    
    dealer.showHiddenHand();
    player.showHand();
    end();
  }      
  
  // deals one card to the player
  // handles all cases where that card ends the game
  private void hit(){
    player.theHand[current] = dealer.dealCard();
    temp = player.theHand[current];
    System.out.println("You get a " + temp);
    player.addCard(temp);
    current++;
    end();
  }
  
  // finishes playing the dealer's hand
  // handles the end of the game after the dealer is done playing
  private void finishRound() {
    current = 2;
    while(dealer.theHand[current] != null);{
      dealer.showHand();
      dealer.theHand[current] = dealer.endRound();
      temp = dealer.theHand[current];
      if(temp != null){
        System.out.println("Dealer draws a " + dealer.theHand[current]);
        dealer.insertCard(temp);
        current++;
      }
      else{
        System.out.println("Dealer stays.");
      }
      dealer.showHand();
      player.showHand();
    }
    end();
    if(!gameOver){
      if(player.theScore() > dealer.theScore()){
        player.addWins();
      }
      else if(player.theScore() == dealer.theScore()){
        player.addDraw();
      }
      else
        player.addLoss();
    }
  }
  
  // prints to the screen what the dealer is showing
  // and what the player's cards are
  private void reportStatus() {
    dealer.showHiddenHand();
    player.showHand();
  }
  
  private void end(){ 
    if(player.endGame() == 1){
      if(dealer.endGame() == 1){
        player.addDraw();
        gameOver = true;
        return;
      }
    }
    if(player.endGame() == 1){
      player.addWins();
      gameOver = true;
    }
    if(player.endGame() == 2){
      player.addLoss();
      gameOver = true;
    }
    if(dealer.endGame() == 1){
      dealer.showHand();
      player.addLoss();
      gameOver = true; 
    }
    if(dealer.endGame() == 2){
      player.addWins();
      gameOver = true;
    }
  }
  
  /** starts a series of blackjack games between the dealer and one player:
    *  rules are:
    *       tie if both dealer and player have blackjack, or if they have the same score
    *       otherwise whoever has blackjack wins
    *       if no one has blackjack, then the dealer wins if:
    *            the player busts (player's hand goes over 21)
    *            the dealer's hand has a higher score than the player's (when no one busts)
    *       the player wins if:
    *            the player has a seven cards without busting (going over 21)
    *            the player's hand has a higher score than the dealer's (when no one busts)
    *            the player's hand does not bust but the dealer's hand does
    */
  public void play() {
    String input;
    
    // get player's name
    player = new Player();
    getName();
    System.out.println(player.getName() + ", let's play blackjack!");
    
    // play rounds of blackjack until the player says to stop
    do {
      playRound();
      System.out.println(player);
      System.out.println("Would you like to play again? [Y]es or [N]o? ");
      input = keyboard.nextLine().toLowerCase();
      while (input.length() != 1 || (!input.equals("y") && !input.equals("n") )) {
        System.out.println("Please answer y or n.");
        input = keyboard.nextLine().toLowerCase();
      }
      System.out.println("============================================");
    } while (input.equals("y"));
    
    System.out.println("Thanks for playing!");       
  }
  
  // plays one round of blackjack 
  // ends when:
  //   either player gets blackjack, or
  //   player busts (goes over 21), or
  //   player has seven cards (automatic win), or
  //   player chooses to stand, or
  //   dealer's hand reaches 17 or higher
  private void playRound() {
    String input;
    
    // deal 2 cards to everyone
    startRound();
    
    // check if game's already over (blackjack)
    if (gameOver)
      return;
    
    // give player chance to hit
    do {
      System.out.println(player.getName() + ", what would you like to do? [H]it or [S]tay? ");
      input = keyboard.nextLine().toLowerCase();
      while (input.length() != 1 || (!input.equals("h") && !input.equals("s")) ) {
        System.out.println("Please enter h or s.");
        input = keyboard.nextLine().toLowerCase();
      }
      System.out.println("---------------------------------");
      if (input.equals("h")){
        hit();
        reportStatus();
      }
    } while (!gameOver && input.equals("h"));
    
    // player didn't bust:  dealer finishes dealer's hand
    if (!gameOver) 
      finishRound();
  }
  
  public static void main(String[] args) {
    BlackJack bj = new BlackJack();
    bj.play();
  }
}

