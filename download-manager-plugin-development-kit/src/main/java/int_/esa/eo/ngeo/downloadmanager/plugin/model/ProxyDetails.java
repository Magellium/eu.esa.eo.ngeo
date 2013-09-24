package int_.esa.eo.ngeo.downloadmanager.plugin.model;

public class ProxyDetails {
	private final String proxyLocation;
	private final int proxyPort;
	private final String proxyUser;
	private final String proxyPassword;

	public ProxyDetails(String proxyLocation, int proxyPort, String proxyUser, String proxyPassword) {
		this.proxyLocation = proxyLocation;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
	}

	public String getProxyLocation() {
		return proxyLocation;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}
}
