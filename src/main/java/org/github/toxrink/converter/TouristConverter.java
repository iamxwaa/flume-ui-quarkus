package org.github.toxrink.converter;

import java.util.Map;

import com.google.gson.Gson;

import org.eclipse.microprofile.config.spi.Converter;

public class TouristConverter implements Converter<Map> {
    /**
     *
     */
    private static final long serialVersionUID = -3929063783703581106L;

    @Override
    public Map convert(String value) {
        return new Gson().fromJson(value, Map.class);
    }

}
