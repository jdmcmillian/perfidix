/*
 * Copyright 2009 Distributed Systems Group, University of Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $LastChangedRevision$
 * $LastChangedBy$
 * $LastChangedDate$
 *
 */
package org.perfidix.perclipseAdapter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * This class creates the connection to the eclipse view, which depict the
 * current progress of the operation. It contains also the methods to upudate
 * the view.
 * 
 * @author Lewandowski Lukas, University of Konstanz
 */
public class PerclipseViewStub implements IBenchRunSessionListener {
    private String host;
    private int viewListenerPort;
    private String command;
    private Socket socket;
    private ObjectOutputStream outputStream;

    /**
     * The constructor initializes the given host name and the port, which are
     * needed for creating a client socket. Afterwards it creates the client
     * socket and the object output stream.
     * 
     * @param host
     *            Host represents the {@link String} host name
     * @param viewListenerPort
     *            This param represents the port of the view.
     */
    public PerclipseViewStub(String host, int viewListenerPort) {
        if (host == null) {
            this.host = "localhost";
        } else {
            this.host = host;
        }
        this.viewListenerPort = viewListenerPort;
        try {
            socket = new Socket(this.host, this.viewListenerPort);
            outputStream =
                    new ObjectOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    public void initTotalBenchProgress(
            HashMap<String, Integer> benchElementsWithTotalBench) {
        command = "init";
        try {
            outputStream.writeObject(command);
            outputStream.writeObject(benchElementsWithTotalBench);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    public void updateCurrentRun(String currentElement) {
        command = "updateCurrentRun";
        try {
            outputStream.writeObject(command);
            outputStream.writeObject(currentElement);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    public void updateError(String element) {
        command = "updateError";
        try {
            outputStream.writeObject(command);
            outputStream.writeObject(element);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    public void finishedBenchRuns() {
        command = "finished";
        try {
            outputStream.writeObject(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
