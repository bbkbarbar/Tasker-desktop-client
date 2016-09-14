package hu.barbar.desktop.livingroomclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import hu.barbar.comm.util.tasker.Commands;

public abstract class Gui {
	
	public static final String title = "Desktop Client";
	
	public static final String TEXT_INFO_AIR          = "Air: ";
	public static final String TEXT_INFO_WATER        = "Water: ";
	private static final String TEXT_INFO_CPU 		  = "CPU: ";
	private static final String TEXT_INFO_HUMIDITY	  = "Humidity: ";
	private static final String TEXT_INFO_PLACEHOLDER = "                            ";
	
	private static final String TEXT_EMPTY = "";
	
	public static final String TEXT_BTN_CONNECT = "Connect";
	public static final String TEXT_BTN_DISCONNECT = "Disconnect";
	
	public static final String TEXT_BTN_REFRESH_INFOS = "Refresh";

	private static final String TEXT_BTN_CLEAR_COMMAND_LINE = "C";
	private static final String TEXT_BTN_SEND_COMMAND = "Send";

	private static final String TEXT_BTN_GET_VALUE = "Get";
	private static final String TEXT_BTN_SET_VALUE = "Set";

	private static final int DEFAULT_TAB_SIZE = 3;
	
	
	private int top, left, width, height;
	private JFrame mainFrame;
	
	private JTextField tfHost = null,
					   tfPort = null;
	private JButton btnConnect = null;
	
	private JButton btnRefreshInfos = null;
	
	private JScrollPane logScroll;
	private static JTextArea logArea;
	
	private JTextField tfCommand = null;
	private JButton btnSendCommand = null;
	
	/*
	 * Info panel
	 */
	private JLabel tempAir = null;
	private JLabel tempWater = null;
	private JLabel tempCPU = null;
	private JLabel humidity = null;
	
