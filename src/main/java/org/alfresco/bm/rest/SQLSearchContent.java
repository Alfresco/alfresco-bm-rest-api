package org.alfresco.bm.rest;

import org.alfresco.bm.common.EventResult;
import org.alfresco.bm.driver.event.Event;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.search.SearchSqlRequest;
import org.alfresco.utility.model.UserModel;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;


public class SQLSearchContent extends RestTest 
{

	public static final String EVENT_SEARCH_CONTENT = "eventSearchContent";
	
	private String eventSearchContent;
	private UserDataService userDataService;
	
	private String searchQuery;
	private String format;
	private boolean includeMetadata;
	private String timezone;
	
	
	public SQLSearchContent() 
	{
		this.eventSearchContent = EVENT_SEARCH_CONTENT;
	}
	
	public void setEventSearchContent(String eventSearchContent) {
		this.eventSearchContent = eventSearchContent;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public void setIncludeMetadata(boolean includeMetadata) {
		this.includeMetadata = includeMetadata;
	}
	
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}


	@Override
	protected EventResult processEvent(Event event) throws Exception {
		
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
        SearchSqlRequest searchRequest = buildSqlSearchRequest();
        
        // do the actual search
        RestResponse response = restWrapper.authenticateUser(userModel).withSearchSqlAPI().searchSql(searchRequest);
        
        int count = response.getResponse().jsonPath().get("list.pagination.totalItems");
        
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
	
	private SearchSqlRequest buildSqlSearchRequest() 
	{
        SearchSqlRequest searchRequest = new SearchSqlRequest();
        
        searchRequest.setSql(searchQuery);
        searchRequest.setFormat(format);
        searchRequest.setIncludeMetadata(includeMetadata);
        //locales
        searchRequest.setTimezone(timezone);
        
        return searchRequest;
	}

}
