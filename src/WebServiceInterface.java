import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.*;

public class WebServiceInterface extends JFrame{
    //second column
    JTextField ipNumberField ;
    JTextField portNUmberField ;

    JTextField hostIPField;
    JTextField portField;
    JButton connect;
    JButton disconnect;
    JTextArea messages;
    OmokInterface listener;
    int port;

    public WebServiceInterface(OmokInterface listenerIn, int portIn){
        super("Omok Server");
        this.port = portIn;
        this.listener = listenerIn;
        JPanel serverPanel = new JPanel();
        setContentPane(serverPanel);
        serverPanel.setLayout(new GridLayout(3,1));
        serverPanel.setPreferredSize(new Dimension(450,600));

        //Get IP and Hostname info
        String hostIp = "";
        String hostName = "";

        //get IP through webserver
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            hostIp = socket.getLocalAddress().getHostAddress();
        }catch(Exception e){
            e.printStackTrace();
        }
        //get IP through inet address does not work on mac
        try{
            InetAddress host = InetAddress.getLocalHost();
            //hostIp = host.getHostAddress();
            hostName = host.getHostName();       
        }catch(Exception e){
            e.printStackTrace();
        }

        //top part of the panel(player part)
        JPanel player = new JPanel();
        player.setLayout(new BorderLayout());
        JLabel playerLabel = new JLabel("Player");
        JPanel playerInfo = new JPanel();
        playerInfo.setLayout(new GridLayout(3,3));

        Border blackline = BorderFactory.createLineBorder(Color.GRAY);
        Border emptyBorder = BorderFactory.createEmptyBorder(8, 15, 8, 15);

        //first row
        JLabel hostNameLabel = new JLabel("Host name: ");
        JTextField hostNameField = new JTextField(hostName);
        hostNameField.setEditable(false);
        //second column
        JLabel ipNumberLabel = new JLabel("IP Number: ");
        ipNumberField = new JTextField(hostIp);
        ipNumberField.setEditable(false);
        //third column
        JLabel portNumberLabel = new JLabel("Port Number: ");
        portNUmberField = new JTextField(port+"");

        //adding them into the the grid
        playerInfo.add(hostNameLabel);
        playerInfo.add(hostNameField);
        playerInfo.add(ipNumberLabel);
        playerInfo.add(ipNumberField);
        playerInfo.add(portNumberLabel);
        playerInfo.add(portNUmberField);
        playerInfo.setBorder(blackline);

        //adding into the top panel
        player.add(playerLabel, BorderLayout.NORTH);
        player.add(playerInfo, BorderLayout.CENTER);
        player.setBorder(emptyBorder);


        // middle part of the panel(opponent)
        JPanel opponentSection = new JPanel();
        opponentSection.setLayout(new BorderLayout());
        JLabel opponentLabel = new JLabel("Opponent");
        JPanel opponentInfo = new JPanel();
        opponentInfo.setLayout(new GridLayout(3,3));
        //first row
        JLabel hostIPLabel = new JLabel("Host name/IP: ");
        hostIPField = new JTextField("localHost");
        //second column
        JLabel portLabel = new JLabel("Port Number: ");
        portField = new JTextField();
        connect = new JButton("Connect");
        disconnect = new JButton("Disconnect");
        disconnect.setEnabled(false);

        //adding into the opponent fill in section
        opponentInfo.add(hostIPLabel);
        opponentInfo.add(hostIPField);
        opponentInfo.add(portLabel);
        opponentInfo.add(portField);
        opponentInfo.add(connect);
        opponentInfo.add(disconnect);
        opponentInfo.setBorder(blackline);

        //adding everything into the middle section
        opponentSection.add(opponentLabel,BorderLayout.NORTH);
        opponentSection.add(opponentInfo,BorderLayout.CENTER);
        opponentSection.setBorder(emptyBorder);

        //south (server text area, notification(connected or not), close button)
        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        messages = new JTextArea();
        messages.setEditable(false);
        JScrollPane scrollText = new JScrollPane(messages);
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        messages.setSize(300,100);
        JButton close = new JButton("Close");

        //adding into the south panel
        messages.setBorder(blackline);
        south.add(scrollText);
        bottom.add(close,BorderLayout.EAST);
        south.add(bottom, BorderLayout.SOUTH);
        south.setBorder(emptyBorder);

        ///adding panels into the super serverpanel
        serverPanel.add(player);
        serverPanel.add(opponentSection);
        serverPanel.add(south);

        pack();

        connect.addActionListener(e -> {
            try{
                listener.connect( hostIPField.getText(), Integer.parseInt( portField.getText() ) );
            }catch(Exception ex ){
                messages.append("Invalid Host/Port\n");
            }
        });

        disconnect.addActionListener(e -> {
            listener.disconnect();
        });

    }

}
