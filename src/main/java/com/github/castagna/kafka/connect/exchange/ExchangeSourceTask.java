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

package com.github.castagna.kafka.connect.exchange;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.SyncFolderItemsScope;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.sync.ChangeCollection;
import microsoft.exchange.webservices.data.sync.ItemChange;

public class ExchangeSourceTask extends SourceTask {

	static final Logger log = LoggerFactory.getLogger(ExchangeSourceTask.class);
	// private static final RedirectionUrlCallback redirectionUrlCallback = new RedirectionUrlCallback();
	protected int task;
	protected int maxTasks;

	protected ExchangeSourceConnectorConfig config;

	protected ArrayList<String> emails = new ArrayList<String>();
	protected ArrayList<String> passwords = new ArrayList<String>();
	protected ArrayList<String> domains = new ArrayList<String>();
	protected ArrayList<String> watermarks = new ArrayList<String>();
	
	protected CustomExchangeService service;
	protected PropertySet propertySet;
	protected FolderId folderId;
	
	public static final Schema KEY_SCHEMA = SchemaBuilder.struct()
			.name("com.github.castagna.kafka.connect.exchange.ConversationKey")
			.field("conversation_id", SchemaBuilder.string().doc("This is the id which identify a conversation in Exchange").build())
			.build();

	public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
			.name("com.github.castagna.kafka.connect.exchange.Conversation")
			.field("conversation_id", SchemaBuilder.string().doc("This is the id which identify a conversation in Exchange").build())
			.field("item_id", SchemaBuilder.string().doc("This is the id which identify an item in Exchange").build())
			.field("from", SchemaBuilder.string().doc("The text of a conversation.").build())
			.field("to", SchemaBuilder.string().doc("The text of a conversation.").build())
			.field("cc", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("bcc", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("date", Timestamp.builder().optional().doc("The date when a conversation was started.").build())
			.field("subject", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("body", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.build();

	public static final Map<String, ?> EMPTY_MAP = new HashMap<>();

	@Override
	public void start(Map<String, String> settings) {
	    task = Integer.parseInt(settings.get("task"));
	    maxTasks = Integer.parseInt(settings.get("maxTasks"));
	    log.info("Starting Exchange Source task {} of {} ...", task, maxTasks);
	    this.config = new ExchangeSourceConnectorConfig(settings);
	    loadCredentials();
	    initTrustManager();
	    try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<SourceRecord>();
		for (int i = 0; i < emails.size(); i++) {
			String email = emails.get(i);
			String password = passwords.get(i);
			String domain = domains.get(i);
			
			log.info("Loading emails for {}...", email);

	        ExchangeCredentials credentials = new WebCredentials(email, password, domain);
			service.setCredentials(credentials);
			
            ChangeCollection<ItemChange> itemChanges;
			try {
                itemChanges = syncFolder(service, propertySet, folderId, watermarks.get(i));
                log.info("Found {} email starting from watermark {} ...", itemChanges.getCount(), watermarks.get(i));

                ArrayList<Item> items = new ArrayList<Item>();
                for (ItemChange itemChange : itemChanges) {
                    items.add(itemChange.getItem());
                }
                service.loadPropertiesForItems(items, propertySet);
                
                for (Item item : items) {
                    records.add(createSourceRecord(item));                	
                }
                
                watermarks.set(i, itemChanges.getSyncState()); 
                
                // TODO: watermarks should be committed back to Kafka so that if the connector is interrupted and must be restarted it will start from there
                
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("Generated {} records ...", records.size());
		return records;
	}
	
	private SourceRecord createSourceRecord (Item item) throws ServiceLocalException {
	    Struct keyStruct = new Struct(KEY_SCHEMA);
	    Struct valueStruct = new Struct(VALUE_SCHEMA);
	    
	    keyStruct.put("conversation_id", item.getConversationId());
	    
	    // TODO: valueStruct.put("from", item.???);
	    valueStruct.put("item_id", item.getId());
	    valueStruct.put("conversation_id", item.getConversationId());
	    valueStruct.put("to", item.getDisplayTo());
	    valueStruct.put("cc", item.getDisplayCc());
	    valueStruct.put("subject", item.getSubject());
	    valueStruct.put("body", item.getBody());
	    valueStruct.put("date", item.getDateTimeSent());

	    SourceRecord record = new SourceRecord(EMPTY_MAP, EMPTY_MAP, config.getTopic(), KEY_SCHEMA, keyStruct, VALUE_SCHEMA, valueStruct);
	    if (log.isDebugEnabled()) {
	    	log.debug("Created record {}", record);
	    }
	    return record;
	}

    private ChangeCollection<ItemChange> syncFolder(ExchangeService service, PropertySet propertySet, FolderId folderId, String watermark) throws Exception {
        return service.syncFolderItems(folderId, propertySet, null, 256, SyncFolderItemsScope.NormalAndAssociatedItems, watermark); // TODO: make the items returned configurable
    }
	
	@Override
	public void stop() {
		service.close();
	    log.info("Stopping Exchange Source task...");
	}

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	private void loadCredentials() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(config.getCredentials()));
			String line = in.readLine();
			int i = 0;
			while (line != null) {
				if (i % maxTasks == task) {
					String[] s = line.split("\t");
					emails.add(s[0]);
					passwords.add(s[1]);
					domains.add(s[2]);
					watermarks.add(null);
				}
				line = in.readLine(); 
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}

	private void init() throws Exception {
		service = new CustomExchangeService(ExchangeVersion.Exchange2010_SP2);
		URI uri = new URI(config.getUrl());
		service.setUrl(uri);
        propertySet = new PropertySet(BasePropertySet.FirstClassProperties);
        propertySet.setRequestedBodyType(BodyType.Text);
        Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox, propertySet);
        folderId = inbox.getId();
        log.info ("Connected to folder {} at {}", folderId, config.getUrl());
	}
	
	private void initTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {}
	}

	static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
		public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
			return redirectionUrl.toLowerCase().startsWith("https://");
		}
	}

}