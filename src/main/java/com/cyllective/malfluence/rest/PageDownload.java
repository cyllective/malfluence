package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.confluence.pages.Page;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.spring.container.ContainerManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Returns the content of a specific page identified by its ID
// GET /maintenance/getpage?accesskey=<Access Key>&id=<Page ID>
@Path("/getpage")
public class PageDownload {
    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response GetPage(@QueryParam("accesskey") String accesskey, @QueryParam("id") String pageId) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }
            if (pageId == null) {
                String message = "Invalid parameter id";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            PageManager pageManager = (PageManager) ContainerManager.getComponent("pageManager");
            Page page = pageManager.getPage(new Long(pageId));

            if (page == null) {
                String message = "Invalid page id";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            Log.Debug(String.format("Downloading page '%s'", page.getDisplayTitle()));

            return Response
                    .status(Response.Status.OK)
                    .entity(page.getBodyContent().getBody())
                    .build();

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}