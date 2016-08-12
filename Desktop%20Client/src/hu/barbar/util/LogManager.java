package hu.barbar.util;

public abstract class LogManager {
	
	public static class Level {
		public static final int DEBUG = 0,
								INFO = 1,
								WARN = 2,
								ERROR = 3;
	}
	 
	
	private int level = Level.INFO;
	private String prefix = null;
	
	public LogManager(int level) {
		this.level = level;
		this.prefix = "";
	}
	
	public LogManager(String prefix, int level) {
		this.level = level;
		if(prefix == null){
			this.prefix = "";
		}else
		if(prefix.trim().length()>0){
			this.prefix = prefix + " ";
		}else{
			this.prefix = prefix;
		}
	}

	public void setLevel(int level){
		this.level = level;
	}
	
	public abstract void showDebug(String text);
	
	public abstract void showInfo(String text);
	
	public abstract void showWarn(String text);
	
	public abstract void showError(String text);
	
	
	public void d(String text){
		if(this.level <= Level.DEBUG)
			this.showDebug(this.prefix + text);
	}
	
	public void i(String text){
		if(this.level <= Level.INFO)
			this.showInfo(this.prefix + text);
	}
	
	public void w(String text){
		if(this.level <= Level.WARN)
			this.showWarn(this.prefix + text);
	}
	
	public void e(String errorMessage){
		if(this.level <= Level.ERROR)
			this.showError(this.prefix + errorMessage);
	}
	
}
