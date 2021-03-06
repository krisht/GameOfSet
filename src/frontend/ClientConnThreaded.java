package frontend;


import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static frontend.GameBoard_Front.*;
import static frontend.LandingPage.*;
import static frontend.LoginPage.*;


public class ClientConnThreaded extends JFrame implements Runnable {


    static ArrayList<GameListing> listofGames = new ArrayList<>();
    private final int GAME_DOES_NOT_EXIST = 1;
    private final int GAME_FULL = 2;
    private final int GENERAL_ERROR = -1;
    private final int SUCCESS = 3;
    private final int GAME_NAMAE_ALREADY_EXISTS = 4;
    private Thread t;
    private String threadName;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientConnThreaded() {
        try {
            socket = new Socket("199.98.20.122", 5000);
            //socket = new Socket("127.0.0.1", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.err.println("Connected to host successfully!");

        } catch (IOException exc) {
            System.err.println("ERROR COMMUNICATING WITH SERVER");
            JOptionPane.showMessageDialog(null, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        threadName = "Main Conn";

    }

    public void run() {
        JSONObject msg;
        String inString;
        StyledDocument doc;
        Style GameStyle;
        Style gameSystemStyle;
        try {

            while (true) {
                if ((inString = in.readLine()) != null) {
                    System.err.println("Received: " + inString);
                    try {
                        JSONObject data = new JSONObject(inString);
                        String fCall = data.getString("fCall");
                        switch (fCall) {
                            case "updateGameResponse":
                                if (data.getInt("gid") == gid) {
                                    JSONArray gameboard = data.getJSONObject("gameboard").getJSONArray("board");
                                    JSONArray scoreboard_usernames = data.getJSONArray("scoreboard_usernames");
                                    JSONArray scoreboard_scores = data.getJSONArray("scoreboard_scores");
                                    JSONArray scoreboard_nomoresets = data.getJSONArray("nomoresets");
                                    gameName = data.getString("gamename");
                                    list_of_cardids.clear();
                                    list_of_users.clear();
                                    for (int i = 0; i < gameboard.length(); i++) {
                                        list_of_cardids.add(gameboard.getInt(i));
                                    }
                                    for (int i = 0; i < scoreboard_scores.length(); i++) {
                                        Friends tempfriend = new Friends(scoreboard_usernames.getString(i), scoreboard_scores.getInt(i), scoreboard_nomoresets.getInt(i));
                                        list_of_users.add(tempfriend);
                                    }
                                    gb.updateGameBoard();
                                    gb.updateLeaderboard();
                                    int nomoresetsnum = 0;
                                    for (int i = 0; i < scoreboard_nomoresets.length(); i++) {
                                        nomoresetsnum = nomoresetsnum + scoreboard_nomoresets.getInt(i);
                                    }
                                    if (nomoresetsnum == scoreboard_nomoresets.length()) {
                                        doc = gb.chatlogarea.getStyledDocument();
                                        GameStyle = doc.getStyle("Game");
                                        gameSystemStyle = doc.getStyle("GameSystem");
                                        doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                        doc.insertString(doc.getLength(), "Adding more cards to the board!", GameStyle);
                                        doc.insertString(doc.getLength(), "\n", GameStyle);
                                        gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                    }
                                }
                                break;
                            case "joinGameResponse":
                                doc = landingPage.chatlogarea.getStyledDocument();
                                GameStyle = doc.getStyle("System");
                                gameSystemStyle = doc.getStyle("GameSystem");
                                if (data.getInt("uid") == uid) {
                                    switch (data.getInt("returnValue")) {
                                        case GAME_DOES_NOT_EXIST:
                                            doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                            doc.insertString(doc.getLength(), "Game no longer exists. Please click refresh!", GameStyle);
                                            doc.insertString(doc.getLength(), "\n", GameStyle);
                                            landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
                                            gameName = "";
                                            gid = -1;
                                            break;
                                        case GAME_FULL:
                                            doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                            doc.insertString(doc.getLength(), "Game is already full. Please try another game!", GameStyle);
                                            doc.insertString(doc.getLength(), "\n", GameStyle);
                                            landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
                                            gameName = "";
                                            gid = -1;
                                            break;
                                        case SUCCESS:
                                            gid = data.getInt("gid");
                                            landingPage.enterGame();
                                            break;
                                        case GENERAL_ERROR:
                                            doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                            doc.insertString(doc.getLength(), "Could not process Join Game request. Please Refresh and try again.", GameStyle);
                                            doc.insertString(doc.getLength(), "\n", GameStyle);
                                            landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
                                            gameName = "";
                                            gid = -1;
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
                                        doc = landingPage.chatlogarea.getStyledDocument();
                                        GameStyle = doc.getStyle("System");
                                        gameSystemStyle = doc.getStyle("GameSystem");
                                        doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                        doc.insertString(doc.getLength(), "Game name is already taken. Please choose another name and try again.", GameStyle);
                                        doc.insertString(doc.getLength(), "\n", GameStyle);
                                        landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
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
                                doc = gb.chatlogarea.getStyledDocument();
                                GameStyle = doc.getStyle("Game");
                                gameSystemStyle = doc.getStyle("GameSystem");
                                switch (data.getInt("returnValue")) {
                                    case 0:
                                        if (data.getInt("uid") == uid) {
                                            try {
                                                doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                                doc.insertString(doc.getLength(), "Sorry, that's not a set! Please try again!", GameStyle);
                                                doc.insertString(doc.getLength(), "\n", GameStyle);
                                            }
                                            catch (BadLocationException e) {
                                                e.printStackTrace();
                                            }
                                            gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                            gb.resetBorders();
                                        }
                                        break;
                                    case 1:
                                        if (data.getInt("uid") == uid) {
                                            try {
                                                doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                                doc.insertString(doc.getLength(), "You scored!", GameStyle);
                                                doc.insertString(doc.getLength(), "\n", GameStyle);
                                            }
                                            catch (BadLocationException e) {
                                                e.printStackTrace();
                                            }
                                            gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                         } else {
                                            try {
                                                doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                                doc.insertString(doc.getLength(), data.getString("username"), GameStyle);
                                                doc.insertString(doc.getLength(), " scored!", GameStyle);
                                                doc.insertString(doc.getLength(), "\n", GameStyle);
                                            }
                                            catch (BadLocationException e) {
                                                e.printStackTrace();
                                            }
                                            gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                        }
                                        gb.resetBorders();
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case "loggingOutResponse":
                                System.exit(0);
                                break;
                            case "updatePublicChat":
                                updatePublicChat(data.getString("username"), data.getString("msg"));
                                break;
                            case "updateGameChat":
                                updateLocalChat(data.getString("username"), data.getString("msg"));
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
                                doc = landingPage.chatlogarea.getStyledDocument();
                                GameStyle = doc.getStyle("System");
                                gameSystemStyle = doc.getStyle("GameSystem");
                                if (data.getInt("uid") == uid) {
                                    if (data.getInt("returnValue") == 1) {
                                        StringBuilder leavemsg = new StringBuilder();
                                        gb.returnToLanding();
                                        gid = -1;
                                        landingPage.getUserScore();
                                        landingPage.requestupdateServerList();
                                        try {
                                            for (int i = 0; i < list_of_users.size(); i++) {
                                                if (list_of_users.get(i).getName().equals(username)){
                                                    posinlist = i;
                                                    break;
                                                }
                                            }
                                            leavemsg.append("Left game with a final score of ");
                                            leavemsg.append(list_of_users.get(posinlist).getScore());
                                            try {
                                                doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                                doc.insertString(doc.getLength(), leavemsg.toString(), GameStyle);
                                                doc.insertString(doc.getLength(), "\n", GameStyle);
                                            }
                                            catch (BadLocationException e) {
                                                e.printStackTrace();
                                            }
                                            gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        doc = gb.chatlogarea.getStyledDocument();
                                        GameStyle = doc.getStyle("Game");
                                        gameSystemStyle = doc.getStyle("GameSystem");
                                        try {
                                            doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                            doc.insertString(doc.getLength(), "Could not leave game. Please try again.", GameStyle);
                                            doc.insertString(doc.getLength(), "\n", GameStyle);
                                        }
                                        catch (BadLocationException e) {
                                            e.printStackTrace();
                                        }
                                        gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
                                    }
                                }
                                break;
                            case "gameOverResponse":
                                doc = landingPage.chatlogarea.getStyledDocument();
                                GameStyle = doc.getStyle("System");
                                gameSystemStyle = doc.getStyle("GameSystem");
                                if (data.getInt("gid") == gid) {
                                    StringBuilder gameovermsg = new StringBuilder();
                                    gameovermsg.append("Game is over! ");
                                    ArrayList<Integer> winpos = new ArrayList<Integer>();
                                    int winscore = 0;
                                    int selfpos = -1;
                                    for (int i = 0; i < list_of_users.size(); i++) {
                                        if (list_of_users.get(i).getScore() == winscore) {
                                            winpos.add(i);
                                        } else if (list_of_users.get(i).getScore() > winscore) {
                                            winpos.clear();
                                            winpos.add(i);
                                            winscore = list_of_users.get(i).getScore();
                                        }
                                        if (list_of_users.get(i).getName().equals(username)){
                                            selfpos = i;
                                        }
                                    }
                                    gb.returnToLanding();
                                    if (winpos.size() > 1) {
                                        gameovermsg.append("It's a draw between ");
                                        for (int i = 0; i < winpos.size() - 1; i++) {
                                            gameovermsg.append(list_of_users.get(winpos.get(i)).getName());
                                            gameovermsg.append(", ");
                                        }
                                        gameovermsg.append("and ");
                                        gameovermsg.append(list_of_users.get(winpos.get(winpos.size() - 1)).getName());
                                        gameovermsg.append(" with a final score of ");
                                        gameovermsg.append(winscore);
                                    } else {
                                        gameovermsg.append(list_of_users.get(winpos.get(0)).getName());
                                        gameovermsg.append(" won the game with a score of ");
                                        gameovermsg.append(winscore);
                                        gameovermsg.append("!");
                                    }
                                    doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                    doc.insertString(doc.getLength(), gameovermsg.toString(), GameStyle);
                                    doc.insertString(doc.getLength(), "\n", GameStyle);
                                    landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
                                    gid = data.getInt("gid");
                                    gb.leavegameRequest();
                                    landingPage.getUserScore();
                                    landingPage.requestupdateServerList();
                                }
                                break;
                            case "noMoreSetsResponse":
                                doc = gb.chatlogarea.getStyledDocument();
                                GameStyle = doc.getStyle("Game");
                                gameSystemStyle = doc.getStyle("GameSystem");
                                if (data.getString("username").equals(username)) {
                                    doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                    doc.insertString(doc.getLength(), "You have selected no-more-sets!", GameStyle);
                                    doc.insertString(doc.getLength(), "\n", GameStyle);
                                    gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());

                                } else {
                                    String nosetsuname = data.getString("username");
                                    doc.insertString(doc.getLength(), "System: ", gameSystemStyle);
                                    doc.insertString(doc.getLength(), nosetsuname + " has selected no-more-sets!", GameStyle);
                                    doc.insertString(doc.getLength(), "\n", GameStyle);
                                    gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
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

    public void updatePublicChat(String chatUserName, String chatMessage) {
        StyledDocument doc = landingPage.chatlogarea.getStyledDocument();
        Style unameStyle = doc.getStyle("Username");
        Style myunameStyle = doc.getStyle("Myusername");
        Style msgStyle = doc.getStyle("Msg");
        Style mymsgStyle = doc.getStyle("Mymsg");
        try {
            if (username.equals(chatUserName)){
                doc.insertString(doc.getLength(), "You: ", myunameStyle);
                doc.insertString(doc.getLength(), chatMessage, mymsgStyle);
                doc.insertString(doc.getLength(), "\n", mymsgStyle);
            }else{
                doc.insertString(doc.getLength(), chatUserName, unameStyle);
                doc.insertString(doc.getLength(), ": ", unameStyle);
                doc.insertString(doc.getLength(), chatMessage, msgStyle);
                doc.insertString(doc.getLength(), "\n", msgStyle);
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
        landingPage.chatlogarea.setCaretPosition(landingPage.chatlogarea.getDocument().getLength());
    }

    public void updateLocalChat(String chatUserName, String chatMessage) {
        StringBuilder chatitem = new StringBuilder();
        StyledDocument doc = gb.chatlogarea.getStyledDocument();
        Style unameStyle = doc.getStyle("Username");
        Style myunameStyle = doc.getStyle("Myusername");
        Style msgStyle = doc.getStyle("Msg");
        Style mymsgStyle = doc.getStyle("Mymsg");
        try {
            if (username.equals(chatUserName)){
                doc.insertString(doc.getLength(), "You: ", myunameStyle);
                doc.insertString(doc.getLength(), chatMessage, mymsgStyle);
                doc.insertString(doc.getLength(), "\n", mymsgStyle);
            }else{
                doc.insertString(doc.getLength(), chatUserName, unameStyle);
                doc.insertString(doc.getLength(), ": ", unameStyle);
                doc.insertString(doc.getLength(), chatMessage, msgStyle);
                doc.insertString(doc.getLength(), "\n", msgStyle);
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
        gb.chatlogarea.setCaretPosition(gb.chatlogarea.getDocument().getLength());
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
                System.err.println("Received: " + inobjString);
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

    public int registerUser(String usernamein, String password) {
        JSONObject obj = new JSONObject();

        obj.put("fCall", "registerUser");
        obj.put("login", usernamein);
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
                    username = usernamein;
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


