/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.xml.languageserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;

public class XMLServerLauncher {

	/**
	 * Calls {@link #launch(InputStream, OutputStream)}, using the standard input
	 * and output streams.
	 */
	public static void main(String[] args) {
		launch(System.in, System.out);
	}

	/**
	 * Launches {@link XMLLanguageServer} and makes it accessible through the
	 * JSON RPC protocol defined by the LSP.
	 * 
	 * @param launcherFuture The future returned by
	 *                       {@link org.eclipse.lsp4j.jsonrpc.Launcher#startListening()}.
	 *                       (I'm not 100% sure how it meant to be used though, as it's
	 *                       undocumented...)
	 */
	public static Future<?> launch(InputStream in, OutputStream out) {
		XMLLanguageServer server = new XMLLanguageServer();
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
		server.setClient(launcher.getRemoteProxy());
		
		/*Collection<ICompletionParticipant> completionParticipants = new ArrayList<ICompletionParticipant>();
		ServiceLoader<ICompletionParticipant> loader = ServiceLoader.load(ICompletionParticipant.class);
		loader.forEach(p -> completionParticipants.add(p));
		System.err.println("{\"nb\": " + completionParticipants.size() + "}");
		*/
		return launcher.startListening();
	}
}
