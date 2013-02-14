/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.tests.qa;

import org.glassfish.tyrus.tests.qa.lifecycle.ProgrammaticClientConfiguration;
import org.glassfish.tyrus.tests.qa.lifecycle.ProgrammaticClient;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.glassfish.tyrus.server.Server;
import org.glassfish.tyrus.tests.qa.config.AppConfig;
import org.glassfish.tyrus.tests.qa.handlers.client.BasicTextMessageHandlerClient;
import org.glassfish.tyrus.tests.qa.handlers.server.BasicTextMessageHandlerServer;
import org.glassfish.tyrus.tests.qa.lifecycle.LifeCycleDeployment;
import org.glassfish.tyrus.tests.qa.regression.Issue;
import org.glassfish.tyrus.tests.qa.lifecycle.ProgrammaticServerConfiguration;
import org.glassfish.tyrus.tests.qa.tools.TyrusToolkit;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Michal Conos (michal.conos at oracle.com)
 */
public class LifeCycleTest {

    AppConfig testConf = new AppConfig(LifeCycleDeployment.CONTEXT_PATH, LifeCycleDeployment.PROGRAMMATIC_ENDPOINT);
    TyrusToolkit tyrus = new TyrusToolkit(testConf);

    @Test
    public void testLifeCycleProgrammatic() throws DeploymentException, InterruptedException, URISyntaxException {
        Issue.TYRUS_93.disable();
        Issue.TYRUS_94.disable();

        ProgrammaticServerConfiguration.registerMessageHandler("messageHandler", new BasicTextMessageHandlerServer());
        tyrus.registerEndpoint(ProgrammaticServerConfiguration.class);
        final Server server = tyrus.startServer();

        final CountDownLatch stopConversation = new CountDownLatch(1);
        WebSocketContainer wsc = ContainerProvider.getWebSocketContainer();
        Session clientSession = wsc.connectToServer(
                ProgrammaticClient.class,
                new ProgrammaticClientConfiguration(
                new BasicTextMessageHandlerClient()),
                testConf.getURI());
        // FIXME TC: clientSession.equals(lcSession)
        // FIXME TC: clientSession.addMessageHandler .. .throw excetpion
        stopConversation.await(10, TimeUnit.SECONDS);
        if (stopConversation.getCount() != 0) {
            fail();
        }

        tyrus.stopServer(server);
    }
}
