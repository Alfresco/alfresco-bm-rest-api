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

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.driver.event.RaiseEventsEventProcessor;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataService;
import org.alfresco.bm.site.SiteMemberData;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 *  Emits a given number of events at a given frequency for a given time.
 * <b>INPUT:   </b>-<br/>
 * <b>ACTIONS: </b>Picks a random site and a random site member<br/>
 * <b>OUTPUT: Propagates the same site id and member to the next event. </b>
 *
 * @author Ancuta Morarasu
 */
public class PrepareRandomSiteEventProcessor extends RaiseEventsEventProcessor
{
    public static final String EVENT_PREPARED = "eventPrepared";

    private SiteDataService siteDataService;
    
    public PrepareRandomSiteEventProcessor(SiteDataService siteDataService,
            String outputEventName,
            long timeBetweenEvents,
            int outputEventCount)
    {
        super(outputEventName, timeBetweenEvents, outputEventCount);
        this.siteDataService = siteDataService;
    }

    @Override
    protected Object getNextEventData()
    {
         super.suspendTimer();
         SiteData site = siteDataService.randomSite(null, DataCreationState.Created);
         String siteId = site.getSiteId();
         
         SiteMemberData siteMember = siteDataService.randomSiteMember(siteId, DataCreationState.Created, null);

         super.resumeTimer();
         
         // Build next event
         DBObject eventData = BasicDBObjectBuilder.start()
                 .add(SiteContextConstants.SITE_ID, siteId)
                 .add(SiteContextConstants.SITE_MEMBER, siteMember.getUsername()).get();
         
         return eventData;
    }

}