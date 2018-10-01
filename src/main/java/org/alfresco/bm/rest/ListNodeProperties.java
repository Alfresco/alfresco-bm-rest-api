/*
 * #%L
 * Alfresco Benchmark Rest Api
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.bm.rest;

import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.UserModel;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * <b>INPUT:   </b>A site id and a random site member of the site, along with a nodeRef of a file created in a previous event by the same user.<br/>
 * <b>ACTIONS: </b>Lists the properties of the node with the corresponding nodeRef.<br/>
 * <b>OUTPUT:  </b>Propagates the same site id and member to the next event, along with the noderef of the file.
 * 
 * @author Ancuta Morarasu
 */
public class ListNodeProperties extends RestTest 
{
    public static final String EVENT_PROPERTIES_LISTED = "eventNodePropertiesListed";    
    
    private UserDataService userDataService;
    private String eventNodePropertiesListed;
    
    
    public ListNodeProperties() 
    {
        this.eventNodePropertiesListed = EVENT_PROPERTIES_LISTED;
    }

    public void setEventNodePropertiesListed(String event)
    {
        this.eventNodePropertiesListed = event;
    }

    public void setUserDataService(UserDataService userDataService) 
    {
        this.userDataService = userDataService;
    }
    
    @Override
    protected EventResult processEvent(Event event) throws Exception 
    {
super.suspendTimer();
        
        DBObject dataObj = (DBObject) event.getData();
        if (dataObj == null)
        {
            throw new IllegalStateException("This processor requires data with field " + SiteContextConstants.SITE_ID);
        }
        String site = (String) dataObj.get(SiteContextConstants.SITE_ID);
        String member = (String) dataObj.get(SiteContextConstants.SITE_MEMBER);
        String nodeRef = (String) dataObj.get(SiteContextConstants.NODE_ID);
        
        //Get the user for the selected site member
        
        UserData userdata = userDataService.findUserByUsername(member);
        if (userdata == null)
        {
            return new EventResult("The picked user doesn't exist anymore: " + member, false);
        }
        
        UserModel userModel = new UserModel();
        userModel.setUsername(userdata.getUsername());
        userModel.setPassword(userdata.getPassword());
        
        RestWrapper restWrapper = getRestWrapper();
        
        FileModel file = new FileModel();
        file.setNodeRef(nodeRef);
        
        super.resumeTimer();
        // actual rest calls
        restWrapper.authenticateUser(userModel).configureRequestSpec()
                   .addFormParam("include", "isLink,path,allowableOperations,isLocked");
        
         RestNodeModel node = restWrapper.withCoreAPI().usingNode(file).getNode();

        super.suspendTimer();
        
        // Build next event
        DBObject eventData = BasicDBObjectBuilder.start()
                .add(SiteContextConstants.SITE_ID, site)
                .add(SiteContextConstants.SITE_MEMBER, member)
                .add(SiteContextConstants.NODE_ID, nodeRef).get();
        
        Event nextEvent = new Event(eventNodePropertiesListed, eventData);

        DBObject resultData = BasicDBObjectBuilder.start().add("msg", "Properties for file " + node.getName() )
                .add("status: ", getRestWrapper().getStatusCode()).get();

        return processStatusCode(resultData, getRestWrapper().getStatusCode(), nextEvent);
    }

}
