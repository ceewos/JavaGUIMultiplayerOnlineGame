/* Team:
 * Carlos Cisneros &
 * Braulio Bracamontes
*/
/**
 * A player in an Omok game. It holds the name of the player and
 * can be used to identify a specific player throughout the game.
 * The Player class helps to keep track of the moves made by each
 * player during the game.
 */
public class Player {
    /** Return the name of this player.
     * @return name Player's name/id
     */
    /** Name of this player. */
    private final String name;
    private final int id;

    public Player(String nameIn, int idIn){
        this.name = nameIn;
        this.id = idIn;
    }
    public String name(){
        return name;
    }

     /** Return the name of this player.
     * @return name Player's name/id
     */
    public int id() {
        return id;
    }

}



