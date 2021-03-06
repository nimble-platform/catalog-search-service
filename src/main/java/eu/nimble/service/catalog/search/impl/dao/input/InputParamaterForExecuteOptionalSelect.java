package eu.nimble.service.catalog.search.impl.dao.input;

import de.biba.triple.store.access.enums.Language;

public class InputParamaterForExecuteOptionalSelect {

	private String uuid;
	private String language;
	OrangeCommands orangeCommandSelected = new OrangeCommands();

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "InputParamaterForExecuteOptionalSelect [uuid=" + uuid + "]";
	}
	
	public Language getLanguage() {
		return Language.fromString(language);
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public OrangeCommands getOrangeCommandSelected() {
		return orangeCommandSelected;
	}

	public void setOrangeCommandSelected(OrangeCommands orangeCommandSelected) {
		this.orangeCommandSelected = orangeCommandSelected;
	}
	
}
