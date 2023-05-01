import java.io.*;
import java.net.*; 
import javax.swing.SwingUtilities;
public class Connection  {
    /** Default port number on which this server to be run. */
    private int port;
    private Socket incoming; 
    private NetworkAdapter networkAdapter;
    public boolean connected = false;
    public boolean hosting = true;
    private Thread hostThread;
    private GUIOmok ui ;
    private Controller ctrl;
    private LoadingScreen loadScreen;
    
    /** Create a new server. */
    public Connection(int port, GUIOmok uiIn, Controller ctrl) {
        this.port = port;  
        this.ui = uiIn;
        this.ctrl = ctrl;
        hostThread = new Thread(this::host);
        hostThread.start();
        
    }

    public void host(){
        System.out.println("server started on port "+ port +"!"); 
        ui.write("server started on port "+ port +"!"); 
        try {
            ServerSocket s = new ServerSocket(port); 
            while (hosting) {
                incoming = s.accept(); 
                try {
                    if(!connected){
                        ui.write(incoming.getInetAddress().getHostAddress()+" is connected");
                        connected = true;
                        connect(s.getInetAddress().getHostAddress(), port, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server terminated"); 
    }
    
    



    public void connect(String host, int port, boolean isHost) {
        try {
            Socket socket = new Socket(host, port);
            if(isHost){
                PrintStream log = new PrintStream(  socket.getOutputStream());
                this.networkAdapter =  new NetworkAdapter( incoming, log);
            }else{
                ui.write("Connected to "+host);
                PrintStream log = new PrintStream(socket.getOutputStream());
                this.networkAdapter =  new NetworkAdapter(socket );
                connected = true;
            }
            recieve();
            ui.handleConnection(true);
        } catch (Exception e) {
            writeUI("Host/Port not found");
        }
        
    }

    public void recieve(){
        networkAdapter.setMessageListener(new NetworkAdapter.MessageListener() {
            public void messageReceived(NetworkAdapter.MessageType type, int x, int y) {
                switch (type) {

                    //called when someone requests for you to join
                    case PLAY: 
                        writeUIin("play:");
                        //0 = Yes, 1 = No , 2 = Cancel
                        int choice = ui.warn("join new game ? ");
                        if(choice == 0){
                            playResponse(true);
                            ctrl.newGame();
                            ctrl.setPlayerTurn(1);
                            ctrl.gameStart = true;
                        }else{
                            playResponse(false);
                        }
                        break;

                    case PLAY_ACK:
                        loadScreen.stop();
                        writeUIin("play_ack:"+ x+","+y);
                        if( x == 1 ){//join game request has been accepted
                            ctrl.newGame();
                            if (y == 1){ //opponent turn    
                                ctrl.setPlayerTurn(2);
                            }else{ //self turn
                                ctrl.setPlayerTurn(1);
                            }
                            ctrl.gameStart = true;

                        }else{//denied
                            quit();
                        }
                             
                        break;
                    case MOVE:    
                        writeUIin("move: "+x+","+y);
                        ackMove(x, y);
                        ctrl.makeMove(x, y);
                
                        break;
                    case MOVE_ACK:
                        writeUIin("move_ack: "+x+","+y);
                        
                        break;
                    case QUIT:
                        //make the UI give the option to pair again
                        ui.handleConnection(false);
                        ctrl.opponentLeftGame();
                        ui.clearLog();
                        writeUI("Opponent quit game");
                        connected = false;

                        break;
                    case CLOSE: 
                        ui.handleConnection(false);
                        connected = false;
                        ctrl.quitGame();
                        writeUIin("Connection terminated");
                        break;
                    case UNKNOWN: 
                        writeUIin("Recieved Unknown message from server");
                        break;
                }
            }
        });
     
        // receive messages asynchronously
        networkAdapter.receiveMessagesAsync();
    }

    public void sendPlayRequest(){
        //write play on log
        writeUIout("play:");
        //send play to other log
        this.networkAdapter.writePlay();
        loadScreen = new LoadingScreen();
        Thread loadScreenThread = new Thread(loadScreen);
        loadScreenThread.start();
    }

    /* Send a message out */
    public void sendMove(int x, int y ) {
        //this.networkAdapter.writeMsg(text+" "+port);
        writeUIout("move: "+x+","+y);
        this.networkAdapter.writeMove(x, y);
    }
    public void ackMove(int x, int y){
        writeUIout("move_ack:"+x+","+y);
        this.networkAdapter.writeMoveAck(x, y);
    }
    
    public void playResponse(boolean yes){
        //write play on log
        int response = 0;
        int turn = 1;
        if(yes)
            response = 1;
        writeUIout("play_ack:" + response+","+turn);
        //send play to other log
        this.networkAdapter.writePlayAck(yes, true);

    }

    public void quit(){
        this.networkAdapter.writeQuit();
        ctrl.quitGame();
        connected = false;
        ui.handleConnection(false);
        ui.clearLog();
        writeUI("You have left the game");
    }


    public void writeUIin(String txt){
        SwingUtilities.invokeLater(() -> ui.write(" < "+txt));
    }
    public void writeUIout(String txt){
        SwingUtilities.invokeLater(() -> ui.write(" > "+txt));
    }
    public void writeUI(String txt){
        SwingUtilities.invokeLater(() -> ui.write(txt));
    }




}
