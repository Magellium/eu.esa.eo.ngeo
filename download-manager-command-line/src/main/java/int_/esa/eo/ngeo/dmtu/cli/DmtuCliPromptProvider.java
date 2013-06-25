package int_.esa.eo.ngeo.dmtu.cli;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DmtuCliPromptProvider extends DefaultPromptProvider {

	@Override
	public String getPrompt() {
		return "ngEO-D:>";
	}

	
	@Override
	public String name() {
		return "DMTU CLI prompt provider";
	}

}
