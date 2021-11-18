package com.eisgroup;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;


@Path("/jacoco")
public class RestApi {
    final String FILE_PATH = "data/jacoco-server.exec";
    @GET
    @Path("dump")
    @Produces({MediaType.TEXT_PLAIN})
    public String runDump() {
    App.Handler.resetID();
        try {
                App.Handler.captureDump(true, true);
            sleep(10000);
            String logs= App.Handler.getLogs();
            return logs;
            }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
         return "FAILED";
    }

    @GET
    @Path("logs")
    @Produces(MediaType.TEXT_PLAIN)
    public String showLogs() {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("nohup.out")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @GET
    @Path("/getDump")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
        File file = new File(FILE_PATH);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition", "attachment; filename=e2e-jacoco.exec");
        return response.build();

    }

}
