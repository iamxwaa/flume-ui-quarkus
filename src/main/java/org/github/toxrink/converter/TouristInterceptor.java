package org.github.toxrink.converter;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Priority;

import com.google.gson.Gson;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.ConfigValue.ConfigValueBuilder;
import io.smallrye.config.Priorities;

@Priority(Priorities.APPLICATION + 100)
public class TouristInterceptor implements ConfigSourceInterceptor {

    /**
     *
     */
    private static final long serialVersionUID = 7312753861153008292L;

    public static final String TOURIST = "tourist";
    public static final String TOURIST_SOURCE = "tourist.source-map";
    public static final String TOURIST_CHANNEL = "tourist.channel-map";
    public static final String TOURIST_SINK = "tourist.sink-map";
    public static final String TOURIST_INTERCEPTOR = "tourist.interceptor-map";

    @Override
    public ConfigValue getValue(ConfigSourceInterceptorContext context, String name) {
        ConfigValue configValue = context.proceed(name);
        if (null == configValue) {
            String filter;
            switch (name) {
                case TOURIST_SOURCE:
                case TOURIST_CHANNEL:
                case TOURIST_SINK:
                case TOURIST_INTERCEPTOR:
                    filter = name;
                    break;
                default:
                    filter = null;

            }
            if (null != filter) {
                Map<String, String> map = new TreeMap<>();
                context.iterateValues().forEachRemaining(a -> {
                    if (a.getName().startsWith(filter)) {
                        map.put(a.getName().substring(filter.length() + 1), a.getValue());
                    }
                });
                String value = new Gson().toJson(map);
                configValue = new ConfigValueBuilder().withName(name).withValue(value).build();
            }
        }
        return configValue;
    }

}
