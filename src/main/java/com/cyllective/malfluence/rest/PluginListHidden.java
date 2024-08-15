package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Returns all currently hidden plugins for the JS listener
// GET /maintenance/gethiddenplugins
@Path("/gethiddenplugins")
public class PluginListHidden {

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response GetHiddenPlugins() {
        try {
            if (PluginHide.Enabled) {
                // Get the currently hidden plugins
                StringBuilder stringBuilder = new StringBuilder();
                for(String plugin: PluginHide.HiddenPlugins) {
                    stringBuilder.append(plugin).append("\n");
                }

                return Response
                        .status(Response.Status.OK)
                        .entity(stringBuilder.toString())
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