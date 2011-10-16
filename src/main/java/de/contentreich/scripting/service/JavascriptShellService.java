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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JavascriptShellService extends ScriptService implements JavascriptObjectInitializer {
	public JavascriptObjectInitializer javascriptObjectInitializer;
    protected Thread serverThread;
	private ServerSocket serverSocket;
	private int port;
	private HashMap<String, JavascriptShellThread> threads = new HashMap<String, JavascriptShellThread>();
	private Map<String, String> preferences;
	private Map<String, Object> bindings = new HashMap<String, Object>(); 

	public JavascriptShellService() {
		super();
	}

	synchronized void startAsync() {
		if (!isRunning()) {
			logger.info("Starting Service ...");
			serverThread = new Thread() {
				@Override
				public void run() {
					try {
						serverSocket = new ServerSocket(port);
						logger.info(serverSocket + " created");

						while (true) {
							try {
								Socket clientSocket = serverSocket.accept();
								logger.info("Got connection with client socket: "
										+ clientSocket);
								JavascriptShellThread clientThread = new JavascriptShellThread(clientSocket, getBindings(),
										getJavascriptShellService());
								addThread(clientThread);
								clientThread.start();
							} catch (IOException e) {
								logger.info("Service stopped : "
										+ e.getMessage());
								return;// Exit !
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (serverSocket.isBound()) {
							try {
								serverSocket.close();
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
						}
						logger.info("Terminating all shell threads");
						Iterator<String> it = threads.keySet().iterator();
						while (it.hasNext()) {
							String key = it.next();
							JavascriptShellThread thread = threads.get(key);
							thread.terminate();
						}
					}

				}

			};
			serverThread.setDaemon(true);
			serverThread.start();

		} else {
			logger.warn("Already running");
		}

	}

	@Override
	public void stop() {
		disable();
	}

	public void setPort(int socket) {
		this.port = socket;
	}

	public int getPort() {
		return port;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	@Override
	public synchronized void disable() {
		if (isRunning()) {
			logger.info("Disabling - closing server socket " + serverSocket);
			try {
				serverSocket.close();
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
			int sz = threads.size();
			while (sz > 0) {
				logger.debug("Waiting for " + sz + " shell threads to terminate");
				try {
					wait();
					sz = threads.size();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.debug("No more shell threads running");
		} else {
			logger.debug("Not running");
		}
	}

	@Override
	public boolean isRunning() {
		return (serverThread != null && serverThread.isAlive());
	}

	JavascriptShellService getJavascriptShellService() {
		return this;
	}

	synchronized void removeThread(JavascriptShellThread thread) {
		threads.remove(thread.getName());
		notify();
	}

	synchronized void addThread(JavascriptShellThread thread) {
		threads.put(thread.getName(), thread);
		notify();/* Not needed I guess ?*/
	}

	@Override
	Map<String, Object> getBindings() {
		return bindings;
	}

	public void setBindings(Map<String, Object> bindings) {
		this.bindings = bindings;
	}

	public JavascriptObjectInitializer getJavascriptObjectInitializer() {
		return javascriptObjectInitializer;
	}

	public void setJavascriptObjectInitializer(
			JavascriptObjectInitializer javascriptObjectInitializer) {
		this.javascriptObjectInitializer = javascriptObjectInitializer;
	}

	@Override
	public void initGlobal(ScriptableObject scope) {
		if (this.javascriptObjectInitializer != null) {
			this.javascriptObjectInitializer.initGlobal(scope);
		}
	}

	@Override
	public void addObjects(Map<String, Object> model, Scriptable scope) {
		if (this.javascriptObjectInitializer != null) {
			this.javascriptObjectInitializer.addObjects(model, scope);
		}
	}

	@Override
	public void initContext(Context context) {
		if (this.javascriptObjectInitializer != null) {
			this.javascriptObjectInitializer.initContext(context);
		}
	}
}
