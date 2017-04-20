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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExchangeSourceTaskTest {

	private static ExchangeSourceTask task;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties properties = new Properties();
		properties.load(VersionUtil.class.getResourceAsStream("/exchange-source-example.properties"));
		properties.put("task", "0");
		properties.put("maxTasks", "3");
		Map<String, String> settings = ExchangeSourceConnectorConfigTest.properties2Map(properties);
		
		task = new ExchangeSourceTask();
		task.start(settings);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		task.stop();	
	}
	
	@Test
	public void testConfiguration() {
		assertEquals(0, task.task);
		assertEquals(3, task.maxTasks);
	}
	
	@Test
	public void testCredentialsLoaded() {
		assertFalse(task.emails.isEmpty());
		assertFalse(task.passwords.isEmpty());
		assertFalse(task.domains.isEmpty());
		assertEquals(2, task.emails.size());
		assertEquals(2, task.passwords.size());
		assertEquals(2, task.domains.size());
		assertEquals("paolo@domain.com", task.emails.get(0)); 
		assertEquals("password1", task.passwords.get(0)); 
		assertEquals("london", task.domains.get(0)); 
		assertEquals("david@domain.com", task.emails.get(1)); 
		assertEquals("password4", task.passwords.get(1)); 
		assertEquals("madrid", task.domains.get(1)); 
	}
}