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

import java.util.Collection;
import java.util.Collections;

public class LDAPUser
{

    public LDAPUser(String commonName, String username, String email, Collection groups)
    {
        this.commonName = commonName;
        this.username = username;
        this.email = email;
        this.groups = groups;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Collection getGroups()
    {
        return Collections.unmodifiableCollection(groups);
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    public String toString()
    {
        return (new StringBuilder()).append("username=").append(username).append(", commonName=").append(commonName).append(", email=").append(email).append(" | groups: ").append(groups.toString()).toString();
    }

    private String commonName;
    private String username;
    private String email;
    private Collection groups;
}
