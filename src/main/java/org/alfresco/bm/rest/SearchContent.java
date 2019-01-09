package org.alfresco.bm.rest;

import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.search.Pagination;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.utility.model.UserModel;

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
        
        RestRequestQueryModel model = new RestRequestQueryModel();
        model.setQuery(searchQuery);
        model.setLanguage(searchLanguage);
        
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(model);
        Pagination pagination = new Pagination();
        pagination.setMaxItems(maxItems);
        pagination.setSkipCount(skipCount); 
        searchRequest.setPaging(pagination);
            
        SearchResponse container = restWrapper.authenticateUser(userModel).withSearchAPI().search(searchRequest);
      
        int count = container.getPagination() != null ? container.getPagination().getCount() : 0;
        
        super.suspendTimer();
        
        DBObject resultData = BasicDBObjectBuilder.start().add("msg", "Searched in " + site + "/documentLibrary.")
                                                          .add("count", count).get();

        Event nextEvent = new Event(eventSearchContent, null);
        return processStatusCode(resultData, getRestWrapper().getStatusCode(), nextEvent);
	}

}
