/*
 * Andreas Steffan - http://www.contentreich.de
 *
 * Created on Sep 30, 2011
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.contentreich.scripting.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;

public abstract class ScriptService implements ApplicationContextAware,
		Lifecycle {
	protected Logger logger = Logger.getLogger(getClass());

	private boolean launchAtStart;
	protected ApplicationContext applicationContext;

	public ScriptService() {
		super();
	}

	abstract void startAsync();

	@Override
	public void start() {
		if (launchAtStart) {
			startAsync();
		}
	}

	abstract Map<String, Object> getBindings();

	public boolean isLaunchAtStart() {
		return launchAtStart;
	}

	public void setLaunchAtStart(boolean launchAtStart) {
		this.launchAtStart = launchAtStart;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public synchronized void enable() {
		startAsync();
	}

	public abstract void disable();

	public void toggleEnabled() {
		if (isRunning()) {
			disable();
		} else {
			enable();
		}
	}
}
