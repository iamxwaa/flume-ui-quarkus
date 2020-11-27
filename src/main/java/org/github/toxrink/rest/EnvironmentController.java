package org.github.toxrink.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.github.toxrink.config.EnvConfig;
import org.github.toxrink.utils.EnvUtils;

/**
 * EnvironmentController
 */
public class EnvironmentController {

    @GET
    @Path("/env/config")
    @Produces(MediaType.APPLICATION_JSON)
    public EnvConfig getConfig() {
        return EnvUtils.getEnvConfig();
    }
}