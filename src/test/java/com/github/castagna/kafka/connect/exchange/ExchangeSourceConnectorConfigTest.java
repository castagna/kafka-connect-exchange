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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class ExchangeSourceConnectorConfigTest {

	@Test
	public void testExchangeSourceConnectorConfig() throws IOException {
	      Properties properties = new Properties();
	      properties.load(VersionUtil.class.getResourceAsStream("/exchange-source-example.properties"));
	      
	      ExchangeSourceConnectorConfig config = new ExchangeSourceConnectorConfig(properties2Map(properties));
	      
	      properties.containsKey(ExchangeSourceConnectorConfig.EXCHANGE_CREDENTIALS);
	      properties.containsKey(ExchangeSourceConnectorConfig.EXCHANGE_URL);
	      properties.containsKey(ExchangeSourceConnectorConfig.TOPIC_CONF);
	      
	      assertEquals("com.github.castagna.kafka.connect.exchange.ExchangeSourceConnector", properties.getProperty("connector.class"));
	      assertEquals("./config/credentials.tsv", config.getCredentials());
	}

	public static Map<String,String> properties2Map (Properties properties) {
	      Map<String,String> map = new HashMap<String,String>();
	      for (Map.Entry<Object, Object> e : properties.entrySet()) {
	    	  String key = (String) e.getKey();
	    	  String value = (String) e.getValue();
	    	  map.put(key, value);
	      }
	      return map;
	}
	
}
