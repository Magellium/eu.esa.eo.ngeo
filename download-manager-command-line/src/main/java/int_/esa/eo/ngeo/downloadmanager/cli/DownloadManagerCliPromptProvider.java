package int_.esa.eo.ngeo.downloadmanager.cli;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;

import org.springframework.shell.plugin.support.DefaultPromptProvider;

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
