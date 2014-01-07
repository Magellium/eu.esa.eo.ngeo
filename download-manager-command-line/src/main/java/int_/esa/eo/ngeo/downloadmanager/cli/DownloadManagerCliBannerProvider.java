package int_.esa.eo.ngeo.downloadmanager.cli;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DownloadManagerCliBannerProvider extends DefaultBannerProvider implements CommandMarker {
    private static final String BANNER_WRAPPER = "===========================================================";
    private ConfigurationProvider configurationProvider;
    
    public DownloadManagerCliBannerProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }
    
    @CliCommand(value = { "version" }, help = "Displays current CLI version")
    public String getBanner() {
        StringBuilder buf = new StringBuilder();
        buf.append(BANNER_WRAPPER + OsUtils.LINE_SEPARATOR);
        buf.append(name() + " v" + getVersion() + OsUtils.LINE_SEPARATOR);
        buf.append(BANNER_WRAPPER + OsUtils.LINE_SEPARATOR);
        return buf.toString();

    }

    public String getVersion() {
        return configurationProvider.getProperty(DmCliSetting.VERSION);
    }

    public String getWelcomeMessage() {
        return "Welcome to the " + name();
    }

    @Override
    public String name() {
        return configurationProvider.getProperty(DmCliSetting.DM_TITLE) + " CLI";
    }
}