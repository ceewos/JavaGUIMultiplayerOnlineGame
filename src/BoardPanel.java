import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
/**
* Class that draws Board
*/
public class BoardPanel extends JPanel{
    public ArrayList<Placement> placements;
    Color[] stoneColors = {Color.BLACK ,Color.WHITE, Color.ORANGE };
    int[] currPos;
    public BoardPanel(int[] currPos){
        setPreferredSize(new Dimension(405, 405));
        this.currPos = currPos;
        this.placements = new ArrayList<Placement>();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = getWidth();
        Color color = new Color(137, 207, 240);
        int gridSize = size / 15;

        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        
        // Draw grid lines
        for (int i = 1; i <= 15; i++) {
            int x = i * gridSize;
            g.drawLine(x, 0, x, size);
            g.drawLine(0, x, size, x);
        }
        
        if (currPos[0] >= 0 && currPos[1] >= 0) {
            g.setColor(Color.BLACK);
            int circleSize = gridSize / 2;
            int circleX = currPos[1] * gridSize + gridSize / 2 - circleSize;
            int circleY = currPos[0] * gridSize + gridSize / 2 - circleSize;
            g.drawOval(circleX - circleSize , circleY - circleSize , circleSize * 2 , circleSize * 2);
        }

        for(Placement placement: placements){
            g.setColor(placement.color);
            int circleSize = gridSize / 2;
            int circleX = placement.col * gridSize + gridSize / 2 - circleSize;
            int circleY = placement.row * gridSize + gridSize / 2 - circleSize;
            g.fillOval(circleX - circleSize , circleY - circleSize , circleSize * 2 , circleSize * 2);
        }
    }

    /**
     * adds placements to board to be drawn
     * @param x  x coordinate to add to
     * @param y  y coordinate to add to
     * @param player  player determines color of stone
     */
    public void addPlacement(int x, int y, int player){
        placements.add(new Placement(x, y, stoneColors[player]));
    }

    /**
     * Class that holds placement information.
     */
    public class Placement{
        public int row;
        public int col;
        public Color color;
        public Placement(int row, int col, Color color){
            this.row = row;
            this.col = col;
            this.color = color;
        }
    }
    
    
}
