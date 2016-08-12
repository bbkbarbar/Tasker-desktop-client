package hu.barbar.comm.client;

import java.io.IOException;
import java.io.ObjectOutputStream;

import hu.barbar.comm.util.Msg;
import hu.barbar.util.LogManager;

public class SenderThread extends Thread {

	private ObjectOutputStream objOut = null;
	
	private LogManager log = null;
	
	public SenderThread(ObjectOutputStream aOut, LogManager l){
		this.objOut = aOut;
		this.log = l;
	}
	
	public void sendMsg(Msg msg) {
		
		try {
			
			objOut.writeObject( msg );
			return;
			
		} catch (IOException e) {
			if(log != null)
				log.e("Client.SenderThread.sendMsg() -> IOException catched.");
			e.printStackTrace();
		}
		
	}
	
	
}
