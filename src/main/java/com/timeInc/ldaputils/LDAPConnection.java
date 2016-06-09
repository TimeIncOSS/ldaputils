/*******************************************************************************
 * Copyright 2016 Time Inc
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
package com.timeInc.ldaputils;

import com.timeInc.ldaputils.LDAPUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class LDAPConnection {
    private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String SECURITY_AUTHENTICATION = "simple";
    private static final String SAMACCOUNTNAME_PARAM = "sAMAccountName=";
    private static final String[] PERSON_RETURN_ATRRIBUTES = new String[]{"CN", "sAMAccountName", "mail", "memberOf"};
    private static final String GROUP_FILTER = "objectClass=group";
    private static final String ADVAR_MEMBEROF = "memberOf";
    private static final String ADVAR_EMAIL = "mail";
    private static final String ADVAR_COMMON_NAME = "CN";
    static Logger logger = LoggerFactory.getLogger((Class)LDAPConnection.class);

    public static final LDAPUser loginUser(String ldapURL, String domain, String userSearchBase, String adminUsername, String adminPassword, String username, String password, boolean recursiveGroup) {
        LDAPUser user = null;
        String email = null;
        String name = null;
        ArrayList<String> groups = new ArrayList<String>();
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", ldapURL);
        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.security.principal", domain + "\\" + username);
        env.put("java.naming.security.credentials", password);
        try {
            new InitialDirContext(env);
            env.put("java.naming.referral", "follow");
            InitialLdapContext context = new InitialLdapContext(env, null);
            SearchControls searchCtls = new SearchControls();
            searchCtls.setReturningObjFlag(true);
            searchCtls.setReturningAttributes(PERSON_RETURN_ATRRIBUTES);
            searchCtls.setSearchScope(2);
            String filter = "sAMAccountName=" + username;
            if (adminUsername != null && !adminUsername.isEmpty()) {
                env.put("java.naming.security.principal", domain + "\\" + adminUsername);
                env.put("java.naming.security.credentials", adminPassword);
                new InitialDirContext(env);
            }
            NamingEnumeration<SearchResult> ne = context.search(userSearchBase, filter, searchCtls);
            while (ne.hasMore()) {
                Attribute aMemberOf;
                String[] temp1;
                SearchResult sr = ne.next();
                name = sr.getAttributes().get("CN").toString();
                String[] temp = name.split(":");
                if (temp.length > 1) {
                    name = temp[1].trim();
                }
                if ((temp1 = (email = sr.getAttributes().get("memberOf") != null && sr.getAttributes().get("mail") != null ? sr.getAttributes().get("mail").toString() : "").split(":")).length > 1) {
                    email = temp1[1].trim();
                }
                if ((aMemberOf = sr.getAttributes().get("memberOf")) == null) continue;
                NamingEnumeration e = aMemberOf.getAll();
                while (e.hasMore()) {
                    String group = (String)e.next();
                    groups.add(LDAPConnection.getOnlyGroupCN(group));
                    if (!recursiveGroup) continue;
                    LDAPConnection.getNestedGroups(env, group, groups, context, searchCtls);
                }
            }
            user = new LDAPUser(name, username, email, groups);
        }
        catch (AuthenticationException e) {
            logger.debug("Login Failed for: " + username, (Throwable)e);
        }
        catch (NamingException e) {
            logger.error("Error getting groups for: " + username, (Throwable)e);
        }
        return user;
    }

    private static void getNestedGroups(Hashtable<String, String> env, String group, List<String> groups, LdapContext context, SearchControls searchCtls) {
        try {
            NamingEnumeration<SearchResult> ne = context.search(group, "objectClass=group", searchCtls);
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attribute memberOf = sr.getAttributes().get("memberOf");
                if (memberOf == null) continue;
                NamingEnumeration e1 = memberOf.getAll();
                while (e1.hasMore()) {
                    String nestedGroup = (String)e1.next();
                    groups.add(LDAPConnection.getOnlyGroupCN(nestedGroup));
                }
            }
        }
        catch (NamingException e) {
            logger.error("Problem getting children group for: " + group, (Throwable)e);
        }
    }

    private static String getOnlyGroupCN(String group) {
        String groupCN = group.split(",")[0];
        if (groupCN != null) {
            return groupCN.split("=")[1];
        }
        return null;
    }
}
