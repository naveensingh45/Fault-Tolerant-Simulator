
public class Server {
    int id;
    boolean isAlive = true;
    boolean isLeader = false;
    boolean isLocked = false;   
    int lockedBy = -1;
    private String content = "";

    public Server(int id) {
    	this.id = id;
    }
    
    //Initial leader election : Will update with bully algorithm
    public static Server electLeader(Server[] servers) {
        // Server with lowest ID becomes leader
        Server leader = servers[0];
        for (Server s : servers) {
            if (s.isAlive && s.id < leader.id) {
                leader = s;
            }
        }
        leader.isLeader = true;
        return leader;
    }
    
    // Handle read request from client
    String handleRead() {
    	return content;
    }
    
    // Handle Lock request from client
    boolean handleLock(int clientID) {
    	if(!isLocked) {
    		isLocked = true;
    		lockedBy = clientID;
    		return true;
    	}
    	else 
    		return false;
    }
    
    //Update the file content with new content and release the lock
    void handleWriteandRelease(String newContent) {
    	content = newContent;
    	isLocked = false;
    	lockedBy = -1;
    }
    
    // Simulate Crash : Toggle the server alive state
    void simulateCrash() {
        isAlive = !isAlive;
    } 
}

