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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.bm.AbstractRestApiEventProcessor;
import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;

public class RestRequestsTest extends AbstractRestApiEventProcessor
{

    private String eventName;
    private int numberOfRestRequests;
    private long creationDelay;

    public RestRequestsTest(String eventName, int numberOfRestRequests, long creationDelay)
    {
        this.eventName = eventName;
        this.numberOfRestRequests = numberOfRestRequests;
        this.creationDelay = creationDelay;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {

        List<Event> nextEvents = new ArrayList<Event>();

        long lastEventTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfRestRequests; i++)
        {
            Event self = new Event(eventName, lastEventTime, null);
            nextEvents.add(self);
            lastEventTime += creationDelay;
        }

        return new EventResult(event.getName(), nextEvents);
    }
}
