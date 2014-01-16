package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.List;

public interface SettingsObserver {
    void updateToUserModifiableSettings(List<UserModifiableSetting> userModifiableSettings);
    void updateToNonUserModifiableSettings(List<NonUserModifiableSetting> nonUserModifiableSettings);
}
