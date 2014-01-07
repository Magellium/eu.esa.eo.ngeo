
package int_.esa.eo.ngeo.downloadmanager.cli.config;


public enum DmCliSetting {
    DM_TITLE("DM_TITLE"),
    DM_CLI_PROMPT("DM_CLI_PROMPT"),
    DM_WEBAPP_URL("DM_WEBAPP_URL"),
    VERSION("VERSION");

    private final String value;

    private DmCliSetting(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
