/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
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
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.system.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeyCompare<A, B> {

	private final Collection<A> aCollection;
	private final Collection<B> bCollection;
	private final Function<A, String> keyOfA;
	private final Function<B, String> keyOfB;
	private final List<A> notInB = new ArrayList<>();
	private final List<A> inB = new ArrayList<>();
	private final List<B> notInA = new ArrayList<>();
	private final List<B> inA = new ArrayList<>();
	private Map<String, A> aByKey;
	private Map<String, B> bByKey;

	public KeyCompare(Collection<A> aCollection, Collection<B> bCollection, Function<A, String> keyOfA, Function<B, String> keyOfB) {
		this.aCollection = aCollection;
		this.bCollection = bCollection;
		this.keyOfA = keyOfA;
		this.keyOfB = keyOfB;
		compare();
	}

	private void compare() {
		aByKey = aCollection.stream().collect(Collectors.toMap(keyOfA, a -> a));
		bByKey = bCollection.stream().collect(Collectors.toMap(keyOfB, b -> b));
		for (A a : aCollection) {
			if (bByKey.containsKey(keyOfA.apply(a))) {
				inB.add(a);
			} else {
				notInB.add(a);
			}
		}
		for (B b : bCollection) {
			if (aByKey.containsKey(keyOfB.apply(b))) {
				inA.add(b);
			} else {
				notInA.add(b);
			}
		}
	}

	public A getA(B b) {
		return aByKey.get(keyOfB.apply(b));
	}

	public B getB(A a) {
		return bByKey.get(keyOfA.apply(a));
	}

	public List<A> getAEntriesNotInB() {
		return notInB;
	}

	public List<B> getBEntriesNotInA() {
		return notInA;
	}

	public List<A> getAEntriesInB() {
		return inB;
	}

	public List<B> getBEntriesInA() {
		return inA;
	}

	public boolean isDifferent() {
		return !notInA.isEmpty() || !notInB.isEmpty();
	}
}
