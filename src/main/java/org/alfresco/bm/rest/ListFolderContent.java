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
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestSiteContainerModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * <b>INPUT:   </b>A site id and a random site member of the site <br/>
 * <b>ACTIONS: </b>Lists the content of the site/documentLibrary location <br/>
 * <b>OUTPUT:  </b>No data
 * 
 * @author Ancuta Morarasu
 */
public class ListFolderContent extends RestTest 
{
    public static final String EVENT_FOLDER_CONTENT_LISTED = "eventFolderContentListed";    
    
    private String eventFolderContentListed;
    
    private UserDataService userDataService;
    
    public ListFolderContent() 
    {
        this.eventFolderContentListed = EVENT_FOLDER_CONTENT_LISTED;
    }

    public void setEventFolderContentListed(String eventFolderContentListed)
    {
        this.eventFolderContentListed = eventFolderContentListed;
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
        
        //Get the user for the selected site member
        
        UserData userdata = userDataService.findUserByUsername(member);
        if (userdata == null)
        {
            return new EventResult("Failure: The picked user doesn't exist anymore: " + member, false);
        }
        
        UserModel userModel = new UserModel();
        userModel.setUsername(userdata.getUsername());
        userModel.setPassword(userdata.getPassword());
        
        RestWrapper restWrapper = getRestWrapper();
        
        super.resumeTimer();
        
        RestSiteContainerModel container = restWrapper.authenticateUser(userModel).withCoreAPI().usingSite(site).getSiteContainer("documentLibrary");
        
        FolderModel documentLibrary = new FolderModel();
        documentLibrary.setNodeRef(container.getId());

        RestNodeModelsCollection collection = restWrapper.withCoreAPI().usingNode(documentLibrary).listChildren();
        
        super.suspendTimer();
        
        DBObject resultData = BasicDBObjectBuilder.start().add("msg", "Listed  " + site + "/documentLibrary.")
                                                          .add("count", collection.getPagination().getTotalItems()).get();

        Event nextEvent = new Event(eventFolderContentListed, null);
        return processStatusCode(resultData, getRestWrapper().getStatusCode(), nextEvent);
    }

}
