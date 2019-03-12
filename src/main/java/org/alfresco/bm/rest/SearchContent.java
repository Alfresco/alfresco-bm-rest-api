/*
 * #%L
 * Alfresco Benchmark Manager
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

import static org.alfresco.bm.rest.SiteContextConstants.SEARCH_SERVICES_SEARCH_TYPE;
import static org.alfresco.bm.rest.SiteContextConstants.INSIGHT_ENGINE_SEARCH_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestRequestRangesModel;
import org.alfresco.rest.search.FacetQuery;
import org.alfresco.rest.search.Pagination;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.rest.search.SearchSqlRequest;
import org.alfresco.utility.model.UserModel;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * <b>INPUT: </b>A site id, a random site member of the site and the text/query
 * to search for <br/>
 * <b>ACTIONS: </b>Search for the text/query on the random site<br/>
 * <b>OUTPUT: </b>List all the documents that match the search query<br/>
 * 
 * @author Cristina Diaconu
 */
public class SearchContent extends RestTest
{

    private static final String EVENT_SEARCH_CONTENT = "eventSearchContent";

    private String eventSearchContent;
    private UserDataService userDataService;

    // search common properties
    private String searchQuery;
    private String searchType;

    // Search Services properties
    private String searchLanguage;
    private int maxItems;
    private int skipCount;

    // Search Services Range properties
    private String rangeField;
    private String rangeStart;
    private String rangeStop;
    private String rangeGap;
    private boolean rangeHardend;

    private String facetQueries;

    // Insight Engine Search properties
    private String format;
    private boolean includeMetadata;
    private String timezone;

    public SearchContent()
    {
        this.eventSearchContent = EVENT_SEARCH_CONTENT;
    }

    public void setEventSearchContent(String eventSearchContent)
    {
        this.eventSearchContent = eventSearchContent;
    }

    public void setUserDataService(UserDataService userDataService)
    {
        this.userDataService = userDataService;
    }

    public void setSearchQuery(String searchQuery)
    {
        this.searchQuery = searchQuery;
    }

    public void setSearchType(String searchType)
    {
        this.searchType = searchType;
    }

    public void setSearchLanguage(String searchLanguage)
    {
        this.searchLanguage = searchLanguage;
    }

    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    public void setSkipCount(int skipCount)
    {
        this.skipCount = skipCount;
    }

    public void setRangeField(String rangeField)
    {
        this.rangeField = rangeField;
    }

    public void setRangeStart(String rangeStart)
    {
        this.rangeStart = rangeStart;
    }

    public void setRangeStop(String rangeStop)
    {
        this.rangeStop = rangeStop;
    }

    public void setRangeGap(String rangeGap)
    {
        this.rangeGap = rangeGap;
    }

    public void setRangeHardend(boolean rangeHardend)
    {
        this.rangeHardend = rangeHardend;
    }