	/*
	 * PWM panel
	 */
	protected JProgressBar[] pwmProgressBars = new JProgressBar[App.NUMBER_OF_PWM_CHANNELS];
	protected JSlider[] pwmSliders = new JSlider[App.NUMBER_OF_PWM_CHANNELS];
	
	
	public Gui() {
		
		top = 150;
		left = 150;
		width = 1030;
		height = 400;
		
		mainFrame = new JFrame();
		mainFrame.setBounds(top, left, width, height);
		mainFrame.setTitle(Gui.title + " " + App.version);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	onWindowClosing();
		    	try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
		    	System.exit(0);
		    }
		});
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		FlowLayout experimentLayout = new FlowLayout();
		JPanel connectionPanel = new JPanel(experimentLayout);
		connectionPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		connectionPanel.setBackground(new Color(0xFF, 0x99, 0x33));
		
			connectionPanel.setToolTipText("ConnectionPanel");
			
			tfHost = new JTextField(" barbarhome.ddns.net ");
			connectionPanel.add(tfHost);
			tfPort = new JTextField(" 10714 ");
			connectionPanel.add(tfPort);
			
			btnConnect = new JButton(TEXT_BTN_CONNECT);  
			btnConnect.addActionListener( new ActionListener(){
			    public void actionPerformed(ActionEvent e){
			    	
			    	if(btnConnect.getText().toString().equals(TEXT_BTN_CONNECT)){
			    		try{
				    		if(Gui.this.onBtnClickedConnect(tfHost.getText().trim(), Integer.valueOf(tfPort.getText().trim()))){
				    			btnConnect.setText(TEXT_BTN_DISCONNECT);
				    			if(btnRefreshInfos != null){
				    				btnRefreshInfos.setEnabled(true);
				    			}
				    			if(btnSendCommand != null){
				    				btnSendCommand.setEnabled(true);
				    			}
				    		}
			    		}catch(NumberFormatException nfe){
			    			showLine(">>Port number can not parse to string.");
			    		}
			    	}else{
			    		if(Gui.this.onBtnClickedDisconnect()){
			    			btnConnect.setText(TEXT_BTN_CONNECT);
			    			if(btnRefreshInfos != null){
			    				btnRefreshInfos.setEnabled(false);
			    			}
			    			if(btnSendCommand != null){
			    				btnSendCommand.setEnabled(false);
			    			}
			    		}
			    	}
			    	
			    }
			});
			connectionPanel.add(btnConnect);
			
		JPanel contentPanel = new JPanel(new BorderLayout());
		
			JPanel infoPanel = new JPanel(new GridLayout(10,1));
				infoPanel.setToolTipText("Infos");
				btnRefreshInfos = new JButton(TEXT_BTN_REFRESH_INFOS);
				btnRefreshInfos.addActionListener( new ActionListener(){
				    public void actionPerformed(ActionEvent e){
				    	onBtnClickedRefreshInfos();
				    }
				});
				btnRefreshInfos.setEnabled(false);
			
				tempAir   = new JLabel(TEXT_INFO_AIR);
				tempWater = new JLabel(TEXT_INFO_WATER);
				tempCPU = new JLabel(TEXT_INFO_CPU);
				humidity = new JLabel(TEXT_INFO_HUMIDITY);
				
				infoPanel.add(btnRefreshInfos);						// 1
				infoPanel.add(tempAir);								// 2
				infoPanel.add(tempWater);							// 3
				infoPanel.add(humidity);              				// 4
				infoPanel.add(new JLabel(TEXT_EMPTY));              // 5
				infoPanel.add(new JLabel(TEXT_INFO_PLACEHOLDER));   // 6
				infoPanel.add(new JLabel(TEXT_EMPTY));              // 7
				infoPanel.add(new JLabel(TEXT_EMPTY));              // 8
				infoPanel.add(new JLabel(TEXT_EMPTY));              // 9
				infoPanel.add(tempCPU);                 			// 10
			
			
			JPanel commandPanel = new JPanel(new GridLayout(14,1));
				commandPanel.add(getPredefinedCommandButton("Set PWM output",   Commands.SET_PWM_OUTPUT + " ",    false ));
				commandPanel.add(getPredefinedCommandButton("Worker info mail", Commands.EMAIL_WORKER_INFO + " ", false ));
				commandPanel.add(getPredefinedCommandButton("Enable ToDoItem",  Commands.ENABLE_TODO_ITEM + " ",  false ));
				commandPanel.add(getPredefinedCommandButton("Disable ToDoItem", Commands.DISBALE_TODO_ITEM + " ", false ));
				commandPanel.add(getPredefinedCommandButton("Get client count", Commands.GET_CLIENT_COUNT,        true ));
				commandPanel.add(getPredefinedCommandButton("Send info mail",   Commands.SEND_INFO_MAIL + " ",    false ));
				commandPanel.add(getPredefinedCommandButton("Get worker info",  Commands.GET_WORKER_INFO + " ",   true ));
				
			JPanel pwmPanel = new JPanel(new GridLayout(App.NUMBER_OF_PWM_CHANNELS,1));
				for(int i=0; i<App.NUMBER_OF_PWM_CHANNELS; i++){
					pwmPanel.add(getPwmPanelItem(i));
				}
				
			JPanel centerPanel = new JPanel(new BorderLayout());
				logArea = new JTextArea(TEXT_EMPTY);
				logArea.setTabSize(DEFAULT_TAB_SIZE);
				logScroll = new JScrollPane(logArea);
				centerPanel.add(logScroll, BorderLayout.CENTER);
				
				JPanel customCommandPanel = new JPanel(new BorderLayout());
					
					tfCommand = new JTextField(TEXT_EMPTY);
				
					JButton btnClearCommandTf = new JButton(TEXT_BTN_CLEAR_COMMAND_LINE);
					btnClearCommandTf.addActionListener( new ActionListener(){
					    public void actionPerformed(ActionEvent e){
					    	if(tfCommand != null){
					    		tfCommand.setText(TEXT_EMPTY);
					    	}
					    }
					});
					
					btnSendCommand = new JButton(TEXT_BTN_SEND_COMMAND);
					btnSendCommand.addActionListener( new ActionListener(){
					    public void actionPerformed(ActionEvent e){
					    	if(tfCommand != null){
					    		String command = tfCommand.getText().trim();
					    		if(command.length()>0){
					    			onBtnClickedSendCommand(command);
					    		}else{
					    			showLine("Can not send empty command..");
					    		}
					    	}
					    }
					});
					btnSendCommand.setEnabled(false);
					JPanel commandLineBtnPanel = new JPanel(new BorderLayout());
					commandLineBtnPanel.add(btnSendCommand, BorderLayout.CENTER);
					commandLineBtnPanel.add(btnClearCommandTf, BorderLayout.EAST);
					customCommandPanel.add(tfCommand, BorderLayout.CENTER);
					customCommandPanel.add(commandLineBtnPanel, BorderLayout.EAST);
					
				
				centerPanel.add(customCommandPanel, BorderLayout.SOUTH);
				centerPanel.add(pwmPanel, BorderLayout.WEST);
				
			contentPanel.add(infoPanel, BorderLayout.WEST);
			contentPanel.add(commandPanel, BorderLayout.EAST);
			contentPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(connectionPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		mainFrame.getContentPane().add(mainPanel);
		mainFrame.setVisible(true);
	}
	
	private JButton getPredefinedCommandButton(String btnText, String command, boolean instantSend){
		JButton btnSendPredefinedCommand = new JButton(btnText);
		btnSendPredefinedCommand.addActionListener( new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		    	if(command != null || command.trim().length() > 0){
		    		if(instantSend){
		    			onBtnClickedSendCommand(command);
		    		}else{
		    			if(tfCommand != null){
		    				tfCommand.setText(command);
		    			}
		    		}
		    	}
		    }
		});
		return btnSendPredefinedCommand;
	}
	
	private JPanel getPwmPanelItem(int channel){
		JPanel complete = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(1, 2));
		
		JProgressBar bar = new JProgressBar(0, 100);
		Rectangle r = bar.getBounds();
		r.height = r.height / 3;
		r.width  = r.width  / 3;
		bar.setBounds(r);
		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		r = slider.getBounds();
		r.height = r.height / 3;
		r.width  = r.width  / 3;
		slider.setBounds(r);
		
		this.pwmProgressBars[channel] = bar;
		this.pwmSliders[channel] = slider;
		panel.add(slider);
		panel.add(bar);
		
		
		JButton getButton = new JButton(TEXT_BTN_GET_VALUE); 
		getButton.addActionListener( new ActionListener(){
		    public void actionPerformed(ActionEvent e){
				Gui.this.pwmSliders[channel].setValue(Gui.this.pwmProgressBars[channel].getValue());
		    }
		});
		
		JButton setButton = new JButton(TEXT_BTN_SET_VALUE); 
		setButton.addActionListener( new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		    	onBtnClickedSendPwmValues(channel);
		    }
		});
		
		complete.add(panel, BorderLayout.CENTER);
		complete.add(new JLabel("ch" + channel + ((channel<10)?"   ":" ")), BorderLayout.WEST);
		JPanel tmp = new JPanel(new GridLayout(1, 2));
			tmp.add(getButton);
			tmp.add(setButton);
		complete.add(tmp, BorderLayout.EAST);
		
		return complete;
	}
	
	protected boolean simulateConnectButtonClicked(){
		boolean ret = false;
		try{
    		if(ret = Gui.this.onBtnClickedConnect(tfHost.getText().trim(), Integer.valueOf(tfPort.getText().trim()))){
    			btnConnect.setText(TEXT_BTN_DISCONNECT);
    			if(btnRefreshInfos != null){
    				btnRefreshInfos.setEnabled(true);
    			}
    			if(btnSendCommand != null){
    				btnSendCommand.setEnabled(true);
    			}
    		}
		}catch(NumberFormatException nfe){
			showLine(">>Port number can not parse to string.");
		}
		return ret;
	}
	
	protected void simulateDisonnectButtonClicked(){
		if(btnConnect != null){
			btnConnect.setText(TEXT_BTN_CONNECT);
		}
		if(btnRefreshInfos != null){
			btnRefreshInfos.setEnabled(false);
		}
		if(btnSendCommand != null){
			btnSendCommand.setEnabled(false);
		}
	}
	
	/**
	 * @return result of connection attempt.
	 */
	protected abstract boolean onBtnClickedConnect(String host, int port);
	
	/**
	 * @return result of disconnection attempt.
	 */
	protected abstract boolean onBtnClickedDisconnect();

	protected abstract void onBtnClickedRefreshInfos();
	
	protected abstract void onBtnClickedSendCommand(String command);

	protected abstract void onWindowClosing();
	
	protected abstract void onBtnClickedSendPwmValues(int channel);
	
	public void showLine(String line){
		logArea.setText(logArea.getText() + line + "\n");
	}
	
	public void showTemp(float air, float water){
		tempAir.setText(   TEXT_INFO_AIR   + String.format("%.2f", air)   + "°C");
		tempWater.setText( TEXT_INFO_WATER + String.format("%.2f", water) + "°C");
	}
	
	public void showTemp(String air, String water){
		tempAir.setText(   TEXT_INFO_AIR   + air   + "°C");
		tempWater.setText( TEXT_INFO_WATER + water + "°C");
	}

	public void showPwmValue(int channel, int valueOfCurrentChannel) {
		
		this.pwmProgressBars[channel].setValue( (int)(valueOfCurrentChannel/40.95) );
		
	}

	public int[] getPwmValuesFromSliders() {
		if(this.pwmSliders == null){
			return null;
		}
		
		int[] result = new int[this.pwmSliders.length];
		
		for(int i=0; i<this.pwmSliders.length; i++){
			result[i] = pwmSliders[i].getValue();
		}
		
		return result;
	}

	public void showCPUTemp(String content) {
		tempCPU.setText(TEXT_INFO_CPU + content + "°C");
		
	}
	
	public void showHumidityOnUI(int humidityValue) {
		if(humidityValue < 0){
			humidity.setText(TEXT_INFO_HUMIDITY + "--%");
		}else{
			humidity.setText(TEXT_INFO_HUMIDITY + humidityValue + "%");
		}
	}
	
	
}
