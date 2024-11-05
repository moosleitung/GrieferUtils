/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.UNOBFUSCATED;

public class UnsafeJsonSerializer {

	private static final Unsafe UNSAFE;

	public static JsonElement toJson(Object o) {
		if (o == null)
			return JsonNull.INSTANCE;

		if (o instanceof Number n)
			return new JsonPrimitive(n);
		if (o instanceof Character c)
			return new JsonPrimitive(c);
		if (o instanceof Boolean b)
			return new JsonPrimitive(b);
		if (o instanceof String s)
			return new JsonPrimitive(s);

		if (o instanceof Iterable<?> it) {
			JsonArray array = new JsonArray();
			for (Object itO : it)
				array.add(toJson(itO));

			return array;
		}

		if (o instanceof Map<?,?> m)
			return toJson(m.entrySet());

		return serializeUnsafe(o);
	}

	private static JsonObject serializeUnsafe(Object o) {
		Class<?> clazz = o.getClass();
		JsonObject result = new JsonObject();

		while (clazz != Object.class) {
			for (Field declaredField : clazz.getDeclaredFields()) {
				if ((declaredField.getModifiers() & Modifier.STATIC) != 0)
					continue;

				String name = declaredField.getName();
				if (Mapper.isObfuscated())
					name = Mapper.mapField(clazz, name, Reflection.getMappingTarget(), UNOBFUSCATED);

				Object value = get(o, UNSAFE.objectFieldOffset(declaredField), declaredField.getType());
				result.add(name, toJson(value));
			}
			clazz = clazz.getSuperclass();
		}

		return result;
	}

	private static Object get(Object o, long offset, Class<?> type) {
		if (!type.isPrimitive())
			return UNSAFE.getObject(o, offset);

		if (type == byte.class)
			return UNSAFE.getByte(o, offset);
		if (type == short.class)
			return UNSAFE.getShort(o, offset);
		if (type == int.class)
			return UNSAFE.getInt(o, offset);
		if (type == long.class)
			return UNSAFE.getLong(o, offset);
		if (type == float.class)
			return UNSAFE.getFloat(o, offset);
		if (type == double.class)
			return UNSAFE.getDouble(o, offset);
		if (type == char.class)
			return UNSAFE.getChar(o, offset);
		if (type == boolean.class)
			return UNSAFE.getBoolean(o, offset);

		throw new IllegalArgumentException("Unknown primitive: " + type);
	}

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

	}

}