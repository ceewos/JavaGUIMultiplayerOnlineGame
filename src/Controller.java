/**
 * a controller that can connect to an interface 
 */
public class Controller implements OmokInterface {
    private GUIOmok ioStream;
    private Board game;
    private Player[] players;
    private Player playerTurn;
    private String rules;
    private Connection server;
    private int port = 8001;
    public boolean gameStart;

    public Controller(int portIn){
        this.port = portIn;
    }
    /**
     * Launches the interface
     */
    public void start(){
        game = new Board();
        ioStream = new GUIOmok(port);
        ioStream.addIOListener(this);
        rules = "The game is played by two players on a grid.\nThe players take turns marking the spaces in the grid [O].One of them will be WHITE and the other BLACK.\nIn order to win the game, a player must place five of their marks in a horizontal, vertical or diagonal row, while preventing the opponent from doing so.\nThe game is won by the player who manages to place five marks in a row.\nGame On, Good Luck!";
        server = new Connection(port, ioStream, this);
        gameStart = false;
        //your turn or opponents turn
        this.players = new Player[]{new Player("Your" ,1), new Player("Opponent's" ,2)};
    }

    /**
     * Starts a new game, human or computer game
     */
    public void newGame(){
        game.clear();
        ioStream.resetBoard();
    }

    public void quitGame(){
        game.clear();
        ioStream.resetBoard();
        gameStart = false;
        ioStream.show("Pair for a new game!");

    }

    public void opponentLeftGame(){
        game.clear();
        gameStart = false;
        ioStream.show("Opponent left game, Pair for a new game!");
    }

    /**
     * Places stone on board and checks for a win
     * @param x,y given coordinates
     */
    public void makeMove(int x, int y){
        game.placeStone(x, y, playerTurn); //adds stone to board
        ioStream.addStone(x, y, playerTurn.id()-1); //adds stone to interface
        boolean won = game.isWonBy(playerTurn);
        if(won){
            if(game.isWonBy(players[0])){
                ioStream.show("You Won!");
            }else{
                ioStream.show("You Lost");

            }
            ioStream.highLight(game.winningRow());
            gameStart = false;
            return;
        }
        changePlayerTurn();
    }

    /**
     * Changes the player turn if Player1 -> Player2, if Player2 -> Player1
     */
    public void changePlayerTurn(){
        playerTurn = players[ (playerTurn.id() == 1) ? 1 : 0 ];
        ioStream.show(playerTurn.name()+" turn.");

    }

    //sets up who's turn it is
    public void setPlayerTurn(int id){
        //player [ p1, p2]
        playerTurn = players[ (id == 2) ? 1 : 0 ];
        ioStream.show(playerTurn.name()+" turn.");

    }


    @Override
    /**
     * Allows the game to be played and board to be clicked
     */
    public void play() {
        //if the game hasnt been started yet
        if(!server.connected){
            ioStream.show("Connect to game first");
            return;
        }
        if(!gameStart){
            server.sendPlayRequest();
        //if in the middle of a game
        }else{
            //0 = Yes, 1 = No , 2 = Cancel
            int choice = ioStream.warn("Play a new Game?");
            if(choice == 0){
                newGame();
            } 
        }
        
    }

    @Override
    /**
     * Checks for the selected coordinates availability
     * @param x,y  cooridinates
     * @return boolean stating if the move is valid or not
     */
    public boolean moveSelected(int x, int y) {
        if(gameStart){
            if(game.isEmpty(x, y)){
                makeMove(x, y);
                server.sendMove(x, y);
                return true;
            }else{
                ioStream.show("Space taken, try another spot");
                return false;
            }
        }
        return false;
    }

    @Override
    /**
     * Sends the rules string to the GUI
     */
    public void about() {//displays rules
        ioStream.info(rules);
    }

    @Override
    /**
     * Prompt to exit the game with buttons
     */
    public void endGame(){//message before exiting the game
        int choice = ioStream.warn("Click yes if you want to exit.");
        if(choice == 0){
            System.exit(0);
        }
    }

    @Override
    /**
     *Current game status
     @return boolean true-game going on , false game not available
     */
    public boolean gameStart(){
        return gameStart;
    }
   

    @Override
    /**
     *Current game status
     @return boolean true-game going on , false game not available
     */
    public void connect(String ipIn, int portIn){
        server.connect(ipIn, portIn,false);
    }

    @Override
    public void disconnect() {
        server.quit();
    }

    @Override
    public boolean isTurn() {
        return playerTurn.id() == 1;
    }


}
