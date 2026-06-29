
public class Client 
{
	int id;
	boolean isAlive = true;
	Server leader;
	String content = "";

	public Client(int id, Server leader)
	{
        this.id = id;
        this.leader = leader;
    }
	
	//Read Request from Client
	String readRequest()
	{
		if(!leader.isLocked && leader.isAlive) {
			content = leader.handleRead();
			return "Read Successful";
		}
		else if(leader.isAlive){
			return "Can't Handle Read Request. File is locked by some writer Client.";
		} else {
			return "Server is dead.";
		}
	}
	//Lock Request from Client
	boolean requestLock()
	{	
		if(leader.isAlive) {
			boolean granted = leader.handleLock(id);
			return granted;
		} else {
			return false;
		}
	
	}
	//Write new Content and Release Lock
	void writeAndReleaseLock(String newContent) 
	{
		leader.handleWriteandRelease(newContent);
	}
	//Simulate crash : Toggle the alive state.
	void simulateCrash()
	{
		isAlive = !isAlive;
	}
}
