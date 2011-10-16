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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.QuitAction;
import org.mozilla.javascript.tools.shell.ShellContextFactory;

public class JavascriptShellThread extends Thread {
	private ToolErrorReporter errorReporter;
	public ShellContextFactory shellContextFactory;
	public Global global = new Global();
	private static final Logger logger = Logger
			.getLogger(JavascriptShellThread.class);
	private Socket socket;
	private JavascriptShellService service;
	private Map javaBinding;

	public JavascriptShellThread(Socket socket, Map javaBinding,
			JavascriptShellService service) {
		super();
		this.socket = socket;
		this.service = service;
		this.javaBinding = javaBinding;
	}

	@Override
	public void run() {
		try {
			logger.debug("Starting shell");
			PrintStream out = new PrintStream(socket.getOutputStream());
			InputStream in = socket.getInputStream();
			Actions actions = new Actions();
			global = new Global();
			global.setIn(in);
			global.setOut(out);
			global.setErr(out);
			global.initQuitAction(actions);
			shellContextFactory = new ShellContextFactory();
			errorReporter = new ToolErrorReporter(false, global.getErr());
			shellContextFactory.setErrorReporter(errorReporter);
			global.init(shellContextFactory);
			service.initGlobal(global);
			// global.defineFunctionProperties(new String[] { "testTx" }, TestFunctions.class, ScriptableObject.DONTENUM);
			shellContextFactory.call(actions);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.debug("Shell finished");
			terminate();
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public synchronized void terminate() {
		if (socket.isConnected()) {
			try {
				logger.debug("Terminating - Closing socket/streams");
				global.getOut().close();
				global.getIn().close();
				socket.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.debug("Socket not connected - nothing to to");
		}
		service.removeThread(this);
	}
	/*
	private void initJavaPolicySecuritySupport() {
		Throwable exObj;
		try {
			Class cl = Class
					.forName("org.mozilla.javascript.tools.shell.JavaPolicySecurity");
			securityImpl = (SecurityProxy) cl.newInstance();
			SecurityController.initGlobal(securityImpl);
			return;
		} catch (ClassNotFoundException ex) {
			exObj = ex;
		} catch (IllegalAccessException ex) {
			exObj = ex;
		} catch (InstantiationException ex) {
			exObj = ex;
		} catch (LinkageError ex) {
			exObj = ex;
		}
		throw Kit.initCause(new IllegalStateException(
				"Can not load security support: " + exObj), exObj);
	}
    */
	/**
	 * Proxy class to avoid proliferation of anonymous classes.
	 */
	private class Actions implements ContextAction, QuitAction {
		public Object run(Context cx) {
			service.addObjects(javaBinding, global);
			processSource(cx);
			return null;
		}

		public void quit(Context cx, int exitCode) {
			terminate();
			// throw Kit.codeBug();
		}
	}

	public void processSource(Context cx) {
			PrintStream ps = global.getErr();
			ps.println(cx.getImplementationVersion());

			// Use the interpreter for interactive input
			cx.setOptimizationLevel(-1);
			BufferedReader br = new BufferedReader(new InputStreamReader(global.getIn()));
			int lineno = 1;
			boolean hitEOF = false;
			while (!hitEOF) {
				ps.print("js> ");
				ps.flush();
				String source = "";

				// Collect lines of source to compile.
				while (true) {
					String newline;
					try {
						newline = br.readLine();
					} catch (IOException ioe) {
						ps.println(ioe.toString());
						break;
					}
					if (newline == null) {
						hitEOF = true;
						break;
					}
					source = source + newline + "\n";
					lineno++;
					if (cx.stringIsCompilableUnit(source))
						break;
				}
				Script script = loadScriptFromSource(cx, source, "<stdin>",
						lineno, null);
				if (script != null) {
					Object result = evaluateScript(script, cx, global);
					// Avoid printing out undefined or function definitions.
					if (result != Context.getUndefinedValue()
							&& !(result instanceof Function && source.trim()
									.startsWith("function"))) {
						try {
							ps.println(Context.toString(result));
						} catch (RhinoException rex) {
							ToolErrorReporter.reportException(
									cx.getErrorReporter(), rex);
						}
					}
					NativeArray h = (NativeArray) ScriptableObject.getProperty(global, "history");
					h.put((int) h.getLength(), h, source);
				}
			}
			ps.println();
		// System.gc();
	}

	public static Object evaluateScript(Script script, Context cx,
			Scriptable scope) {
		try {
			return script.exec(cx, scope);
		} catch (RhinoException rex) {
			ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
			// exitCode = EXITCODE_RUNTIME_ERROR;
		} catch (VirtualMachineError ex) {
			// Treat StackOverflow and OutOfMemory as runtime errors
			logger.error(ex.getMessage(), ex);
			String msg = ToolErrorReporter.getMessage(
					"msg.uncaughtJSException", ex.toString());
			// exitCode = EXITCODE_RUNTIME_ERROR;
			Context.reportError(msg);
		}
		return Context.getUndefinedValue();
	}

	public static Script loadScriptFromSource(Context cx, String scriptSource,
			String path, int lineno, Object securityDomain) {
		try {
			return cx.compileString(scriptSource, path, lineno, securityDomain);
		} catch (EvaluatorException ee) {
			// Already printed message.
			// exitCode = EXITCODE_RUNTIME_ERROR;
		} catch (RhinoException rex) {
			ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
			// exitCode = EXITCODE_RUNTIME_ERROR;
		} catch (VirtualMachineError ex) {
			// Treat StackOverflow and OutOfMemory as runtime errors
			ex.printStackTrace();
			String msg = ToolErrorReporter.getMessage(
					"msg.uncaughtJSException", ex.toString());
			// exitCode = EXITCODE_RUNTIME_ERROR;
			Context.reportError(msg);
		}
		return null;
	}

    /*
     * Initializes a scope for script execution. The easiest way to embed Rhino is just to create a new scope this
     * way whenever you need one. However, initStandardObjects() is an expensive method to call and it allocates a
     * fair amount of memory.
     * 
     * @param cx        the thread execution context
     * @param secure    Do we consider the script secure? When <code>false</code> this ensures the script may not
     *                  access insecure java.* libraries or import any other classes for direct access - only the
     *                  configured root host objects will be available to the script writer.
     * @param sealed    Should the scope be sealed, making it immutable? This should be <code>true</code> if a scope
     *                  is to be reused.
     * @return the scope object
    protected Scriptable createScope(Context cx, boolean secure, boolean sealed, Map model)
    {
        Scriptable scope;
        if (secure)
        {
            // Initialise the non-secure scope
            // allow access to all libraries and objects, including the importer
            // @see http://www.mozilla.org/rhino/ScriptingJava.html
            scope = new ImporterTopLevel(cx, sealed);
        }
        else
        {
            // Initialise the secure scope
            scope = cx.initStandardObjects(null, sealed);
            // remove security issue related objects - this ensures the script may not access
            // unsecure java.* libraries or import any other classes for direct access - only
            // the configured root host objects will be available to the script writer
            scope.delete("Packages");
            scope.delete("getClass");
            scope.delete("java");
        }
        return scope;
    }
    */
}
