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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeSourceTask extends SourceTask {

	static final Logger log = LoggerFactory.getLogger(ExchangeSourceTask.class);
	private static final RedirectionUrlCallback redirectionUrlCallback = new RedirectionUrlCallback();
	protected int task;
	protected int maxTasks;

	protected ExchangeSourceConnectorConfig config;

	protected ArrayList<String> emails = new ArrayList<String>();
	protected ArrayList<String> passwords = new ArrayList<String>();
	

	@Override
	public void start(Map<String, String> settings) {
	    task = Integer.parseInt(settings.get("task"));
	    maxTasks = Integer.parseInt(settings.get("maxTasks"));
	    log.info("Starting Exchange Source task {} of {} ...", task, maxTasks);
	    this.config = new ExchangeSourceConnectorConfig(settings);
	    loadCredentials();
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		for (int i = 0; i < emails.size(); i++) {
			String email = emails.get(i);
			String password = passwords.get(i);

			ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
			ExchangeCredentials credentials = new WebCredentials(email, password);
			service.setCredentials(credentials);
			try {
				service.autodiscoverUrl(email, redirectionUrlCallback);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO: Create SourceRecord objects that will be sent the kafka cluster.
		throw new UnsupportedOperationException("This has not been implemented.");
	}

	@Override
	public void stop() {
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
	
	static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
		public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
			return redirectionUrl.toLowerCase().startsWith("https://");
		}
	}

}