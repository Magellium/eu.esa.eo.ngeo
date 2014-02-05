package int_.esa.eo.ngeo.downloadmanager.cli;

import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;

public class DownloadManagerCliHistoryFileNameProvider extends DefaultHistoryFileNameProvider{

    public String getHistoryFileName() {
        return System.getProperty("DM_HOME") + "/logs/CLI_command_history.log";
    }

    @Override
    public String name() {
        return "Download Manager CLI history file name provider";
    }
}