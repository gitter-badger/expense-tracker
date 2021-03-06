/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exp.tracker.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import com.exp.tracker.data.entities.AuthEntity;
import com.exp.tracker.data.entities.GroupEntity;
import com.exp.tracker.data.entities.UserEntity;

/**
 * Models a user of this system.
 * 
 * @author Arun Patra
 * 
 */
public class UserBean implements Serializable
{

    private static final long serialVersionUID = 2025298387662418996L;
    /**
     * The unique record id.
     */
    private Long id;
    /**
     * The user name.
     */
    private String username;
    /**
     * The user password.
     */
    private String password;
    /**
     * Is the user enabled in the system?.
     */
    private boolean enabled;
    /**
     * Has this user been marked for a password reset?.
     */
    private boolean passwordChangeNeeded;
    /**
     * EMail ID of the user.
     */
    private String emailId;
    
    /**
     * The phone number
     */
    private String phoneNumber;
    /**
     * First name of the user.
     */
    private String firstName;
    /**
     * Last name of the user.
     */
    private String lastName;
    /**
     * Middle initial of the user.
     */
    private String middleInit;
    /**
     * The user creation date.
     */
    private Date createdDate;
    /**
     * The last update date.
     */
    private Date lastUpdatedDate;

    /**
     * The authorization to be added.
     */
    private AuthBean authToBeAdded;

    /**
     * A list of authorities for this user.
     */
    private List<AuthBean> authSet;
    
    private List<String> groups;

    /**
     * Clears user entered data.
     */
    public void clearUserData()
    {
        id = null;
        username = null;
        password = null;
        enabled = true;
        passwordChangeNeeded = false;
        emailId = null;
        firstName = null;
        lastName = null;
        middleInit = null;
        createdDate = null;
        lastUpdatedDate = null;
        authSet = new ArrayList<AuthBean>();
        authToBeAdded = null;
        groups = null;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public UserBean() {
        setEnabled(true);
        authSet = new ArrayList<AuthBean>();
    }

    /**
     * Constructor
     * 
     * @param ue
     */
    public UserBean(UserEntity ue) {
        if (ue.getEnabled() == 1) {
            this.setEnabled(true);
        } else {
            this.setEnabled(false);
        }
        this.id = ue.getId();
        this.setUsername(ue.getUsername());
        this.setPassword(ue.getPassword());
        this.setCreatedDate(ue.getCreatedDate());
        this.setEmailId(ue.getEmailId());
        this.setFirstName(ue.getFirstName());
        this.setLastName(ue.getLastName());
        this.setLastUpdatedDate(ue.getLastUpdatedDate());
        this.setMiddleInit(ue.getMiddleInit());
        if (ue.getPasswordChangeNeeded() == 1) {
            this.setPasswordChangeNeeded(true);
        } else {
            this.setPasswordChangeNeeded(false);
        }
        this.setPhoneNumber(ue.getPhoneNumber());
        List<AuthBean> abl = new ArrayList<AuthBean>();
        for (AuthEntity ae : ue.getAuthSet()) {
            AuthBean ab = new AuthBean(ae);
            abl.add(ab);
        }
        this.setAuthSet(abl);
        List<String> groupList = new ArrayList<String>();
        if (null != ue.getGroups()) {
        	 for (GroupEntity ge : ue.getGroups()) {
             	groupList.add(ge.getGroupName());
             }
        }      
        this.setGroups(groupList);
    }

    public UserEntity getUserEntity()
    {
        UserEntity ue = new UserEntity();
        ue.setId(this.id);
        ue.setUsername(this.getUsername());
        ue.setPassword(this.getPassword());
        if (this.getEnabled()) {
            ue.setEnabled(1);
        } else {
            ue.setEnabled(0);
        }
        ue.setCreatedDate(this.getCreatedDate());
        ue.setEmailId(this.getEmailId());
        ue.setFirstName(this.getFirstName());
        ue.setLastName(this.getLastName());
        ue.setMiddleInit(this.getMiddleInit());
        ue.setLastUpdatedDate(this.getLastUpdatedDate());
        ue.setCreatedDate(this.getCreatedDate());
        if (this.isPasswordChangeNeeded()) {
            ue.setPasswordChangeNeeded(1);
        }
        ue.setPhoneNumber(this.getPhoneNumber());
        List<AuthEntity> ael = new ArrayList<AuthEntity>();
        for (AuthBean ab : getAuthSet()) {
            AuthEntity ae = ab.getAuthEntity();
            ael.add(ae);
        }
        ue.setAuthSet(ael);
        return ue;
    }

    public List<AuthBean> getAuthSet()
    {
        return authSet;
    }

    public void setAuthSet(List<AuthBean> authSet)
    {
        this.authSet = authSet;
    }

    public void removeAuth()
    {
        //
    }

    public void addAuth(String auth)
    {
        Map<String, AuthBean> aMap = new HashMap<String, AuthBean>();
        for (AuthBean ab : getAuthSet()) {
            aMap.put(ab.getAuthority(), ab);
        }
        if (!aMap.containsKey(auth)) {
            AuthBean ab = new AuthBean();
            ab.setAuthority(auth);
            ab.setUsername(username);
            this.authSet.add(ab);
        }
    }
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void validateAddNewAuth(ValidationContext context)
    {
        MessageContext messages = context.getMessageContext();
        messages.addMessage(new MessageBuilder().error()
                .code("role.allready.exists").build());
    }

    public String getEmailId()
    {
        return emailId;
    }

    public void setEmailId(String emailId)
    {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public boolean isPasswordChangeNeeded()
    {
        return passwordChangeNeeded;
    }

    public void setPasswordChangeNeeded(boolean passwordChangeNeeded)
    {
        this.passwordChangeNeeded = passwordChangeNeeded;
    }

    public String getMiddleInit()
    {
        return middleInit;
    }

    public void setMiddleInit(String middleInit)
    {
        this.middleInit = middleInit;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public AuthBean getAuthToBeAdded()
    {
        return authToBeAdded;
    }

    public void setAuthToBeAdded(AuthBean authToBeAdded)
    {
        this.authToBeAdded = authToBeAdded;
    }

    public Date getLastUpdatedDate()
    {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate)
    {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public void validateUserModificationScreen(ValidationContext context)
    {
        String userEventName = context.getUserEvent();
        if (("addAuth".equalsIgnoreCase(userEventName))
                || ("deleteAuth".equalsIgnoreCase(userEventName))) {
            if (getUsername().equalsIgnoreCase(
                    context.getUserPrincipal().getName())) {
                MessageContext messages = context.getMessageContext();
                messages.addMessage(new MessageBuilder().error()
                        .code("cannot.modify.selfauths").build());
            }
        }
    }

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
}
