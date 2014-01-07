package int_.esa.eo.ngeo.downloadmanager.cli;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DownloadManagerCliPromptProvider extends DefaultPromptProvider {
    private ConfigurationProvider configurationProvider;

    public DownloadManagerCliPromptProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }
    
    @Override
    public String getPrompt() {
        return configurationProvider.getProperty(DmCliSetting.DM_CLI_PROMPT) + ":>";
    }


    @Override
    public String name() {
        return configurationProvider.getProperty(DmCliSetting.DM_TITLE) + " CLI prompt provider";
    }
}
