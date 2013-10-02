package int_.esa.eo.ngeo.dmtu.cli;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DmtuCliBannerProvider extends DefaultBannerProvider 
				implements CommandMarker {

	@CliCommand(value = { "version" }, help = "Displays current CLI version")
	public String getBanner() {
		StringBuilder buf = new StringBuilder();
		buf.append("===========================================================" + OsUtils.LINE_SEPARATOR);
		buf.append(name() + " v" + getVersion() + OsUtils.LINE_SEPARATOR);
		buf.append("===========================================================" + OsUtils.LINE_SEPARATOR);
		return buf.toString();

	}

	public String getVersion() {
		return ConfigurationProvider.getProperty(ConfigurationProvider.VERSION);
	}

	public String getWelcomeMessage() {
		return "Welcome to the " + name();
	}
	
	@Override
	public String name() {
		return ConfigurationProvider.getProperty(ConfigurationProvider.DM_TITLE) + " CLI";
	}
}