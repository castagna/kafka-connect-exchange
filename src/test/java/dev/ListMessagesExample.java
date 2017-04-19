package dev;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
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
	            ConversationId conversationId = item.getConversationId();
	            System.out.println(conversationId.getUniqueId());
	        }
	        view.setOffset(view.getOffset() + view.getPageSize());
	    } while (findResults.isMoreAvailable());
	    
	    service.close();
	}

}
