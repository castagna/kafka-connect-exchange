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

import java.security.GeneralSecurityException;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import microsoft.exchange.webservices.data.EWSConstants;
import microsoft.exchange.webservices.data.core.EwsSSLProtocolSocketFactory;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

public class CustomExchangeService extends ExchangeService {

	public CustomExchangeService(ExchangeVersion requestedServerVersion) {
		super(requestedServerVersion);
	}

	@Override
	protected Registry<ConnectionSocketFactory> createConnectionSocketFactoryRegistry() {
		try {
			return RegistryBuilder.<ConnectionSocketFactory>create()
					.register(EWSConstants.HTTP_SCHEME, new PlainConnectionSocketFactory())
					.register(EWSConstants.HTTPS_SCHEME, EwsSSLProtocolSocketFactory.build(null, NoopHostnameVerifier.INSTANCE))
					.build();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException();
		}

	}

}