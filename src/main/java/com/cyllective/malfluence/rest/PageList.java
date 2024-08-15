package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.spaces.*;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.spring.container.ContainerManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Returns all pages along their IDs and spaces as CSV
// GET /maintenance/listpages?accesskey=<Access Key>
@Path("/listpages")
public class PageList {
    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response ListPages(@QueryParam("accesskey") String accesskey) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SPACE;PAGEID;PAGENAME\n");

            // To iterate through all spaces and mages we need managers
            SpaceManager spaceManager = (SpaceManager) ContainerManager.getComponent("spaceManager");
            PageManager pageManager = (PageManager) ContainerManager.getComponent("pageManager");

            /// Limit results to the first 3 pages of the first 3 spaces to avoid a DoS
            spaceManager.getAllSpaces()
                    .stream()
                    .limit(3)
                    .forEach(space -> {
                        pageManager.getPages(space, true)
                                .stream()
                                .limit(3)
                                .forEach(page -> {
                                    stringBuilder.append(String.format("%s;%s;%s\n",
                                            space.getName(),
                                            page.getId(),
                                            page.getDisplayTitle()
                                    ));
                                });
                    });

            /*
            // Go through all spaces and pages
            for (Space space : spaceManager.getAllSpaces() ) {
                for (Page page : pageManager.getPages(space, true)) {
                    stringBuilder.append(String.format("%s;%s;%s\n",
                            space.getName(),
                            page.getId(),
                            page.getDisplayTitle()
                    ));
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