package hu.barbar.desktop.livingroomclient;

import java.util.logging.LogManager;

import hu.barbar.comm.client.Client;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.tasker.Commands;
import hu.barbar.comm.util.tasker.PWMMessage;

public class App {

	public static String version = "v0.33";
	
	private static final boolean connectAutomatically = true;
	
	private static final boolean UI_TEST = false;
	
	protected static final int NUMBER_OF_PWM_CHANNELS = 16;

	@SuppressWarnings("unused")
	private static App me = null;
	
	private static Gui gui = null;
	
	private static LogManager log = null;
	
	private Client comm = null;
	
	
	public static void main(String[] args) {
		me = new App();
	}
	
	public App() {
		
		gui = new Gui(){

			@Override
			protected boolean onBtnClickedConnect(String host, int port) {
				comm = new Client(host, port) {
					
					@Override
					protected void showOutput(String text) {
						gui.showLine(text);
					}
					
					@Override
					protected void handleRecievedMessage(Msg message) {
						App.this.handleRecievedMessage(message);
					}
					
					@Override
					public void onDisconnected(String host, int port) {
						gui.showLine("Disconnected.");
						super.onDisconnected(host, port);
					}
				};
				gui.showLine("Start client..");
				comm.start();
				
				try {
					Thread.sleep(500);
				} catch (Exception e) {}/**/
				
				boolean successfullyConnected = (comm != null && comm.isConnected());
				if(successfullyConnected){
					gui.showLine("Connected.");
					//getTemperature();
					sendRequestsToRefreshInfos();
				}else{
					gui.showLine("...");
				}
				
				return successfullyConnected;
			}

			@Override
			protected boolean onBtnClickedDisconnect() {
				if(comm != null){
					comm.disconnect();
				}
				return true;
			}

			@Override
			protected void onBtnClickedRefreshInfos() {
				if(comm != null && comm.isConnected()){
					sendRequestsToRefreshInfos();
				}else{
					gui.showLine("Not connected. Can not refresh infos.");
				}
			}

			@Override
			protected void onBtnClickedSendCommand(String command) {
				sendCommand(command);
				System.out.println("Send command: |" + command + "|");
			}

			@Override
			protected void onWindowClosing() {
				if(comm != null && comm.isConnected()){
					gui.simulateDisonnectButtonClicked();
				}
			}
			
			@Override
			protected void onBtnClickedSendPwmValues(int channel) {
				if(comm == null || comm.isConnected() == false){
					gui.showLine("Can not send pwm values: Not connected.");
					return;
				}
				int[] pwmValuesToSet = gui.getPwmValuesFromSliders();
				PWMMessage msg = new PWMMessage(channel, pwmValuesToSet[channel]);
				if(comm.sendMessage(msg)){
					gui.showLine("SENT:\n" + msg.toString());
				}
				getPwmOutputValues();
			}
			
		};
		
		if(UI_TEST){
			gui.showLine("elso sor");
			gui.showLine("masodik sor");
			gui.showLine("n-edik sor");
			gui.showTemp(25.75f, 27.13f);
		}
		
		if(connectAutomatically){
			gui.simulateConnectButtonClicked();
		}
		
	}
	
	/**
	 * Send command to server
	 * @param command
	 */
	private void sendCommand(String command){
		if(comm != null){
			Msg toSend = new Msg(command, Msg.Types.REQUEST);
			if( comm.sendMessage(toSend) ){
			}else{
				if(gui != null){
					gui.showLine("Can NOT send message");
				}
				System.out.println("Can NOT send message: " + toSend.toString());
			}
		}
	}
	
	/**
	 * Send command(s) to refresh infos on UI..
	 */
	private void sendRequestsToRefreshInfos(){
		getTemperature();
		getPwmOutputValues();
		getCPUTemperature();
		getHumidityValue();
	}
	
