package org.alfresco.bm.rest;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestRequestRangesModel;
import org.alfresco.rest.search.FacetQuery;
import org.alfresco.rest.search.Pagination;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.utility.model.UserModel;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;


/**
 * <b>INPUT:   </b>A site id, a random site member of the site and the text/query to search for <br/>
 * <b>ACTIONS: </b>Search for the text/query on the random site<br/>
 * <b>OUTPUT:  </b>List all the documents that match the search query<br/>
 * 
 * @author Cristina Diaconu
 *
 */
public class SearchContent extends RestTest 
{
	public static final String EVENT_SEARCH_CONTENT = "eventSearchContent";

	private String eventSearchContent;
	
	private UserDataService userDataService;
	
	private String searchQuery;
	private String searchLanguage;
	private int maxItems;
	private int skipCount;
	
	//range properties
	private String rangeField;
	private String rangeStart;
	private String rangeStop;
	private String rangeGap;
	private boolean rangeHardend;
	
	private String facetQueries;
	
	
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
	
	public void setRangeField(String rangeField) {
		this.rangeField = rangeField;
	}

	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	public void setRangeStop(String rangeStop) {
		this.rangeStop = rangeStop;
	}

	public void setRangeGap(String rangeGap) {
		this.rangeGap = rangeGap;
	}

	public void setRangeHardend(boolean rangeHardend) {
		this.rangeHardend = rangeHardend;
	}
	
	public void setFacetQueries(String facetQueries) {
		this.facetQueries = facetQueries;
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
        
        UserModel userModel = buildUserModel(userdata);
        RestWrapper restWrapper = getRestWrapper();
        
        super.resumeTimer();
        
        // build the search model 
        SearchRequest searchRequest = buildSearchRequest();
        
        // do the actual search
        SearchResponse container = restWrapper.authenticateUser(userModel).withSearchAPI().search(searchRequest);
      
        int count = container.getPagination() != null ? container.getPagination().getCount() : 0;
        
        super.suspendTimer();
        
        DBObject resultData = BasicDBObjectBuilder.start().add("msg", "Searched in " + site + "/documentLibrary.")
                                                          .add("count", count).get();

        Event nextEvent = new Event(eventSearchContent, null);
        return processStatusCode(resultData, getRestWrapper().getStatusCode(), nextEvent);
	}
	
	private UserModel buildUserModel(UserData userdata) 
	{
		UserModel userModel = new UserModel();
        userModel.setUsername(userdata.getUsername());
        userModel.setPassword(userdata.getPassword());
        
        return userModel;
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
       
        // add Range parameters
        searchRequest.setRanges(getRanges());
        
        //add Facet queries 
        List<FacetQuery> queries = parseFacetQueries(facetQueries);
        searchRequest.setFacetQueries(queries);
        
        return searchRequest;
	}
	
	private List<RestRequestRangesModel> getRanges() {
		
		if (rangeField == null || rangeField.isEmpty()) {
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
	
	private List<FacetQuery> parseFacetQueries(String facets) {
		
		List<FacetQuery> facetQueries = new ArrayList<FacetQuery>();
		
		// no facet queries to process
		if (facets == null || facets.isEmpty()) {
			return facetQueries;
		}
		
		JSONArray facetsJson = new JSONArray(facets);
		for (int i = 0; i < facetsJson.length(); i++)
        {
            JSONObject entry = facetsJson.getJSONObject(i);

			FacetQuery fQuery = new FacetQuery();
			fQuery.setQuery( entry.get("query") == null ? "" : entry.get("query").toString());
			fQuery.setLabel( entry.get("label") == null ? "" : entry.get("label").toString());
			fQuery.setGroup( entry.get("group") == null ? "" : entry.get("group").toString());
			facetQueries.add(fQuery);
        }
		
		return facetQueries;
	}

}
