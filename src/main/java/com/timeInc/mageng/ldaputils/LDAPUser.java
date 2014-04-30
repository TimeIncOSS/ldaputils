/**
 * 
 */
package com.timeInc.mageng.ldaputils;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Ashim Pradhan
 *
 * April 29, 2014
 */
public class LDAPUser {

	private String commonName;
	private String username;
	private String email;
	private Collection<String> groups;
	
	
	/**
	 * @param commonName
	 * @param username
	 * @param email
	 * @param groups
	 */
	public LDAPUser(String commonName, String username,
			String email, Collection<String> groups) {
		this.commonName = commonName;
		this.username = username;
		this.email = email;
		this.groups = groups;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return unmodifiable Collection groups
	 */
	public Collection<String> getGroups() {
		return Collections.unmodifiableCollection(groups);
	}

	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "username=" + username + ", commonName=" + commonName + ", email=" + email 
				+ " | groups: " + groups.toString();
	}

	
}
