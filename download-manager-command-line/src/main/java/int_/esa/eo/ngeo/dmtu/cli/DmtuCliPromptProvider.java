package int_.esa.eo.ngeo.dmtu.cli;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DmtuCliPromptProvider extends DefaultPromptProvider {

	@Override
	public String getPrompt() {
		return ConfigurationProvider.getProperty(ConfigurationProvider.DM_CLI_PROMPT) + ":>";
	}

	
	@Override
	public String name() {
		return ConfigurationProvider.getProperty(ConfigurationProvider.DM_TITLE) + " CLI prompt provider";
	}

}
