/*******************************************************************************
 * Copyright 2014 Time Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 * Utility class to authenticate against AD
 * and get list of groups the authenticated user belongs to.
 */
package com.timeInc.mageng.ldaputils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ashim Pradhan
 *
 * April 29, 2014
 */
public class LDAPConnection {
	
	private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String SECURITY_AUTHENTICATION = "simple";
	private static final String SAMACCOUNTNAME_PARAM = "sAMAccountName=";
	private static final String[] PERSON_RETURN_ATRRIBUTES = {"CN", "sAMAccountName", "mail", "memberOf"};
//	private static final String[] PERSON_RETURN_ATRRIBUTES ={"*"};
	private static final String GROUP_FILTER = "objectClass=group";
	
	private static final String ADVAR_MEMBEROF = "memberOf";
	private static final String ADVAR_EMAIL = "mail";
	private static final String ADVAR_COMMON_NAME = "CN";
	
	static Logger logger = LoggerFactory.getLogger(LDAPConnection.class);
	
	/**
	 * Authenticates username with supplied password against ldapUrl (with port). Uses adminUsername/adminPassword
	 * to search for list of groups the username is associated to. If adminUserName is null, uses the username
	 * to perform search. 
	 * @param ldapURL
	 * @param domain
	 * @param userSearchBase
	 * @param adminUsername
	 * @param adminPassword
	 * @param username
	 * @param password
	 * @param recursiveGroup this is expensive if true, so it goes only one level deep. 
	 * 			If recursive groups are not used in the directory service, set to false
	 * @return LDAPUser if login successful
	 */
	public static final LDAPUser loginUser(String ldapURL, String domain, String userSearchBase, String adminUsername, 
			String adminPassword, String username, String password, boolean recursiveGroup) {
		LDAPUser user = null;
		String email = null, name = null;
		List <String>groups = new ArrayList<String>();
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION);
		env.put(Context.SECURITY_PRINCIPAL, domain + "\\" + username);
		env.put(Context.SECURITY_CREDENTIALS, password);
				
		try {
			new InitialDirContext(env);
			/* login was successful, now let's get list of groups for the user */
			env.put(Context.REFERRAL, "follow");
			
			LdapContext context = new InitialLdapContext(env,null);
			SearchControls searchCtls = new SearchControls();
			searchCtls.setReturningObjFlag(true);
			searchCtls.setReturningAttributes(PERSON_RETURN_ATRRIBUTES);
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			
			String filter = SAMACCOUNTNAME_PARAM + username;
						
			if (adminUsername != null && !adminUsername.isEmpty()) {
				env.put(Context.SECURITY_PRINCIPAL, domain + "\\" + adminUsername);
				env.put(Context.SECURITY_CREDENTIALS, adminPassword);
				new InitialDirContext(env); // login the admin to do searches
			}
			
			NamingEnumeration<SearchResult> ne = context.search(userSearchBase, filter, searchCtls);
			while (ne.hasMore()) {
				SearchResult sr = (SearchResult)ne.next();
				
				name = sr.getAttributes().get(ADVAR_COMMON_NAME).toString();
				// name may come in as cn:first last
				String temp[] = name.split(":");
				if (temp.length > 1) {
					name = temp[1].trim();
				}
				
				email = sr.getAttributes().get(ADVAR_MEMBEROF) != null && sr.getAttributes().get(ADVAR_EMAIL) != null 
							? sr.getAttributes().get(ADVAR_EMAIL).toString() : "";
				// email may come like mail: ashim_pradhan@timeinc.com
				String temp1[] = email.split(":");
				if (temp1.length > 1) {
					email = temp1[1].trim();
				}
				Attribute aMemberOf = sr.getAttributes().get(ADVAR_MEMBEROF);
				if (aMemberOf != null) {
					for (NamingEnumeration<?> e = aMemberOf.getAll(); e.hasMore(); ) {
						String group = (String) e.next();
						groups.add(getOnlyGroupCN(group));
						if (recursiveGroup) {
							getNestedGroups(env, group, groups, context, searchCtls);
						}
					}
				}
			}
			
			user = new LDAPUser(name, username, email, groups);
		} catch (AuthenticationException e) {
			logger.debug("Login Failed for: " + username, e);
		} catch (NamingException e) {
			logger.error("Error getting groups for: " + username, e);
		}
		return user;
	}
	
	
	/**
	 * Recurse to get all nested groups 
	 * @param env
	 * @param group
	 * @param groups
	 */
	private static void getNestedGroups(Hashtable<String, String> env,
			String group, List<String> groups, LdapContext context, SearchControls searchCtls) {
		try {
			NamingEnumeration<SearchResult> ne = context.search(group, GROUP_FILTER, searchCtls);
			while (ne.hasMore()) {
				SearchResult sr = (SearchResult)ne.next();
				Attribute memberOf = sr.getAttributes().get(ADVAR_MEMBEROF);
				if (memberOf != null)
				for (NamingEnumeration<?> e1 = memberOf.getAll(); e1.hasMore(); ) {
					String nestedGroup = (String) e1.next();
					groups.add(getOnlyGroupCN(nestedGroup));
					/* recursing all nested groups takes too long, going only one level down un-comment to go deeper
					getNestedGroups(env, nestedGroup, groups, context, searchCtls);
					*/
				}
					
			}
		} catch (NamingException e) {
			logger.error("Problem getting children group for: " + group, e);
		}
		
	}


	/**
	 * @param group
	 * @return String
	 */
	private static String getOnlyGroupCN(String group) {
		String groupCN = group.split(",")[0];	// first item in the memberOf is CN. eg. CN=Group Name, OU=...
		if (groupCN != null) {
			return groupCN.split("=")[1];
		}
		return null;
	}

}

