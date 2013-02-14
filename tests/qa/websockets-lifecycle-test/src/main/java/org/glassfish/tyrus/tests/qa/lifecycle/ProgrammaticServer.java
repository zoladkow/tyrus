/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.tyrus.tests.qa.lifecycle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfiguration;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.glassfish.tyrus.tests.qa.handlers.BasicTextMessageHandler;
import org.glassfish.tyrus.tests.qa.regression.Issue;

public class ProgrammaticServer extends Endpoint {

    private static final Logger logger = Logger.getLogger(ProgrammaticServer.class.getCanonicalName());
    BasicTextMessageHandler mh;
    

    @Override
    public void onOpen(Session s, EndpointConfiguration ec) {
        logger.log(Level.INFO, "Someone connected:{0}", s.getRequestURI().toString());
        mh = ((ProgrammaticServerConfiguration)ec).getMessageHandler("messageHandler");
        mh.setSession(s);
        s.addMessageHandler(mh);
    }


    @Override
    public void onClose(Session s, CloseReason reason) {
        logger.log(Level.INFO, "Clossing the session: {0}", s.toString());
        final RemoteEndpoint remote = s.getRemote();
        try {
            
            
            if(!reason.getCloseCode().equals(CloseReason.CloseCodes.GOING_AWAY)) {
                throw new RuntimeException("CloseReason.CloseCode should be GOING_AWAY");
            }
            //should raise on error
            remote.sendString("Raise onError now - socket is closed");
            s.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onError(Session s, Throwable thr) {
        logger.log(Level.SEVERE, "onError: {0}", thr.getLocalizedMessage());
        logger.log(Level.SEVERE, "onError: {0}", thr.getMessage());
        if(Issue.TYRUS_94.isEnabled()) {
           logger.log(Level.SEVERE, "onError: cause: {0}", thr.getCause().getMessage());
        }
        final RemoteEndpoint remote = s.getRemote();
        /*
        try {
            //remote.sendString("onError");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        */
    }
}
