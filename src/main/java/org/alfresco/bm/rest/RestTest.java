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

import java.util.Collections;
import java.util.List;

import org.alfresco.bm.AbstractRestApiEventProcessor;
import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.springframework.http.HttpStatus;

/**
 * Base for the Rest API event processors, default properties are set (like the alfresco url), knows to processes status codes of REST calls.
 */
public abstract class RestTest extends AbstractRestApiEventProcessor
{
    protected String alfrescoAdminUsername;
    protected String alfrescoAdminPassword;
    
    public void setAlfrescoAdminUsername(String alfrescoAdminUsername)
    {
        this.alfrescoAdminUsername = alfrescoAdminUsername;
    }

    public void setAlfrescoAdminPassword(String alfrescoAdminPassword)
    {
        this.alfrescoAdminPassword = alfrescoAdminPassword;
    }

    protected EventResult processStatusCode(Object resultData, String statusCode, Event nextEvent)
    {
        return processStatusCode(resultData, statusCode, Collections.singletonList(nextEvent));
    }
    
    protected EventResult processStatusCode(Object resultData, String statusCode, List<Event> nextEvents)
    {
        if (HttpStatus.CREATED.toString().equals(statusCode) ||
            HttpStatus.OK.toString().equals(statusCode) ||
            HttpStatus.NO_CONTENT.toString().equals(statusCode))
        {
            return markAsSucces(resultData, nextEvents);
        }
        if (HttpStatus.CONFLICT.toString().equals(statusCode))
        {
            return markAsFailure(resultData);
        }
        return markAsFailure(resultData);
    }

    private EventResult markAsFailure(Object resultData)
    {
        return new EventResult("Failure: " + resultData, false);
    }

    private EventResult markAsSucces(Object resultData, List<Event> nextEvents)
    {
        nextEvents = nextEvents == null ? Collections.emptyList() : nextEvents;
        
        return new EventResult(resultData, nextEvents, true);
    }
}
