package org.github.toxrink.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import lombok.Data;

@Data
public class MultipartBody {
    private static final Log LOG = LogFactory.getLog(MultipartBody.class);

    @FormParam("inputfile")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    private InputStream file;

    @HeaderParam("Content-Disposition")
    @PartType(MediaType.TEXT_PLAIN)
    private String fileName;

    public MultipartBody() {
    }

    public MultipartBody(InputPart inputPart) {
        try {
            file = inputPart.getBody(InputStream.class, null);
        } catch (IOException e) {
            LOG.error("", e);
        }
        fileName = inputPart.getHeaders().getFirst("Content-Disposition");
        Pattern pattern = Pattern.compile("filename=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            fileName = matcher.group(1);
        }
    }
}