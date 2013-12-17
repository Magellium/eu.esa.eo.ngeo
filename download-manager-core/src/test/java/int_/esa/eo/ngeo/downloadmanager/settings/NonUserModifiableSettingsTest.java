package int_.esa.eo.ngeo.downloadmanager.settings;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class NonUserModifiableSettingsTest {

	private Properties nonUserModifiableProperties = new Properties();

	@Before
	public void setup() throws IOException {
		InputStream in = SettingsManager.class.getResourceAsStream("/META-INF/non-user-modifiable-settings.properties");
		nonUserModifiableProperties.load(in);
	}
	
	@Test
	public void testNonUserModifiableSettingsAgainstDefaultProperties() {
		List<String> enumValuesWithoutCorrespondingPropertyEntries = new ArrayList<>();
		StringBuilder errorMessage = new StringBuilder();
		
		List<String> enumValues = enumValues(NonUserModifiableSetting.class);
		
		for (String enumValue: enumValues) {
			if(nonUserModifiableProperties.containsKey(enumValue)) {
				nonUserModifiableProperties.remove(enumValue);
			}else{
				enumValuesWithoutCorrespondingPropertyEntries.add(enumValue);
			}
		}
		
		if(enumValuesWithoutCorrespondingPropertyEntries.size() > 0) {
			errorMessage.append("enum values without corresponding property entries: ");
			errorMessage.append(enumValuesWithoutCorrespondingPropertyEntries);
			errorMessage.append("\n");
		}
		if(nonUserModifiableProperties.size() > 0) {
			errorMessage.append("property entries without corresponding enum values: ");
			errorMessage.append(nonUserModifiableProperties.keySet());
		}

		if(errorMessage.length() > 0) {
			fail(errorMessage.toString());
		}
	}

	public <T extends Enum<T>> List<String> enumValues(Class<T> enumType) {
		List<String> enumNames = new ArrayList<>();
		for (T c : enumType.getEnumConstants()) {
			enumNames.add(c.name());
		}
		return enumNames;
	}
}
