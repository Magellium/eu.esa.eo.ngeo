package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lewis Keen
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    @Autowired
    private SettingsManager settingsManager;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        if (!Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP)) || !Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED))) {
            return "redirect:/config/firststartup";
        }

        return "home";
    }
}