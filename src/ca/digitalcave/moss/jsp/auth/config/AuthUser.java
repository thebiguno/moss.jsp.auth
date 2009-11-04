package ca.digitalcave.moss.jsp.auth.config;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("user")
public class AuthUser {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String password;
	@XStreamImplicit(itemFieldName="group")
	private List<String> groups = new ArrayList<String>();

	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
