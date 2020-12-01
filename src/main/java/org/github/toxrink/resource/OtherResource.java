package org.github.toxrink.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.github.toxrink.config.EnvConfig;
import org.github.toxrink.utils.EnvUtils;

@Path("")
public class OtherResource {
    @GET
    @Path("env/config")
    @Produces(MediaType.APPLICATION_JSON)
    public EnvConfig getConfig() {
        return EnvUtils.getEnvConfig();
    }
}
