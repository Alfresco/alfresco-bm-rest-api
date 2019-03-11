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
 *
 */
public class CustomSearchEventWeight extends EventWeight {

	private String searchType;

	public CustomSearchEventWeight(String eventName, double weight) {
		super(eventName, weight);
	}

	public CustomSearchEventWeight(String eventName, String weights) {
		super(eventName, -1.0, weights);
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	/**
	 * Get the weight based on event name and search type. Return weight if and only
	 * if we have one of these combinations: 
	 * rest-collab.scenario.03.search with Search Type: Search Services Search 
	 * or 
	 * rest-collab.scenario.04.search with SearchType: Insight Engine Search
	 * This is because scenario.03 and scenario.04 are not running on the same type.
	 */
	@Override
	public double getWeight() {

		if (isSearchServicesSearch() || isInsightEngineSearch()) {
			return super.getWeight();
		}
		return 0;
	}

	private boolean isSearchServicesSearch() {
		return getEventName().equalsIgnoreCase(EVENT_SEARCH_SERVICES)
				&& searchType.equalsIgnoreCase(SEARCH_SERVICES_SEARCH_TYPE);
	}

	private boolean isInsightEngineSearch() {
		return getEventName().equalsIgnoreCase(EVENT_INSIGHT_ENGINE)
				&& searchType.equalsIgnoreCase(INSIGHT_ENGINE_SEARCH_TYPE);
	}

}
