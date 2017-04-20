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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;

public class StructExample {
	
	public static final Schema KEY_SCHEMA = SchemaBuilder.struct()
			.name("com.github.castagna.kafka.connect.exchange.ConversationKey")
			.field("id", SchemaBuilder.string().doc("This is the id which identify a conversation in Exchange").build())
			.build();

	public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
			.name("com.github.CASTAGNA.kafka.connect.exchange.Conversation")
			.field("from", SchemaBuilder.string().doc("The text of a conversation.").build())
			.field("to", SchemaBuilder.string().doc("The text of a conversation.").build())
			.field("cc", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("bcc", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("date", Timestamp.builder().optional().doc("The date when a conversation was started.").build())
			.field("subject", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.field("body", SchemaBuilder.string().doc("The text of a conversation.").optional().build())
			.build();

	public static final Map<String, ?> EMPTY_MAP = new HashMap<>();
	
	public static void main(String[] args) {
	    Struct keyStruct = new Struct(KEY_SCHEMA);
	    Struct valueStruct = new Struct(VALUE_SCHEMA);
	    
	    keyStruct.put("id", "AAA");
	    
	    valueStruct.put("from", "paolo@domain.com");
	    valueStruct.put("to", "mark@domain.com");
	    valueStruct.put("subject", "Test");
	    valueStruct.put("body", "Hello!");	    
	    

	    SourceRecord sourceRecord = new SourceRecord(EMPTY_MAP, EMPTY_MAP, "topic name", KEY_SCHEMA, keyStruct, VALUE_SCHEMA, valueStruct);
	    System.out.println(sourceRecord);
	    
	}

}
