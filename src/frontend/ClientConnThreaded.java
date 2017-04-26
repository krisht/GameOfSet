package frontend;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static frontend.GameBoard_Front.*;
import static frontend.LandingPage.*;
import static frontend.LoginPage.*;


public class ClientConnThreaded extends JFrame implements Runnable {


    public static ArrayList<GameListing> listofGames = new ArrayList<GameListing>();
    final int GAME_DOES_NOT_EXIST = 1;
    final int GAME_FULL = 2;
    final int GENERAL_ERROR = -1;
    final int SUCCESS = 3;
    final int GAME_NAMAE_ALREADY_EXISTS = 4;
    private Thread t;
    private String threadName;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientConnThreaded() {
        try {
//            socket = new Socket("199.98.20.115", 5000);
            socket = new Socket("127.0.0.1", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.err.println("Connected to host successfully!");

        } catch (IOException exc) {
            System.err.println("ERROR COMMUNICATING WITH SERVER");
            JOptionPane.showMessageDialog(null, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        threadName = "Main Conn";

    }


    public void run() {
        JSONObject msg;
        String inString;
        try {

            while (true) {
                if ((inString = in.readLine()) != null) {
                    System.err.println("Received: " + inString);
                    try {
                        JSONObject data = new JSONObject(inString);
                        String fCall = data.getString("fCall");
                        switch (fCall) {
                            case "updateGameResponse":
                                JSONArray gameboard = data.getJSONObject("gameboard").getJSONArray("board");
                                JSONArray scoreboard_usernames = data.getJSONArray("scoreboard_usernames");
                                JSONArray scoreboard_scores = data.getJSONArray("scoreboard_scores");
                                gameName = data.getString("gamename");
                                list_of_cardids.clear();
                                list_of_users.clear();
                                for (int i = 0; i < gameboard.length(); i++) {
                                    list_of_cardids.add(gameboard.getInt(i));
                                }
                                for (int i = 0; i < scoreboard_scores.length(); i++) {
                                    Friends tempfriend = new Friends(scoreboard_usernames.getString(i), scoreboard_scores.getInt(i), 0);
                                    list_of_users.add(tempfriend);
                                }
                                gb.updateGameBoard();
                                gb.updateLeaderboard();
                                break;
                            case "joinGameResponse":
                                if (data.getInt("uid") == uid) {
                                    switch (data.getInt("returnValue")) {
                                        case GAME_DOES_NOT_EXIST:
                                            JOptionPane.showMessageDialog(null, "Game no longer exists. Please click refresh.", "Error", JOptionPane.ERROR_MESSAGE);
                                            gameName = "";
                                            gid = -1;
                                            break;
                                        case GAME_FULL:
                                            JOptionPane.showMessageDialog(null, "Game is already full.", "Error", JOptionPane.ERROR_MESSAGE);
                                            gameName = "";
                                            gid = -1;
                                            break;
                                        case SUCCESS:
                                            gid = data.getInt("gid");
                                            landingPage.enterGame();
                                            break;
                                    }
                                } else {
                                    System.err.println("New player has joined.");
                                }
                                break;
                            case "createGameResponse":
                                switch (data.getInt("returnValue")) {
                                    case GENERAL_ERROR:
                                        break;
                                    case GAME_NAMAE_ALREADY_EXISTS:
                                        JOptionPane.showMessageDialog(null, "Game name is already taken. Please choose another name and try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        gameName = "";
                                        gid = -1;
                                        break;
                                    case SUCCESS:
                                        gid = data.getInt("gid");
                                    	landingPage.enterGame();
                                    	posinlist = 0;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case "userSubmitsResponse":
                                if (data.getInt("uid") == uid){
                                    switch (data.getInt("returnValue")) {
                                        case 0:
                                            JOptionPane.showMessageDialog(null, "Not a set. Sorry. Sucks to suck.", "Error", JOptionPane.ERROR_MESSAGE);
                                            break;
                                        case 1:
                                            JOptionPane.showMessageDialog(null, "You made a set, you bloody genius.", "YAY!!!", JOptionPane.PLAIN_MESSAGE);
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    switch (data.getInt("returnValue")) {
                                        case 1:
                                            JOptionPane.showMessageDialog(null, "Someone scored.", "Bleh!!!", JOptionPane.PLAIN_MESSAGE);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            case "loggingOutResponse":
                                //LOGOUT
                                break;
                            case "updatePublicChat":
                                updateChat(data.getString("username"), data.getString("msg"));
                                break;
                            case "updateLocalChat":
                                updateChat(data.getString("username"), data.getString("msg"));
                                break;
                            case "playerScoreResponse":
                            	lifetime_score = data.getInt("score");
                            	landingPage.reset_user_score();
                            	break;
                            case "getGameListingResponse":
                                JSONArray gameList = data.getJSONArray("gamesList");
                                listofGames.clear();
                                for (int i = 0; i < gameList.length(); i++) {
                                    JSONObject gameitem = gameList.getJSONObject(i);
                                    listofGames.add(new GameListing(   gameitem.getInt("gid"),
                                                                        gameitem.getString("gameName"),
                                                                        gameitem.getString("username1"),
                                                                        gameitem.getString("username2"),
                                                                        gameitem.getString("username3"),
                                                                        gameitem.getString("username4")));
                                }
                                landingPage.makeGameListings();
                                break;
                            case "leaveGameResponse":
                                if (data.getInt("uid") == uid) {
                                    StringBuilder leavemsg = new StringBuilder();
                                    for (int i = 0; i < list_of_users.size(); i++) {
                                        int blah = list_of_users.get(i).getName().compareTo(username);
                                        if (blah == 1) {
                                            posinlist = i;
                                            break;
                                        }
                                    }
                                    leavemsg.append("Leaving game with a final score of ");
                                    leavemsg.append(list_of_users.get(posinlist).getScore());
                                    JOptionPane.showMessageDialog(null, leavemsg, "YAY!!!", JOptionPane.PLAIN_MESSAGE);
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void messageServer(JSONObject obj) throws Exception {

        try {
            String request = obj.toString();
            this.out.println(request);
            System.err.println("Sending: " + request);
        } catch (Exception ex) {
            System.err.println("Error: Could not send to server.");
        }
    }

    public void updateChat(String chatUserName, String chatMessage) {
        StringBuilder chatitem = new StringBuilder();
        chatitem.append(chatUserName);
        chatitem.append(": ");
        chatitem.append(chatMessage);
        chatitem.append("\n");
        chatlogarea.append(chatitem.toString());
    }


    public int loginUser(String username, String password) {
        JSONObject obj = new JSONObject();

        obj.put("fCall", "loginUser");
        obj.put("login", username);
        obj.put("pass", password);
        try {
            messageServer(obj);
        } catch (Exception e) {
            return -1; //some error
        }
        try {
            String fCall = "";
            String inobjString;
            while ((inobjString = in.readLine()) != null) {
                System.err.println(inobjString);
                JSONObject inobj = new JSONObject(inobjString);
                fCall = inobj.getString("fCall");
                if (fCall.equals("loginResponse")) {
                    uid = inobj.getInt("uid");
                    return inobj.getInt("returnValue");
                }
            }
        } catch (Exception e) {
            System.err.println("Error");
            return -1;
        }
        return 0;
    }

    public int registerUser(String username, String password) {
        JSONObject obj = new JSONObject();

        obj.put("fCall", "registerUser");
        obj.put("login", username);
        obj.put("pass", password);
        try {
            messageServer(obj);
        } catch (Exception e) {
            return -1; //some error
        }
        try {
            String fCall = "";
            String inobjString;
            while ((inobjString = in.readLine()) != null) {
                JSONObject inobj = new JSONObject(inobjString);
                fCall = inobj.getString("fCall");
                if (fCall.equals("registerResponse")) {
                    uid = inobj.getInt("uid");
                    return inobj.getInt("returnValue");
                }
            }
        } catch (Exception e) {
            System.err.println("Error");
            return -1;
        }
        return 0;
    }

}


