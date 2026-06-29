import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulationGUI extends JFrame implements ActionListener {
	
	Server[] servers;
    Client[] clients;

    //Server components
    JButton[] serverCrashButtons = new JButton[3];
    JLabel[] serverStatusLabels = new JLabel[3];

    //File content Area
    JTextArea fileContentArea;
    
    //Text Editor Area
    JTextArea editorArea;

    //Client components
    JTextArea[] clientLogAreas = new JTextArea[4];
    JButton[] readButtons = new JButton[4];
    JButton[] requestLockButtons = new JButton[4];
    JButton[] writeButtons = new JButton[4];
    JButton[] crashButtons = new JButton[4];

    public SimulationGUI(Server[] servers, Client[] clients) {
    	//Server and Client Initialization
    	this.servers = servers;   
        this.clients = clients;
        
        //Frame Setup
        setTitle("Fault Tolerant File System Simulation");
        setSize(1200, 800);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Server Cluster Panel
        JPanel serverClusterPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        serverClusterPanel.setBorder(BorderFactory.createTitledBorder("File Server Cluster"));

        for (int i = 0; i < 3; i++) {
            JPanel nodePanel = new JPanel(new BorderLayout(5, 5));

            JLabel titleLabel = new JLabel("Server " + (i + 1), SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            serverStatusLabels[i] = new JLabel(servers[i].isLeader? "Leader" : "Follower", SwingConstants.CENTER);

            serverCrashButtons[i] = new JButton("Simulate Crash");
            serverCrashButtons[i].addActionListener(this);

            nodePanel.add(titleLabel, BorderLayout.NORTH);
            nodePanel.add(serverStatusLabels[i], BorderLayout.CENTER);
            nodePanel.add(serverCrashButtons[i], BorderLayout.SOUTH);

            serverClusterPanel.add(nodePanel);
        }

        //File State Panel
        JPanel fileStatePanel = new JPanel(new BorderLayout());
        fileStatePanel.setBorder(BorderFactory.createTitledBorder("File State (Leader)"));
        fileContentArea = new JTextArea(servers[0].handleRead(), 5, 20);
        fileContentArea.setEditable(false);
        fileStatePanel.add(new JScrollPane(fileContentArea), BorderLayout.CENTER);
        
        //Editor Panel
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createTitledBorder("Client Editor"));
        editorArea = new JTextArea("", 5, 20);
        editorArea.setEditable(false);
        editorPanel.add(new JScrollPane(editorArea), BorderLayout.CENTER);

        //Clients Panel
        JPanel clientsGridPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        clientsGridPanel.setBorder(BorderFactory.createTitledBorder("Clients"));

        for (int i = 0; i < 4; i++) {
            JPanel clientPanel = new JPanel(new BorderLayout(5, 5));
            clientPanel.setBorder(BorderFactory.createTitledBorder("Client " + (i + 1)));

            clientLogAreas[i] = new JTextArea("Client-" + (i + 1) + " initialized.\n", 5, 5);
            clientLogAreas[i].setEditable(false);

            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            readButtons[i] = new JButton("Read File");
            requestLockButtons[i] = new JButton("Request Lock");
            writeButtons[i] = new JButton("Write & Release");
            writeButtons[i].setEnabled(false);
            crashButtons[i] = new JButton("Simulate Crash");

            buttonPanel.add(readButtons[i]);
            buttonPanel.add(requestLockButtons[i]);
            buttonPanel.add(writeButtons[i]);
            buttonPanel.add(crashButtons[i]);

            readButtons[i].addActionListener(this);
            requestLockButtons[i].addActionListener(this);
            writeButtons[i].addActionListener(this);
            crashButtons[i].addActionListener(this);

            clientPanel.add(new JScrollPane(clientLogAreas[i]), BorderLayout.CENTER);
            clientPanel.add(buttonPanel, BorderLayout.SOUTH);

            clientsGridPanel.add(clientPanel);
        }

        //Add Panels to the Main Panel
        mainPanel.add(serverClusterPanel);
        mainPanel.add(fileStatePanel);
        mainPanel.add(editorPanel);
        mainPanel.add(clientsGridPanel);
        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        //Check for server button clicks
        for (int i = 0; i < 3; i++) {
            if (source == serverCrashButtons[i]) {
                servers[i].simulateCrash();
                if (servers[i].isAlive) {
                    serverStatusLabels[i].setText(servers[i].isLeader? "Leader" : "Follower");
                    serverCrashButtons[i].setText("Simulate Crash");
                } else {
                    serverStatusLabels[i].setText("Offline");
                    serverCrashButtons[i].setText("Recover");
                }
                return;
            }
        }

        //Check for client button clicks
        for (int i = 0; i < 4; i++) {
            String clientName = "Client" + (i + 1);
            
            //Read Button : add log, read request, response from server
            if (source == readButtons[i]) {
                clientLogAreas[i].append(clientName + " request to read the file.\n");
                String response = clients[i].readRequest();
                clientLogAreas[i].append(response + "\n");
                if(response == "Read Successful") {
                	editorArea.setText(clients[i].content);
                }
            } 
            //Request Lock Button: add log, request for lock, if granted enable write button.
            else if (source == requestLockButtons[i]) {
                clientLogAreas[i].append(clientName + " requested a lock.\n");
                boolean granted = clients[i].requestLock();
                if(granted) {
                	clientLogAreas[i].append(clientName + " was granted lock.\n");
                	editorArea.setEditable(true);
                	editorArea.setText(servers[0].handleRead());
                } else {
                	clientLogAreas[i].append(clientName + " was denied lock.\n");
                }
                writeButtons[i].setEnabled(granted);
            } 
            //Write Button: Write the new content to server file and release the lock. Update file content area.
            else if (source == writeButtons[i]) {
            	String newContent = editorArea.getText();
            	clients[i].writeAndReleaseLock(newContent);
                clientLogAreas[i].append(clientName + " wrote and released lock.\n");
                fileContentArea.setText(servers[0].handleRead());
                editorArea.setText("");
                editorArea.setEditable(false);
                writeButtons[i].setEnabled(false);
            } 
            // Simulate Crash : Toggle the online status
            else if (source == crashButtons[i]) {
            	clients[i].simulateCrash();
            	if(clients[i].isAlive) {
            		readButtons[i].setEnabled(true);
            		requestLockButtons[i].setEnabled(true);
            		clientLogAreas[i].append(clientName + " recovered!\n");
            		crashButtons[i].setText("Simulate Crash");
            	} else {
            		readButtons[i].setEnabled(false);
            		requestLockButtons[i].setEnabled(false);
            		writeButtons[i].setEnabled(false);
            		clientLogAreas[i].append(clientName + " crashed!\n");
            		crashButtons[i].setText("Recover");
            	}
            }
        }
    }
}