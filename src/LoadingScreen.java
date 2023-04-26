import javax.swing.*;
public class LoadingScreen implements Runnable {
    private JFrame popUp;
    private JProgressBar progressBar;
    private Timer timer;

    public void run() {
        popUp = new JFrame("Loading...");
        popUp.setSize(200, 80);
        popUp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);

        JLabel messageLabel = new JLabel("Waiting for player...");

        JPanel loadingPanel = new JPanel();
        loadingPanel.add(messageLabel);
        loadingPanel.add(progressBar);

        popUp.add(loadingPanel);

        popUp.setLocationRelativeTo(null);
        popUp.setVisible(true);

        int delay = 20; // milliseconds
        timer = new Timer(delay, e -> {
            int value = progressBar.getValue();
            if (value < progressBar.getMaximum() ) {
                progressBar.setValue(value + 4);
            } else {
                progressBar.setValue(0);
            }
        });
        timer.start();
    }
    public void stop() {
        if (timer != null) {
            timer.stop();
            popUp.dispose();
        }
    }
}
