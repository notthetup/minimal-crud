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
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.MultipartConfigElement;

import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MinimalCrud
{
    public static void main( String[] args ) throws Exception
    {

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setResourceBase(".");
        server.setHandler(context);

        // Add default servlet
        ServletHolder crudServletHolder = new ServletHolder(new CrudServlet());
        crudServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
        context.addServlet(crudServletHolder, "/");

        // Start things up!
        server.start();
        server.join();
    }

    @SuppressWarnings("serial")
    public static class CrudServlet extends HttpServlet
    {
        private static final String CONTENT_DISPOSITION = "content-disposition";
        private static final String CONTENT_DISPOSITION_FILENAME = "filename";
        private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

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
            boolean multipart = request.getContentType().startsWith("multipart/form-data");

            System.out.println("POST at path : " + request.getServletPath() + "\n" +
                "- query string : " + request.getQueryString() + "\n" +
                "- Content-Type : " + request.getContentType());
            if (multipart){
                System.out.println("- Body : ");
                for (Part part : request.getParts()) {
                    String filename = getFilename(part);
                    if (filename == null) {
                        processTextPart(part);
                    } else if (!filename.isEmpty()) {
                        System.out.println("\t File part : " + filename);
                        processFilePart(part, filename);
                    }
                }
            }else{
                String body = request.getReader().lines().reduce("",String::concat);
                System.out.println("- Body : " + body);
            }
            System.out.println("\n");
            response.setStatus(HttpServletResponse.SC_OK);
        }


        @Override
        protected void doPut( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                IOException
        {
            boolean multipart = request.getContentType().startsWith("multipart/form-data");
            System.out.println("PUT at path : " + request.getServletPath()  + "\n" +
                "- query string : " + request.getQueryString() + "\n" +
                "- with Content-Type : " + request.getContentType());
            if (multipart){
                System.out.println("- Body : ");
                for (Part part : request.getParts()) {
                    String filename = getFilename(part);
                    if (filename == null) {
                        processTextPart(part);
                    } else if (!filename.isEmpty()) {
                        System.out.println("\t File part : " + filename);
                        processFilePart(part, filename);
                    }
                }
            }else{
                String body = request.getReader().lines().reduce("",String::concat);
                System.out.println("- Body : " + body);
            }
            System.out.println("\n");
            response.setStatus(HttpServletResponse.SC_OK);
        }

        @Override
        protected void doDelete( HttpServletRequest request,
                                 HttpServletResponse response ) throws ServletException,
                IOException
        {
            System.out.println("DELETE at path " + request.getServletPath() + "\n");
            response.setStatus(HttpServletResponse.SC_OK);
        }

        /**
         * Returns the text value of the given part.
         */
        private String getValue(Part part) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
            StringBuilder value = new StringBuilder();
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                value.append(buffer, 0, length);
            }
            return value.toString();
        }

        /**
         * Process given part as Text part.
         */
        private void processTextPart(Part part) throws IOException {
            String name = part.getName();
            System.out.println("\t Text Part : " + name + " : " + getValue(part));
        }

        private String getFilename(Part part) {
            for (String cd : part.getHeader(CONTENT_DISPOSITION).split(";")) {
                if (cd.trim().startsWith(CONTENT_DISPOSITION_FILENAME)) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
            return null;
        }

        private void processFilePart(Part part, String filename) throws IOException {
                // First fix stupid MSIE behaviour (it passes full client side path along filename).
                filename = filename
                    .substring(filename.lastIndexOf('/') + 1)
                    .substring(filename.lastIndexOf('\\') + 1);

                // Get filename prefix (actual name) and suffix (extension).
                String prefix = filename;
                String suffix = "";
                if (filename.contains(".")) {
                    prefix = filename.substring(0, filename.lastIndexOf('.'));
                    suffix = filename.substring(filename.lastIndexOf('.'));
                }

                // Write uploaded file.
                File file = new File ("upload/" + filename);
                InputStream input = null;
                OutputStream output = null;
                try {
                    input = new BufferedInputStream(part.getInputStream(), DEFAULT_BUFFER_SIZE);
                    output = new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE);
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    for (int length = 0; ((length = input.read(buffer)) > 0);) {
                        output.write(buffer, 0, length);
                    }
                } finally {
                    if (output != null) try { output.close(); } catch (IOException logOrIgnore) { /**/ }
                    if (input != null) try { input.close(); } catch (IOException logOrIgnore) { /**/ }
                }
                part.delete(); // Cleanup temporary storage.
            }
    }
}
