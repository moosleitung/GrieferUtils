/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.HIVEMIND_URL;

public class LogoutRequest extends Request<Void> {

	public LogoutRequest() {
		super(HIVEMIND_URL, "/logout");
	}

	@Override
	protected Void parseResponse(Response response) {
		return null;
	}

}