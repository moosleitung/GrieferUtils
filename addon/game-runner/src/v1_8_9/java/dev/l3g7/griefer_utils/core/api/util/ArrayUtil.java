/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

/**
 * A utility class for array stuff.
 */
public class ArrayUtil {

	/**
	 * @return whether two arrays are equal using a custom function.
	 */
	public static <A, B> boolean equals(A[] a, B[] b, BiPredicate<A, B> check) {
		if(a.length != b.length)
			return false;

		for(int i = 0; i < a.length; i++)
			if(!check.test(a[i], b[i]))
				return false;

		return true;
	}

	/**
	 * @return a string representation of an array using a custom function.
	 */
	public static <T> String toString(T[] array, Function<T, String> func, String delimiter) {
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < array.length; i++) {
			if(i != 0)
				s.append(delimiter);

			s.append(func.apply(array[i]));
		}

		return s.toString();
	}

	/**
	 * @return the last entry of an array, or null, if the array is empty.
	 */
	public static <T> T last(T[] array) {
		if (array.length == 0)
			return null;

		return array[array.length - 1];
	}

	/**
	 * Maps an iterable using a function.
	 * @see Stream#map(Function)
	 */
	public static <V, R> List<R> map(Iterable<V> t, Function<V, R> mapFunc) {
		return StreamSupport.stream(t.spliterator(), false)
			.map(mapFunc)
			.collect(Collectors.toList());
	}

	/**
	 * Maps an array using a function.
	 * @see Stream#map(Function)
	 */
	public static <V, R> List<R> map(V[] t, Function<V, R> mapFunc) {
		return Arrays.stream(t)
			.map(mapFunc)
			.collect(Collectors.toList());
	}

	/**
	 * Flatmaps an array.
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <T> T[] flatmap(Class<T> type, T[]... array) {
		// Convert type to array class
		return c(Arrays.stream(array)
			.flatMap(Stream::of)
			.collect(Collectors.toList())
			.toArray((T[]) Array.newInstance(type, 0)));
	}

	/**
	 * Merges multiple byte arrays into one.
	 */
	public static byte[] merge(byte[]... arrays) {
		int length = 0;
		for (byte[] array : arrays)
			length += array.length;

		byte[] result = new byte[length];
		int idx = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, idx, array.length);
			idx += array.length;
		}
		return result;
	}

	/**
	 * Splits a byte array at the given position.
	 */
	public static Pair<byte[], byte[]> split(byte[] array, int pos) {
		byte[] a = new byte[pos];
		byte[] b = new byte[array.length - pos];
		System.arraycopy(array, 0, a, 0, pos);
		System.arraycopy(array, pos, b, 0, b.length);
		return Pair.of(a, b);
	}

}
