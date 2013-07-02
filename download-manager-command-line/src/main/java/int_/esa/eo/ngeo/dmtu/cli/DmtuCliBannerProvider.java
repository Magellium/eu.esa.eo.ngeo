package int_.esa.eo.ngeo.dmtu.cli;

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
		buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
		buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
		buf.append("*            " + name() + "                 *" +OsUtils.LINE_SEPARATOR);
		buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
		buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
		buf.append("Version:" + getVersion());
		return buf.toString();

	}

	public String getVersion() {
		return "0.4.1";
	}

	public String getWelcomeMessage() {
		return "Welcome to the " + name();
	}
	
	@Override
	public String name() {
		return "DMTU CLI";
	}
}