    public void setFacetQueries(String facetQueries)
    {
        this.facetQueries = facetQueries;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public void setIncludeMetadata(boolean includeMetadata)
    {
        this.includeMetadata = includeMetadata;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {

        super.suspendTimer();

        DBObject dataObj = (DBObject) event.getData();
        if (dataObj == null)
        {
            throw new IllegalStateException("This processor requires data " + SiteContextConstants.SITE_ID);
        }

        String site = (String) dataObj.get(SiteContextConstants.SITE_ID);
        String member = (String) dataObj.get(SiteContextConstants.SITE_MEMBER);

        // Get the user for the selected site member
        UserData userdata = userDataService.findUserByUsername(member);
        if (userdata == null)
        {
            return new EventResult("Failure: The picked user doesn't exist anymore: " + member, false);
        }

        super.resumeTimer();
        int count = 0;

        if (searchType == null)
        {
            return new EventResult("Failure: Search type is missing!", false);
        }

        if (searchType.equalsIgnoreCase(SEARCH_SERVICES_SEARCH_TYPE))
        {
            count = performSearchSearvicesSearch(userdata);
        }
        else if (searchType.equalsIgnoreCase(INSIGHT_ENGINE_SEARCH_TYPE))
        {
            count = performInsightEngineSearch(userdata);
        }
        else
        {
            return new EventResult("Failure: Invalid search type.", false);
        }

        super.suspendTimer();

        DBObject resultData = BasicDBObjectBuilder.start().add("msg", "Searched in " + site + "/documentLibrary.")
                .add("count", count).get();

        Event nextEvent = new Event(eventSearchContent, null);
        return processStatusCode(resultData, getRestWrapper().getStatusCode(), nextEvent);
    }

    /**
     * Perform a SQL Search using Search Services API
     * 
     * @param userdata The user for the selected site member
     * @return The number of total items found on search.
     * @throws Exception
     */
    private int performSearchSearvicesSearch(UserData userdata) throws Exception
    {

        UserModel userModel = buildUserModel(userdata);

        // build the search model
        SearchRequest searchRequest = buildSearchRequest();

        // do the actual search
        SearchResponse container = getRestWrapper().authenticateUser(userModel).withSearchAPI().search(searchRequest);

        return container != null && container.getPagination() != null ? container.getPagination().getCount() : 0;
    }

    /**
     * Perform a SQL Search using Insight Engine API
     * 
     * @param userdata The user for the selected site member
     * @return The number of total items found on search.
     * @throws Exception
     */
    private int performInsightEngineSearch(UserData userdata) throws Exception
    {

        UserModel userModel = buildUserModel(userdata);

        // build the search model
        SearchSqlRequest searchRequest = buildSqlSearchRequest();

        // do the actual search
        RestResponse response = getRestWrapper().authenticateUser(userModel).withSearchSqlAPI()
                .searchSql(searchRequest);

        return response.getResponse().jsonPath().get("list.pagination.totalItems");
    }

    private UserModel buildUserModel(UserData userdata)
    {

        return new UserModel(userdata.getUsername(), userdata.getPassword());
    }

    private SearchRequest buildSearchRequest()
    {

        RestRequestQueryModel model = new RestRequestQueryModel();
        model.setQuery(searchQuery);
        model.setLanguage(searchLanguage);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(model);

        // set pagination properties
        Pagination pagination = new Pagination();
        pagination.setMaxItems(maxItems);
        pagination.setSkipCount(skipCount);
        searchRequest.setPaging(pagination);

        // add Range properties
        searchRequest.setRanges(getRanges());

        // add Facet queries
        List<FacetQuery> queries = parseFacetQueries(facetQueries);
        searchRequest.setFacetQueries(queries);

        return searchRequest;
    }

    private List<RestRequestRangesModel> getRanges()
    {
        if (rangeField == null || rangeField.isEmpty())
        {
            return null;
        }

        RestRequestRangesModel rangeModel = new RestRequestRangesModel();
        rangeModel.setField(rangeField);
        rangeModel.setStart(rangeStart);
        rangeModel.setEnd(rangeStop);
        rangeModel.setGap(rangeGap);
        rangeModel.setHardend(rangeHardend);

        List<RestRequestRangesModel> ranges = new ArrayList<RestRequestRangesModel>();
        ranges.add(rangeModel);

        return ranges;
    }

    private List<FacetQuery> parseFacetQueries(String facets)
    {
        List<FacetQuery> facetQueries = new ArrayList<FacetQuery>();

        // no facet queries to process
        if (facets == null || facets.isEmpty())
        {
            return facetQueries;
        }

        JSONArray facetsJson = new JSONArray(facets);
        for (int i = 0; i < facetsJson.length(); i++)
        {
            JSONObject entry = facetsJson.getJSONObject(i);

            FacetQuery fQuery = new FacetQuery();
            fQuery.setQuery(entry.get("query") == null ? "" : entry.get("query").toString());
            fQuery.setLabel(entry.get("label") == null ? "" : entry.get("label").toString());
            fQuery.setGroup(entry.get("group") == null ? "" : entry.get("group").toString());
            facetQueries.add(fQuery);
        }

        return facetQueries;
    }

    private SearchSqlRequest buildSqlSearchRequest()
    {
        SearchSqlRequest searchRequest = new SearchSqlRequest();

        searchRequest.setSql(searchQuery);
        searchRequest.setFormat(format);
        searchRequest.setIncludeMetadata(includeMetadata);
        searchRequest.setTimezone(timezone);

        return searchRequest;
    }
}
