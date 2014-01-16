package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.List;

public interface SettingsSubject {
    void registerObserver(SettingsObserver o);
    void notifyObserversOfUpdateToUserModifiableSettings(List<UserModifiableSetting> userModifiableSettings);
    void notifyObserversOfUpdateToNonUserModifiableSettings(List<NonUserModifiableSetting> nonUserModifiableSettings);
}
