package ca.digitalcave.moss.jsp.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import ca.digitalcave.moss.jsp.auth.config.AuthGroup;
import ca.digitalcave.moss.jsp.auth.config.AuthMapping;
import ca.digitalcave.moss.jsp.auth.config.AuthUser;
import ca.digitalcave.moss.jsp.auth.config.Config;

public class Test {
	public static void main(String[] args) {
		Config config = new Config();
		
		List<AuthGroup> groups = new ArrayList<AuthGroup>();
		AuthGroup group = new AuthGroup();
		group.setName("users");
		groups.add(group);
		group = new AuthGroup();
		group.setName("admin");
		groups.add(group);
		config.setGroups(groups);
		
		List<AuthUser> users = new ArrayList<AuthUser>();
		AuthUser user = new AuthUser();
		user.setName("wyatt");
		user.setPassword("foobar");
		user.setGroups(Collections.singletonList("admin"));
		users.add(user);
		user = new AuthUser();
		user.setName("scott");
		user.setPassword("tiger");
		user.setGroups(Collections.singletonList("users"));
		users.add(user);
		config.setUsers(users);
		
		List<AuthMapping> authMappings = new ArrayList<AuthMapping>();
		AuthMapping authMapping = new AuthMapping();
		authMapping.setPattern("/.*jsp");
		authMapping.setAllowedGroups(Collections.singletonList("users"));
		authMappings.add(authMapping);
		config.setAuthMappings(authMappings);
		
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		xstream.toXML(config, System.out);
	}
}
