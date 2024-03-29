package ca.digitalcave.moss.jsp.auth.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("auth")
public class Config {

	private final static Logger logger = Logger.getLogger(Config.class.getName());
	
	private transient ConcurrentHashMap<Pattern, AuthMapping> authMappingsByPattern = null;

	@XStreamImplicit
	private List<AuthMapping> authMappings = new ArrayList<AuthMapping>();
	@XStreamImplicit
	private List<AuthGroup> groups = new ArrayList<AuthGroup>();
	@XStreamImplicit
	private List<AuthUser> users = new ArrayList<AuthUser>();
	@XStreamAsAttribute
	@XStreamAlias("type")
	private String authType;
	@XStreamAsAttribute
	@XStreamAlias("realm")
	private String realm;
	@XStreamAsAttribute
	@XStreamAlias("log-level")
	private String logLevel;

	public String getAuthType() {
		return authType;
	}
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getRealm() {
		return realm;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	public List<AuthGroup> getGroups() {
		if (groups == null)
			return new ArrayList<AuthGroup>();
		return groups;
	}
	public void setGroups(List<AuthGroup> groups) {
		this.groups = groups;
	}

	public List<AuthUser> getUsers() {
		if (users == null)
			return new ArrayList<AuthUser>();
		return users;
	}
	public void setUsers(List<AuthUser> users) {
		this.users = users;
	}
	public List<AuthMapping> getAuthMappings() {
		if (authMappings == null)
			return new ArrayList<AuthMapping>();
		return authMappings;
	}
	public void setAuthMappings(List<AuthMapping> authMappings) {
		this.authMappings = authMappings;
	}


	/**
	 * Checks authentication for the URL.  We find the first auth mapping which matches the given URL,
	 * and check all allowed groups in that mapping.  If the user belongs to at least one group which
	 * is allowed, we return true (meaning that the request is allowed).  If the user does not belong to 
	 * any auth mapping groups, we go on to the next matched pattern.  If the URL matched at least one
	 * pattern, but the user is not correct, we deny access.  If no patterns were matched, we allow access.  
	 * @param uri The URI to match
	 * @return true if the URI matches at least one pattern, false otherwise.
	 */
	public boolean checkAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = request.getRequestURL().toString();
		boolean matched = false;
		
		if (authMappingsByPattern == null){
			authMappingsByPattern = new ConcurrentHashMap<Pattern, AuthMapping>();
			for (AuthMapping cacheMapping : getAuthMappings()) {
				authMappingsByPattern.put(Pattern.compile(cacheMapping.getPattern()), cacheMapping);
			}
		}


		for (Pattern pattern : authMappingsByPattern.keySet()) {
			if (pattern.matcher(url).matches()){
				matched = true;

				//Check if the user has sent credentials
				String auth = request.getHeader("Authorization");
				logger.finest("Authorization header: " + auth);

				//Check if the user is valid
				if (allowUser(auth, authMappingsByPattern.get(pattern))){
					logger.finer("User is valid; allowing request to continue");
					return true;
				}
			}
		}
		//If no patterns matched the URL, we default to allowing the request.
		if (matched){
			logger.finest("The URL required authentication, but no valid user / groups were given.  Not allowing the request to proceed.");
			logger.finer("User is not valid; returning code 403");
			if (isBasic())
				response.setHeader("WWW-Authenticate", "BASIC realm=\"" + getRealm() + "\"");
			else if (isDigest())
				response.setHeader("WWW-Authenticate", "DIGEST realm=\"" + getRealm() + "\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		else{
			logger.finest("The URL did not require authentication.");
			return true;
		}
	}
	
	private boolean isBasic(){
		return getAuthType().toUpperCase().equals("BASIC");
	}
	
	private boolean isDigest(){
		return getAuthType().toUpperCase().equals("DIGEST");
	}

	// This method checks the user information sent in the Authorization
	// header against the config file.
	private boolean allowUser(String auth, AuthMapping authMapping) throws IOException {
		if (auth == null){ 
			return false;
		}

		if (isBasic() && !auth.toUpperCase().startsWith("BASIC ")){
			logger.finest("Basic authentication format was not found in the header.");
			return false;
		}
		
		if (isDigest() && !auth.toUpperCase().startsWith("DIGEST ")){
			logger.finest("Digest authentication format was not found in the header.");
			return false;
		}

		String userPassDecoded = "";
		if (isBasic()){
			// Get encoded user and password, comes after "BASIC "
			String userpassEncoded = auth.substring(6);
			userPassDecoded = new String(Base64.getDecoder().decode(userpassEncoded));
		}
		else if (isDigest()){
			//TODO
		}

		//Find if the username is value
		AuthUser user = null;
		String[] split = userPassDecoded.split(":", 2);
		if (split.length > 1){
			user = getUser(split[0]);
			if (user == null){
				logger.finest("No user found with name '" + user + "'");
				return false;
			}

			//If the username is valid, check the password
			if (isBasic()){
				if (split.length != 2){
					logger.finest("No password was given in authorization header");
					return false;
				}
				String password = split[1];
				if (!user.getPassword().equals(password)){
					logger.finest("User password was incorrect");
					return false;
				}
			}
			else if (isDigest()){

			}
		}
		
		//Check the groups - if the intersection of the user's groups and the allowed groups
		// is greater than 0, then the user is allowed.
		Set<String> groups = new HashSet<String>(authMapping.getAllowedGroups());
		groups.retainAll(user.getGroups());
		
		if (groups.size() == 0){
			logger.finest("User is not in any required groups.");
		}
		
		return groups.size() > 0;
	}

	private AuthUser getUser(String user){
		for (AuthUser authUser : getUsers()) {
			if (authUser.getName().equals(user))
				return authUser;
		}
		return null;
	}
}
