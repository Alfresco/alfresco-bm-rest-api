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

import org.alfresco.bm.AbstractRestApiEventProcessor;
import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.rest.core.RestWrapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

public abstract class RestTest extends AbstractRestApiEventProcessor
{
    protected RestWrapper restClient;
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

    protected abstract void prepareData() throws Exception;

    protected abstract void restCall(Event event) throws Exception;

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        prepareData();

        super.resumeTimer();
        restCall(event);
        super.suspendTimer();

        return processStatusCode(event.getName(), getRestWrapper().getStatusCode());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
    }

    private EventResult processStatusCode(String eventName, String statusCode)
    {
        if (HttpStatus.CREATED.toString().equals(statusCode))
        {
            return markAsSucces(eventName);
        }
        if (HttpStatus.OK.toString().equals(statusCode))
        {
            return markAsSucces(eventName);
        }
        if (HttpStatus.CONFLICT.toString().equals(statusCode))
        {
            return markAsFailure(eventName);
        }
        return markAsFailure(eventName);
    }

    private EventResult markAsFailure(String eventName)
    {
        return new EventResult("Event execution failed: " + eventName, false);
    }

    private EventResult markAsSucces(String eventName)
    {
        return new EventResult("Event execution succeeded: " + eventName, true);
    }
}
