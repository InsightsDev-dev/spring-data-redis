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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public class LettuceReactiveKeyCommands implements ReactiveRedisConnection.ReactiveKeyCommands {

	private final LettuceReactiveRedisConnection connection;

	/**
	 * Create new {@link LettuceReactiveKeyCommands}.
	 * 
	 * @param connection must not be {@literal null}.
	 */
	public LettuceReactiveKeyCommands(LettuceReactiveRedisConnection connection) {

		Assert.notNull(connection, "Connection must not be null!");
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.ReactiveRedisConnection.ReactiveKeyCommands#del(org.reactivestreams.Publisher)
	 */
	@Override
	public Flux<Tuple2<ByteBuffer, Long>> del(Publisher<ByteBuffer> keys) {

		return connection.execute(cmd -> {

			return Flux.zip(keys, Flux.from(keys).flatMap(key -> {
				return LettuceReactiveRedisConnection.<Long> monoConverter().convert(cmd.del(key.array()));
			}));
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.ReactiveRedisConnection.ReactiveKeyCommands#mDel(org.reactivestreams.Publisher)
	 */
	@Override
	public Flux<Tuple2<List<ByteBuffer>, Long>> mDel(Publisher<List<ByteBuffer>> keys) {

		return connection.execute(cmd -> {

			return Flux.zip(keys, Flux.from(keys).flatMap(key -> {

				return LettuceReactiveRedisConnection.<Long> monoConverter().convert(
						cmd.del(key.stream().map(ByteBuffer::array).collect(Collectors.toList()).toArray(new byte[key.size()][])));
			}));
		});
	}

}
