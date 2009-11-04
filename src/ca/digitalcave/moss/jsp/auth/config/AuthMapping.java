package ca.digitalcave.moss.jsp.auth.config;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("auth-mapping")
public class AuthMapping {
	@XStreamAsAttribute
	private String pattern;
	@XStreamImplicit
	private List<String> allowedGroups = new ArrayList<String>();

	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public List<String> getAllowedGroups() {
		return allowedGroups;
	}
	public void setAllowedGroups(List<String> allowedGroups) {
		this.allowedGroups = allowedGroups;
	}
}
