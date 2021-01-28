package org.github.iamxwaa.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.github.iamxwaa.config.EnvConfig;
import org.github.iamxwaa.utils.EnvUtils;

@Path("")
public class OtherResource {
    @GET
    @Path("env/config")
    @Produces(MediaType.APPLICATION_JSON)
    public EnvConfig getConfig() {
        return EnvUtils.getEnvConfig();
    }
}
