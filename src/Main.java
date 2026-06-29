
public class Main {
	public static void main(String[] args) {
		Server[] servers = new Server[3];
		Client[] clients =  new Client[4];
		for (int i = 0; i < servers.length; i++) {
		    servers[i] = new Server(i + 1);
		}
		Server leader = Server.electLeader(servers);
		
		for(int i = 0; i < clients.length; i++) {
			clients[i] = new Client(i+1, leader);
		}
        new SimulationGUI(servers, clients);
    }
}
