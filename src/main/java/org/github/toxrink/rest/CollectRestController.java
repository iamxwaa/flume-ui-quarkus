package org.github.toxrink.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.github.toxrink.metric.JMXMetricUtils;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

/**
 * CollectController
 */
public class CollectRestController {

    @GET
    @Path("/collect/metric/{cid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMetricInfo(@PathParam String cid) {
        return JMXMetricUtils.getMetricJSON(cid);
    }
}