	/**
	 * Send request to server to get humidity value.
	 */
	private void getHumidityValue() {
		Msg requestToGetHumidity = new Msg(Commands.GET_ONLY_HUMIDITY, Msg.Types.REQUEST);
		if(comm != null && comm.isConnected()){
			comm.sendMessage(requestToGetHumidity);
			return;
		}
	}
	
	/**
	 * Send request to server to get temperature values.
	 */
	private void getCPUTemperature() {
		if(comm != null){
			if(comm.isConnected()){
				comm.sendMessage(new Msg(Commands.GET_CPU_TEMP, Msg.Types.REQUEST));
			}else{
				gui.showLine("Can not get CPU temperature values: Not connected.");
			}
		}
	}
	
	/**
	 * Send request to server to get temperature values.
	 */
	private void getTemperature() {
		if(comm != null){
			if(comm.isConnected()){
				comm.sendMessage(new Msg(Commands.GET_TEMP, Msg.Types.COMMAND));
			}else{
				gui.showLine("Can not get temperature values: Not connected.");
			}
		}
	}
	
	
	private void getPwmOutputValues() {
		if(comm != null){
			if(comm.isConnected()){
				comm.sendMessage(new Msg(Commands.GET_PWM_OUTPUT_VALUES, Msg.Types.REQUEST));
			}else{
				gui.showLine("Can not get pwm values: Not connected.");
			}
		}
	}

	
	protected void handleRecievedMessage(Msg message) {
		if(gui != null){
			
			/*
			 *  Process temperature response
			 */
			if(message.getContent().startsWith("Temp: ")){
				String[] parts = message.getContent().split(" ");
				if(parts.length >= 4){
					int IDX_OF_AIR_TEMP = 3;
					int IDX_OF_WATER_TEMP = 2;
					float air   = Float.valueOf(parts[IDX_OF_AIR_TEMP]);
					float water = Float.valueOf(parts[IDX_OF_WATER_TEMP]);
					gui.showTemp(air, water);
				}else{
					gui.showLine("Can not find multile temperature value in temp response message: \"" + message.toString() + "\"");
				}
			}else
			
				
			/*
			 *  Process response what contains worker info
			 */
			if(message.getType() == Msg.Types.RESPONSE_WORKER_INFO){
				if(gui != null){
					gui.showLine(message.getContent());
				}
			}else
				
			
			/*
			 *  Process humidity response
			 */
			if(message.getType() == Msg.Types.RESPONSE_HUMIDITY){
				
				if(message.getContent().startsWith("Humidity: ")){
					String[] parts = message.getContent().split(" ");
					if(parts.length > 1){
						int humidityValue = Integer.valueOf(parts[1]);
						if(gui != null){
							gui.showHumidityOnUI(humidityValue);
						}
					}else{
						if(gui != null){
							gui.showLine("Can not find humidity value in response message: \"" + message.toString() + "\"");
						}else{
							System.out.println("Can not find humidity value in response message: \"" + message.toString() + "\"");
						}
					}
				}
				
			}else
				
			/*
			 *  Process response what contains worker info
			 */
			if(message.getType() == Msg.Types.RESPONSE_CPU_TEMP){
				if(gui != null){
					String temp = "";
					String[] parts = message.getContent().split(" ");
					if(parts.length >= 3){
						temp = parts[2];
					}
					gui.showCPUTemp(temp);
				}
			}else
			
			/*
			 *  Process response what contains PWM output states
			 */
			if(message.getType() == Msg.Types.PWM_OUTPUT_STATES){
				String[] valueStrs = message.getContent().split(" ");
				for(int channel = 0; channel<valueStrs.length; channel++){
					try{
						int valueOfCurrentChannel = Integer.valueOf(valueStrs[channel]);
						gui.showPwmValue(channel, valueOfCurrentChannel);
					}catch(NumberFormatException nfe){
						continue;
					}
				}
			}else
			
			
			/*
			 *  Show unprocessed message
			 */
			{
				gui.showLine(message.toString());
			}
		}else{
			System.out.println(message.toString());
		}
		
	}
	

}
