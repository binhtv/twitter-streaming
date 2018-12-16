package c523.spark_streaming_eg;


public class Tweet {
	public Tweet(String id, String userName, String statusText, int createdAt) {
		this.id = id;
		this.userName = userName;
		this.statusText = statusText;
		this.createdAt = createdAt;
		
	}
	
	private String id;
	private String userName;
	private String statusText;
	private int createdAt;
	
	public int getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(int createdAt) {
		this.createdAt = createdAt;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getStatusText() {
		return statusText;
	}
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
}
