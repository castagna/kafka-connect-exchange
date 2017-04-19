package dev;

import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.IAsyncResult;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.FolderId;

public class NotificationsExample {

	public static void main(String[] args) throws Exception {
		ExchangeService service = new ExchangeService(Dev.EXCHANGE_VERSION);
		ExchangeCredentials credentials = new WebCredentials(Dev.EMAIL, Dev.PASSWORD);
		service.setCredentials(credentials);

		FolderId folderId = new FolderId(WellKnownFolderName.Inbox);
		List<FolderId> folder = new ArrayList<FolderId>();
		folder.add(folderId);
		

		IAsyncResult asyncresult = service.beginSubscribeToPullNotificationsOnAllFolders(null, null, 5, null, EventType.NewMail, EventType.Created, EventType.Deleted);
		PullSubscription subscription = service.endSubscribeToPullNotifications(asyncresult);
		GetEventsResults events = subscription.getEvents();

		System.out.println("events======" + events.getItemEvents());
		
		service.close();

	}

}
