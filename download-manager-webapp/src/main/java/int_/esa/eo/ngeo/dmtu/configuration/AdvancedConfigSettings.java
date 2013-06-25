package int_.esa.eo.ngeo.dmtu.configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


// TODO: Cross-field validation
public class AdvancedConfigSettings extends FirstStartupConfigSettings{
	
	// TODO: Review validation constraints with the customer 
	
	@Min(value=1, message="Number of parallel product download threads must be at least one")
	@Max(value=5, message="Number of parallel product download threads must be less than or equal to 5")
	private int noOfParallelProductDownloadThreads;
	
	private String webProxyUrl;

	@Size(min=0, max=5, message="Web proxy port no. must be 1 - 5 numeric digits")
	@Pattern(regexp="^$|[0-9]+$", message="Web proxy port no. must be 1 - 5 numeric digits")
	private String webProxyPort;
	
	@Size(min=0, max=40, message="Web proxy username must be no longer than 40 characters")
	@Pattern(regexp="^$|[a-zA-Z0-9]+$", message="Web proxy username must be alphanumeric with no spaces")
	private String webProxyUsername;
	
	@Size(min=0, max=40, message="Web proxy password must be no longer than 40 characters")
	private String webProxyPassword;
	
	private String productDownloadCompleteCommand; // TODO: Does this encompass both the "command" and the "parameters"?
	
	@Size(min=0, max=40, message="Web interface username must be no longer than 40 characters")
	@Pattern(regexp="^$|[a-zA-Z0-9]+$", message="Web interface username must be alphanumeric with no spaces")
	private String webInterfaceUsername;
	
	@Size(min=0, max=40, message="Web interface password must be no longer than 40 characters")
	private String webInterfacePassword;
	
	private boolean webInterfaceRemoteAccessEnabled;

	
	
	public int getNoOfParallelProductDownloadThreads() {
		return noOfParallelProductDownloadThreads;
	}

	public void setNoOfParallelProductDownloadThreads(int noOfParallelProductDownloadThreads) {
		this.noOfParallelProductDownloadThreads = noOfParallelProductDownloadThreads;
	}

	public String getWebProxyUrl() {
		return webProxyUrl;
	}

	public void setWebProxyUrl(String webProxyUrl) {
		this.webProxyUrl = webProxyUrl;
	}

	public String getWebProxyPort() {
		return webProxyPort;
	}

	public void setWebProxyPort(String webProxyPort) {
		this.webProxyPort = webProxyPort;
	}

	public String getWebProxyUsername() {
		return webProxyUsername;
	}

	public void setWebProxyUsername(String webProxyUsername) {
		this.webProxyUsername = webProxyUsername;
	}

	public String getWebProxyPassword() {
		return webProxyPassword;
	}

	public void setWebProxyPassword(String webProxyPassword) {
		this.webProxyPassword = webProxyPassword;
	}

	public String getProductDownloadCompleteCommand() {
		return productDownloadCompleteCommand;
	}

	public void setProductDownloadCompleteCommand(
			String productDownloadCompleteCommand) {
		this.productDownloadCompleteCommand = productDownloadCompleteCommand;
	}

	public String getWebInterfaceUsername() {
		return webInterfaceUsername;
	}

	public void setWebInterfaceUsername(String webInterfaceUsername) {
		this.webInterfaceUsername = webInterfaceUsername;
	}

	public String getWebInterfacePassword() {
		return webInterfacePassword;
	}

	public void setWebInterfacePassword(String webInterfacePassword) {
		this.webInterfacePassword = webInterfacePassword;
	}

	public boolean isWebInterfaceRemoteAccessEnabled() {
		return webInterfaceRemoteAccessEnabled;
	}

	public void setWebInterfaceRemoteAccessEnabled(
			boolean webInterfaceRemoteAccessEnabled) {
		this.webInterfaceRemoteAccessEnabled = webInterfaceRemoteAccessEnabled;
	}
	
}