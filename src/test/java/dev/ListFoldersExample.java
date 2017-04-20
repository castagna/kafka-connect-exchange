/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;

public class ListFoldersExample {

	public static void main(String[] args) throws Exception {
		ExchangeService service = new ExchangeService(Dev.EXCHANGE_VERSION);
		ExchangeCredentials credentials = new WebCredentials(Dev.EMAIL, Dev.PASSWORD);
		service.setCredentials(credentials);
		
		PropertySet idOnly = new PropertySet(BasePropertySet.IdOnly);
        Folder root = Folder.bind(service, WellKnownFolderName.MsgFolderRoot, idOnly);

        FindFoldersResults folders=service.findFolders(root.getId(),new FolderView(Integer.MAX_VALUE));

        for (Folder f:folders.getFolders()) {
            // f.getDisplayName()
        	// f.getFolderClass()
        }		
		service.close();

	}

}
