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

import org.alfresco.bm.driver.event.Event;
import org.alfresco.rest.model.RestNodeBodyModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang.RandomStringUtils;

public class CreateNodeTest extends RestTest
{

    public CreateNodeTest(String baseUrl)
    {
        super(baseUrl);
    }

    @Override
    protected void prepareData() throws Exception
    {
        restClient.authenticateUser(new UserModel(alfrescoAdminUsername, alfrescoAdminPassword));
    }

    @Override
    protected void restCall(Event event) throws Exception
    {
        RestNodeBodyModel node = new RestNodeBodyModel();
        node.setName(RandomStringUtils.randomAlphanumeric(8));
        node.setNodeType("cm:folder");

        restClient.withParams("autoRename=true").withCoreAPI().usingNode(ContentModel.my()).createNode(node);

    }

}
