/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.connection.lettuce;

import static org.hamcrest.collection.IsIterableContainingInOrder.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnection.ReactiveStringCommands.KeyValue;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.test.TestSubscriber;

/**
 * @author Christoph Strobl
 */
public class LettuceReactiveStringCommandsTests extends LettuceReactiveCommandsTestsBase {

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void setShouldAddValueCorrectly() {

		Mono<Boolean> result = connection.stringCommands().set(KEY_1_BBUFFER, VALUE_1_BBUFFER);

		assertThat(result.block(), is(true));
		assertThat(nativeCommands.get(KEY_1), is(equalTo(VALUE_1)));
	}

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void setShouldAddValuesCorrectly() {

		Flux<Boolean> result = connection.stringCommands().set(Flux.fromIterable(
				Arrays.asList(new KeyValue(KEY_1_BBUFFER, VALUE_1_BBUFFER), new KeyValue(KEY_2_BBUFFER, VALUE_2_BBUFFER))));

		TestSubscriber<Boolean> subscriber = TestSubscriber.create();
		result.subscribe(subscriber);
		subscriber.await();

		subscriber.assertValueCount(2);
		assertThat(nativeCommands.get(KEY_1), is(equalTo(VALUE_1)));
		assertThat(nativeCommands.get(KEY_2), is(equalTo(VALUE_2)));
	}

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void getShouldRetriveValueCorrectly() {

		nativeCommands.set(KEY_1, VALUE_1);

		Mono<ByteBuffer> result = connection.stringCommands().get(KEY_1_BBUFFER);
		assertThat(result.block(), is(equalTo(VALUE_1_BBUFFER)));
	}

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void getShouldRetriveValuesCorrectly() {

		nativeCommands.set(KEY_1, VALUE_1);
		nativeCommands.set(KEY_2, VALUE_2);

		Flux<ByteBuffer> result = connection.stringCommands()
				.get(Flux.fromIterable(Arrays.asList(KEY_1_BBUFFER, KEY_2_BBUFFER)));

		TestSubscriber<ByteBuffer> subscriber = TestSubscriber.create();
		result.subscribe(subscriber);
		subscriber.await();

		subscriber.assertValueCount(2);
		subscriber.assertContainValues(new HashSet<>(Arrays.asList(VALUE_1_BBUFFER, VALUE_2_BBUFFER)));
	}

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void mGetShouldRetriveValueCorrectly() {

		nativeCommands.set(KEY_1, VALUE_1);
		nativeCommands.set(KEY_2, VALUE_2);

		Mono<List<ByteBuffer>> result = connection.stringCommands().mGet(Arrays.asList(KEY_1_BBUFFER, KEY_2_BBUFFER));
		assertThat(result.block(), contains(VALUE_1_BBUFFER, VALUE_2_BBUFFER));
	}

	/**
	 * @see DATAREDIS-525
	 */
	@Test
	public void mGetShouldRetriveValuesCorrectly() {

		nativeCommands.set(KEY_1, VALUE_1);
		nativeCommands.set(KEY_2, VALUE_2);

		Flux<List<ByteBuffer>> result = connection.stringCommands().mGet(
				Flux.fromIterable(Arrays.asList(Arrays.asList(KEY_1_BBUFFER, KEY_2_BBUFFER), Arrays.asList(KEY_2_BBUFFER))));

		TestSubscriber<List<ByteBuffer>> subscriber = TestSubscriber.create();
		result.subscribe(subscriber);
		subscriber.await();

		subscriber.assertValueCount(2);
		subscriber.assertContainValues(
				new HashSet<>(Arrays.asList(Arrays.asList(VALUE_1_BBUFFER, VALUE_2_BBUFFER), Arrays.asList(VALUE_2_BBUFFER))));
	}

}
