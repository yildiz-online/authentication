//        This file is part of the Yildiz-Online project, licenced under the MIT License
//        (MIT)
//
//        Copyright (c) 2016 Grégory Van den Borre
//
//        More infos available: http://yildiz.bitbucket.org
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

package be.yildiz.authentication.network;

import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.common.Token;
import be.yildiz.common.log.Logger;
import be.yildiz.module.network.AbstractHandler;
import be.yildiz.module.network.exceptions.InvalidNetworkMessage;
import be.yildiz.module.network.protocol.*;
import be.yildiz.module.network.server.Session;

/**
 * Handle the reception of authentication request and token verification request, call authentication logic and send the authentication response.
 *
 * @author Grégory Van den Borre
 */
class AuthenticationHandler extends AbstractHandler {

    /**
     * Object managing all the authentication logic.
     */
    private final AuthenticationManager manager;

    AuthenticationHandler(AuthenticationManager manager) {
        this.manager = manager;
    }

    /**
     * Can receive either be
     * <ul>
     * <li>an AuthenticationRequest from a client the authentication manager will authenticate and will answer an AuthenticationResponse containing the Token with the data to the client.
     * <li>a TokenVerificationRequest from a server, the authentication manager will provide a Token matching the request and will answer with a TokenVerificationResponse to provide the validity of
     * the request.
     * </ul>
     *
     * @param session Session sending the request.
     * @param message Message received, can only be an AuthenticationRequest or a TokenVerificationRequest, otherwise, session will be closed.
     */
    //@Requires (session != null)
    //@Requires (message != null)
    @Override
    public void messageReceivedImpl(final Session session, final MessageWrapper message) {
        try {
            int command = NetworkMessage.getCommandFromMessage(message);
            if (command == Commands.AUTHENTICATION_REQUEST) {
                AuthenticationRequest r = new AuthenticationRequest(message);
                Token token = this.manager.authenticate(r);
                Logger.debug("Send authentication response message to " + session.getPlayer() + " : " + token.getStatus());
                session.sendMessage(new AuthenticationResponse(token));
            } else if (command == Commands.TOKEN_VERIFICATION_REQUEST) {
                TokenVerificationRequest r = new TokenVerificationRequest(message);
                Token t = this.manager.getAuthenticated(r.getToken().getId());
                boolean authenticated = t.isAuthenticated() && t.getKey() == r.getToken().getKey();
                session.sendMessage(new TokenVerificationResponse(r.getToken().getId(), authenticated));
            } else {
                Logger.warning("Invalid message:" + message + " from " + session);
                session.disconnect();
            }
        } catch (InvalidNetworkMessage ex) {
            Logger.warning("Invalid message:" + message + " from " + session);
            session.disconnect();
        }
    }
}
