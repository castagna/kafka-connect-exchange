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

import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.ConversationId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

public class ListMessagesExample {

	public static void main(String[] args) throws Exception {
		ExchangeService service = new ExchangeService(Dev.EXCHANGE_VERSION);
		ExchangeCredentials credentials = new WebCredentials(Dev.EMAIL, Dev.PASSWORD);
		service.setCredentials(credentials);
		
	    ItemView view = new ItemView(100);
	    FindItemsResults<Item> findResults;
	    do {
	        findResults = service.findItems(WellKnownFolderName.Inbox, view);
	        for(Item item : findResults.getItems()) {
	            System.out.println(item.getSubject());
	            System.out.println(item.getId());
	            
	            // ???
	            List<Attachment> attachments = item.getAttachments().getItems();
	            for (Attachment attachment : attachments) {
					attachment.load();
				}
	            
	            ConversationId conversationId = item.getConversationId();
	            System.out.println(conversationId.getUniqueId());
	        }
	        view.setOffset(view.getOffset() + view.getPageSize());
	    } while (findResults.isMoreAvailable());
	    
	    service.close();
	}

}
