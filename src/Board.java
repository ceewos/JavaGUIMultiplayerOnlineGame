import java.util.ArrayList;
/**
 * Team:
 * Carlos Cisneros &
 * Braulio Bracamontes
 * 
 * Abstraction of an Omok board, which consists of n x n intersections
 * or places where players can place their stones. The board can be
 * accessed using a pair of 0-based indices (x, y), where x and y
 * denote the column and row number, respectively. The top-left
 * intersection is represented by the indices (0, 0), and the
 * bottom-right intersection is represented by the indices (n-1, n-1).
 */
public class Board {
    private Player[][] board;
    private int size;
    private ArrayList<Place> winningRow;
    /** Create a new board of the default size. */
    public Board() {
        this.size = 15;
        this.board = new Player[size][size];
    }
    /** Create a new board of the specified size.
     * @param size using the given size
     */
    public Board(int size) {
        this.size = size;
        this.board = new Player[size][size];
    }

    /** Return the size of this board.
     * @return  board's size.
     */
    public int size(){
        return this.size;
    }

    /** Removes all the stones placed on the board, effectively
     * resetting the board to its original state.
     * @return  empty board.
     */
    public void clear() {
        this.board = new Player[this.size][this.size];
    }

    /** Return a boolean value indicating whether all the places
     * on the board are occupied or not.
     *
     * @return boolean
     */
    public boolean isFull() {
        for(int i = 0;i < this.size;i++){
            for(int j = 0;j < this.size;j++){
                if(this.board[i][j]==null){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Place a stone for the specified player at a specified
     * intersection (x, y) on the board.
     *
     * @param x 0-based column (vertical) index
     * @param y 0-based row (horizontal) index
     * @param player Player whose stone is to be placed
     */
    public void placeStone(int x, int y, Player player) {
        this.board[x][y] = player;
    }

    /**
     * Return a boolean value indicating whether the specified
     * intersection (x, y) on the board is empty or not.
     *
     * @param x 0-based column (vertical) index
     * @param y 0-based row (horizontal) index
     * @return boolean true or false, board empty or not.
     */
    public boolean isEmpty(int x, int y) {
        if(this.board[x][y]==null){
            return true;
        }
        return false;
    }

    /**
     * Is the specified place on the board occupied?
     *
     * @param x 0-based column (vertical) index
     * @param y 0-based row (horizontal) index
     * @return boolean true or false, if its occupied or not.
     */
    public boolean isOccupied(int x, int y) {
        if(this.board[x][y]!=null){
            return true;
        }
        return false;
    }

    /**
     * Rreturn a boolean value indicating whether the specified
     * intersection (x, y) on the board is occupied by the given
     * player or not.
     *
     * @param x 0-based column (vertical) index
     * @param y 0-based row (horizontal) index
     * @param player using the current player
     * @return boolean  if current player is at the tested location.
     */
    public boolean isOccupiedBy(int x, int y, Player player) {
        if( this.board[x][y] == player ){
            return true;
        }
        return false;
    }

    /**
     * Return the player who occupies the specified intersection (x, y)
     * on the board. If the place is empty, this method returns null.
     *
     * @param x 0-based column (vertical) index
     * @param y 0-based row (horizontal) index
     * @return Player gives back the player object that is located int [x][y].
     */
    public Player playerAt(int x, int y) {
        return board[x][y];
    }

    /**
     * Return a boolean value indicating whether the given player
     * has a winning row on the board. A winning row is a consecutive
     * sequence of five or more stones placed by the same player in
     * a horizontal, vertical, or diagonal direction.
     *
     * @param player    based on the player checks for a win
     * @return boolean  will return true if there is a win, false if there isn't.
     */
    public boolean isWonBy(Player player) {
        //checks for horizontal and vertical win
        for(int i = 0; i < this.size; i++){
            for(int j = 0; j < this.size ; j++){
                //check rows
                if( board[i][j] != null  && j < this.size - 4){ //check if there's 5 stones in a row of the same player
                    if(board[i][j] == board[i][j+1] && board[i][j] == board[i][j+2] && board[i][j] == board[i][j+3] && board[i][j] == board[i][j+4]){
                        int[][] win =  { {i,j},{i,j+1},{i,j+2},{i,j+3},{i,j+4}};
                        this.winningRow = new ArrayList<>();
                        for(int w = 0; w < win.length ;w++){
                            this.winningRow.add(new Place(win[w][0], win[w][1]));
                        }
                        if(player == board[i][j]){
                            return true;
                        }
                    }
                }
                //check cols
                if( board[i][j] != null  && i < this.size - 4){ //check if there's 5 stones in a column of the same player
                    if(board[i][j] == board[i+1][j] && board[i][j] == board[i+2][j] && board[i][j] == board[i+3][j] && board[i][j] == board[i+4][j]){
                        int[][] win = { {i,j},{i+1,j},{i+2,j},{i+3,j},{i+4,j}};
                        this.winningRow = new ArrayList<>();
                        for(int w = 0; w < win.length ;w++){
                            this.winningRow.add(new Place(win[w][0], win[w][1]));
                        }
                        if(player == board[i][j]){
                            return true;
                        }
                    }
                }
            }
        }
        // check diagonals Right to Left
        for(int i = 0; i < this.size - 4; i++){
            for(int j = 0; j < this.size - 4; j++){
                if(board[i][j] != null && board[i][j] == board[i+1][j+1] && board[i][j] == board[i+2][j+2] && board[i][j] == board[i+3][j+3] && board[i][j] == board[i+4][j+4]){
                    int[][] win = { {i,j},{i+1,j+1},{i+2,j+2},{i+3,j+3},{i+4,j+4}};
                    this.winningRow = new ArrayList<>();
                    for(int w = 0; w < win.length ;w++){
                        this.winningRow.add(new Place(win[w][0], win[w][1]));
                    }
                    if(player == board[i][j]){
                        return true;
                    }
                }
            }
        }
        // check diagonals Left to Right
        for(int i = 0; i < this.size - 4; i++){
            for(int j = 4; j < this.size; j++){
                if(board[i][j] != null && board[i][j] == board[i+1][j-1] && board[i][j] == board[i+2][j-2] && board[i][j] == board[i+3][j-3] && board[i][j] == board[i+4][j-4]){
                    int[][] win = { {i,j},{i+1,j-1},{i+2,j-2},{i+3,j-3},{i+4,j-4}};
                    this.winningRow = new ArrayList<>();
                    for(int w = 0; w < win.length ;w++){
                        this.winningRow.add(new Place(win[w][0], win[w][1]));
                    }
                    if(player == board[i][j]){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Return the winning row. For those who are not familiar with
     * the Iterable interface, you may return an object of
     *
     * @return List  Gives the winning row back.
     */
    public ArrayList<Place> winningRow() {
        if( this.winningRow == null){
            isWonBy(null);
        }
        return this.winningRow;
    }

    /**
     * Gets the actual board from the list of Objects of type PLayer[][].
     *
     * @return Player[][]   Gets the actual board.
     */
    public Player[][] getBoard(){
        return this.board;
    }

    /**
     * An intersection on an Omok board identified by its 0-based column
     * index (x) and row index (y). The indices determine the position
     * of a place on the board, with (0, 0) denoting the top-left
     * corner and (n-1, n-1) denoting the bottom-right corner,
     * where n is the size of the board.
     */
    public class Place {
        /** 0-based column index of this place. */
        public final int x;

        /** 0-based row index of this place. */
        public final int y;

        /** Create a new place of the given indices.
         *
         * @param x 0-based column (vertical) index
         * @param y 0-based row (horizontal) index
         */
        public Place(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Method gives the x and y in a coordinate format (%,%).
         *
         * @return String   This returns the x and y coordinates.
         */
        // other methods if needed ...
        @Override
        public String toString(){
            return "("+this.x+", "+this.y+")";
        }
    }

}

