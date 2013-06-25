package int_.esa.eo.ngeo.dmtu.configuration;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

// TODO: Cross-field validation, e.g. consider there to be an error if password is provided but username isn't.
// TODO: Should we render a password confirmation field, and check that both passwords match?
public class FirstStartupConfigSettings {
	
	//TODO: Revisit validation rules to ensure that they match with customer expectations
	@Size(min=1, max=40, message="SSO username must be 1 - 40 characters long")
	@Pattern(regexp="^[a-zA-Z0-9]+$", message="SSO username must be alphanumeric with no spaces")
	private String ssoUsername;
	
	@Size(min=4, max=40, message="SSO password must be 4 - 40 characters long")
	private String ssoPassword;
	
	@Size(min=3, max=100, message="Name of DMTU instance must be 3 - 100 characters long")
	@Pattern(regexp="^[^ ].*$", message="Name of DMTU instance must not start with a space")
	private String dmFriendlyName;
	
	@Size(min=1, message="Download directory name must be at least one character long")
	private String baseDownloadFolder;
	
	@Size(min=1, max=5, message="DMTU web interface port no. must be 1 - 5 numeric digits")
	@Pattern(regexp="^[0-9]+$", message="DMTU web interface port no. must be 1 - 5 numeric digits")
	private String webInterfacePortNo; // XXX: Would this be more elegantly implemented as an int or an Integer?
	
	
	public String getSsoUsername() {
		return ssoUsername;
	}
	public void setSsoUsername(String ssoUsername) {
		this.ssoUsername = ssoUsername;
	}
	public String getSsoPassword() {
		return ssoPassword;
	}
	public void setSsoPassword(String ssoPassword) {
		this.ssoPassword = ssoPassword;
	}
	public String getDmFriendlyName() {
		return dmFriendlyName;
	}
	public void setDmFriendlyName(String dmFriendlyName) {
		this.dmFriendlyName = dmFriendlyName;
	}
	public String getBaseDownloadFolder() {
		return baseDownloadFolder;
	}
	public void setBaseDownloadFolder(String baseDownloadFolder) {
		this.baseDownloadFolder = baseDownloadFolder;
	}
	public String getWebInterfacePortNo() {
		return webInterfacePortNo;
	}
	public void setWebInterfacePortNo(String webInterfacePortNo) {
		this.webInterfacePortNo = webInterfacePortNo;
	}

}