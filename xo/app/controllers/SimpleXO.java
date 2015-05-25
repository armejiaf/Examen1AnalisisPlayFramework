package controllers;

import play.mvc.*;
import play.libs.F.*;

import java.util.*;

public class SimpleXO {

    // collect all websockets here
    private static HashMap<String, WebSocket.Out<String>> connections = new HashMap<>();
    private static char board[] = {' ', ' ', ' ', ' ',' ', ' ', ' ', ' ', ' '};
    private static char currentPlayer = 'X';

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {
        String id = generateID();
        connections.put(id, out);
        in.onMessage(new Callback<String>() {
            public void invoke(String event) {
                SimpleXO.parseInput(event);
            }
        });

        in.onClose(new Callback0() {
            public void invoke() {
                SimpleXO.notifyAll("A connection closed");
            }
        });
        out.write(id);
    }

    //Decides what should be done with the input.
    public static void parseInput(String message) {
        if (message.contains("Close")) {
            connections.get(message.split("~")[1]).close();
            connections.remove(message.split("~")[1]);
            restartGame();
            return;
        }
        if(message.contains("Restart")){
            restartGame();
            return;
        }
        if (message.split("~").length == 3) {
            runValidations(message);
        }
    }

    // Iterate connection list and send the message to all connections
    public static void notifyAll(String message) {
        for (Map.Entry<String, WebSocket.Out<String>> entry : connections.entrySet()) {
            entry.getValue().write(message);
        }
    }

    //Run all game validations
    public static void runValidations(String element) {
        if (isPlayerTurn(element.split("~")[1])) {
            if (isValidClickedSquare(element)) {
                doMovement(element);
                notifyAll(stringifyBoard() + "~" + currentPlayer);
                char winValidationResult = validateWin();
                if (winValidationResult != ' ') { //do win case
                    if(winValidationResult == '3'){
                        notifyAll("Draw!");
                    }else{
                        notifyAll("Player " + winValidationResult + " has won!");
                    }
                    restartGame();
                } else {
                    changeTurns();
                }
            } else {
                connections.get(element.split("~")[2]).write("That square is already marked!");
            }
        } else {
            connections.get(element.split("~")[2]).write("It's not your turn yet!");
        }
    }

    //Checks that the clicked square is not occupied already
    public static boolean isValidClickedSquare(String element) {
        return board[Integer.parseInt(element.split("~")[0])] == ' ';
    }

    public static void doMovement(String element) {
        board[Integer.parseInt(element.split("~")[0])] = element.split("~")[1].charAt(0);
    }

    //Checks whether it's the clicking player's turn
    public static boolean isPlayerTurn(String player) {
        return player.charAt(0) == currentPlayer;
    }

    public static void changeTurns() {
        currentPlayer = currentPlayer == 'O' ? 'X' : 'O';
    }

    //generated a Unique ID for each client connected to the server
    public static String generateID() {
        String UID;
        do {
            UID = java.util.UUID.randomUUID().toString();
        } while (connections.containsKey(UID));
        return UID;
    }

    public static void restartGame() {
        for (int i = 0; i < board.length; i++)
            board[i] = ' ';
        currentPlayer = 'X';
        notifyAll(stringifyBoard() + "~" + "O");
    }

    public static String stringifyBoard() {
        String toReturn = "";
        for (int i = 0; i < board.length; i++) {
            toReturn += board[i];
            if (i != board.length - 1)
                toReturn += ",";
        }
        return toReturn;
    }

    public static char validateWin(){
        for(int i = 0; i <= 6; i+=3){
            if(board[i] == board[i+1] && board[i] == board[i+2] && board[i] != ' ')
                return board[i];
        }
        for(int i = 0; i <= 2; i++){
            if(board[i] == board[i+3] && board[i] == board[i+6] && board[i] != ' ')
                return board[i];
        }
        if((board[0] == board[4] && board[0] == board[8]) || (board[2] == board[4] && board[2] == board[6]))
            return board[4];
        for(int i = 0; i < board.length; i++){
            if(board[i] == ' ')
                return ' ';
        }
        return '3';
    }
}