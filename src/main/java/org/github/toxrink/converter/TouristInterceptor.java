package org.github.toxrink.converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                List<String[]> list = new ArrayList<>();
                context.iterateValues().forEachRemaining(a -> {
                    if (a.getName().startsWith(filter)) {
                        list.add(new String[] { a.getName().substring(filter.length() + 1), a.getValue() });
                    }
                });
                list.sort((c1, c2) -> {
                    String a = c1[0];
                    String b = c2[0];
                    if (a.startsWith("vap")) {
                        if (b.startsWith("vap")) {
                            return a.compareTo(b);
                        }
                        return -1;
                    } else if (b.startsWith("vap")) {
                        return 1;
                    } else {
                        return a.compareTo(b);
                    }
                });
                Map<String, String> map = new LinkedHashMap<>();
                list.forEach(s -> {
                    map.put(s[0], s[1]);
                });
                String value = new Gson().toJson(map);
                configValue = new ConfigValueBuilder().withName(name).withValue(value).build();
            }
        }
        return configValue;
    }

}
