public interface OmokInterface {
    public void play();
    public void about();
    public void connect(String ipIn, int portIn);

    public boolean isTurn();
    public void disconnect();

    public void endGame();
    public boolean moveSelected(int x, int y);
    public boolean gameStart(); 
    
}
