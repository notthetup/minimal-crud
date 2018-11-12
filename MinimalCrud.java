//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.embedded;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MinimalCrud
{
    public static void main( String[] args ) throws Exception
    {
        // Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setResourceBase(System.getProperty("java.io.tmpdir"));
        server.setHandler(context);

        // Add default servlet
        context.addServlet(CrudServlet.class, "/");

        // Start things up!
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See
        // http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        server.join();
    }

    @SuppressWarnings("serial")
    public static class CrudServlet extends HttpServlet
    {
        @Override
        protected void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                IOException
        {
            System.out.println("GET at path : " + request.getServletPath() + "\n" +
                "- query string : " + request.getQueryString() + "\n");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from CrudServlet</h1>");
        }

        @Override
        protected void doPost( HttpServletRequest request,
                               HttpServletResponse response ) throws ServletException,
                IOException
        {
            String body = request.getReader().lines().reduce("",String::concat);
            System.out.println("POST at path : " + request.getServletPath() + "\n" +
                "- query string : " + request.getQueryString() + "\n" +
                "- Content-Type : " + request.getContentType() + "\n" +
                "- Body : " + body  + "\n");
            response.setStatus(HttpServletResponse.SC_OK);
        }


        @Override
        protected void doPut( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                IOException
        {
            String body = request.getReader().lines().reduce("",String::concat);
            System.out.println("PUT at path : " + request.getServletPath()  + "\n" +
                "- query string : " + request.getQueryString() + "\n" +
                "- with Content-Type : " + request.getContentType()  + "\n" +
                "- Body : " + body);
            response.setStatus(HttpServletResponse.SC_OK);
        }

        @Override
        protected void doDelete( HttpServletRequest request,
                                 HttpServletResponse response ) throws ServletException,
                IOException
        {
            System.out.println("DELETE at path " + request.getServletPath());
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
