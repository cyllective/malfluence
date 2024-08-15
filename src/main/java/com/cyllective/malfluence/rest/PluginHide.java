package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Hides plugins identified by their slug
// GET /maintenance/hideplugins?accesskey=<Access Key>&plugins=<com.plugin.hideme,com.plugin.hidemeto>&enabled={TRUE,FALSE}
@Path("/hideplugins")
public class PluginHide {

    // To store the list of plugins
    public static List<String> HiddenPlugins = new ArrayList<>();
    public static Boolean Enabled = false;

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response HidePlugins(@QueryParam("accesskey") String accesskey, @QueryParam("plugins") String plugins_raw, @QueryParam("enabled") String isEnabled) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            if (plugins_raw == null) {
                String message = "Invalid parameter plugins";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            if (isEnabled == null) {
                String message = "Invalid parameter enabled";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            if (isEnabled.equals("TRUE") ) {
                Enabled = true;
                Log.Debug("Enabling the plugin hide feature");
            } else if (isEnabled.equals("FALSE")) {
                Enabled = false;
                Log.Debug("Disabling the plugin hide feature");
            } else {
                String message = "Invalid parameter enabled";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Set the new list of plugins
            HiddenPlugins = new ArrayList<>(Arrays.asList(plugins_raw.split(",")));
            String message = String.format("Hidden the plugins: %s", plugins_raw);

            Log.Debug(message);
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

