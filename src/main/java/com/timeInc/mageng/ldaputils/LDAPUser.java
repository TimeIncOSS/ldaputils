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
