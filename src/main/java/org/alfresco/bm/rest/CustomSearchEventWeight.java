/*
 * #%L
 * Alfresco Benchmark Rest Api
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.bm.rest;

import static org.alfresco.bm.rest.SiteContextConstants.EVENT_INSIGHT_ENGINE;
import static org.alfresco.bm.rest.SiteContextConstants.EVENT_SEARCH_SERVICES;
import static org.alfresco.bm.rest.SiteContextConstants.SEARCH_SERVICES_SEARCH_TYPE;
import static org.alfresco.bm.rest.SiteContextConstants.INSIGHT_ENGINE_SEARCH_TYPE;

import org.alfresco.bm.driver.event.EventWeight;

/**
 * Data representing relative weight for given event name.
 * 
 * @author Cristina Diaconu
 */
public class CustomSearchEventWeight extends EventWeight
{

    private String searchType;

    /**
     * @param eventName The name of the event being lent some weight.
     * @param weight An explicit event weight (ignored if less than zero).
     */
    public CustomSearchEventWeight(String eventName, double weight)
    {
        super(eventName, weight);
    }

    /**
     * @param eventName The name of the event being lent some weight.
     * @param weights A comma-separated list of weight values that will be multiplied together e.g. "1.0, 0.5" will give a weighting of "0.5".
     */
    public CustomSearchEventWeight(String eventName, String weights)
    {
        super(eventName, -1.0, weights);
    }

    public void setSearchType(String searchType)
    {
        this.searchType = searchType;
    }

    /**
     * Return the weight if the event type matches the search type.
     */
    @Override
    public double getWeight()
    {
        if (isSearchServicesSearch() || isInsightEngineSearch())
        {
            return super.getWeight();
        }
        return 0;
    }

    private boolean isSearchServicesSearch()
    {
        return getEventName().equalsIgnoreCase(EVENT_SEARCH_SERVICES)
                && searchType.equalsIgnoreCase(SEARCH_SERVICES_SEARCH_TYPE);
    }

    private boolean isInsightEngineSearch()
    {
        return getEventName().equalsIgnoreCase(EVENT_INSIGHT_ENGINE)
                && searchType.equalsIgnoreCase(INSIGHT_ENGINE_SEARCH_TYPE);
    }

}
