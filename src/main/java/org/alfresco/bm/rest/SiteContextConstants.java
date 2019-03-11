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

/**
 * Constants used as keys for data passed from event to event during a rest-api responsiveness scenario.
 * 
 */
public interface SiteContextConstants 
{
    public static final String SITE_ID = "siteId";
    
    public static final String SITE_MEMBER = "siteMember";
    
    public static final String NODE_ID = "nodeId";

	public static final String EVENT_SEARCH_SERVICES = "rest-collab.scenario.03.search";

	public static final String EVENT_INSIGHT_ENGINE = "rest-collab.scenario.04.search";

	public static final String SEARCH_SERVICES_SEARCH_TYPE = "Search Services Search";

	public static final String INSIGHT_ENGINE_SEARCH_TYPE = "Insight Engine Search";
}
