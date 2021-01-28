package org.github.toxrink.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * flume配置解析
 * 
 * @author xw
 * 
 *         2020年12月07日
 */
public class FlumeConfigUtils {

    public static class TouristPoint {
        public final List<Map<String, Object>> data;
        public final List<Map<String, Object>> link;

        public TouristPoint(List<Map<String, Object>> data, List<Map<String, Object>> link) {
            this.data = data;
            this.link = link;
        }
    }

    /**
     * 组件信息
     */
    private static Map<String, String> currentComponentMap;

    /**
     * 获取向导绘图节点
     * 
     * @param input        flume配置
     * @param componentMap 组件type对应名称
     * @return
     */
    public static TouristPoint getTouristPoint(String input, Map<String, String> componentMap) {
        currentComponentMap = componentMap;
        String agentName = getAgentName(input);
        String[] channels = getChannels(agentName, input);
        String[] sources = getSources(agentName, input);
        Map<String, String[]> interceptors = getInterceptors(agentName, sources, input);
        String[] sinks = getSinks(agentName, input);
        Map<String, Map<String, String>> channelProperties = getChannelProperties(agentName, channels, input);
        Map<String, Map<String, String>> sourceProperties = getSourceProperties(agentName, sources, input);
        Map<String, Map<String, String>> sinkProperties = getSinkProperties(agentName, sinks, input);
        Map<String, Map<String, Map<String, String>>> interceptorProperties = getInterceptorProperties(agentName,
                interceptors, input);
        List<Map<String, Object>> pointDataList = buildPointDataList(sourceProperties);
        pointDataList.addAll(buildPointDataList(channelProperties));
        pointDataList.addAll(buildPointDataList(sinkProperties));
        interceptorProperties.forEach((k, v) -> {
            pointDataList.addAll(buildPointDataList(v));
        });

        Map<String, Map<String, Object>> pointDataMap = pointDataList.stream().map(m -> {
            Map<String, Map<String, Object>> map = new TreeMap<>();
            map.put(m.get("nameIndex").toString(), m);
            return map;
        }).reduce((m1, m2) -> {
            m1.putAll(m2);
            return m1;
        }).get();

        Map<String, String[]> sourceChannelRelaction = getSourceChannelRelation(agentName, sources, input);
        Map<String, String> channelSinkRelaction = getSinkChannelRelation(agentName, sinks, input);
        List<Map<String, Object>> pointLinkList = buildPointLinkList(sourceChannelRelaction, channelSinkRelaction,
                interceptors, addEchartStyle(pointDataMap));
        return new TouristPoint(pointDataList, pointLinkList);
    }

    private static Map<String, Map<String, Object>> addEchartStyle(Map<String, Map<String, Object>> pointDataMap) {
        Map<String, Map<String, Object>> pointDataWithStyleMap = new TreeMap<>();
        pointDataWithStyleMap.putAll(pointDataMap);
        final Integer[] sourcePosition = new Integer[] { -80, 10 };
        final Integer[] channelPosition = new Integer[] { 0, 10 };
        final Integer[] sinkPosition = new Integer[] { 80, 10 };
        final Integer[] interceptorPosition = new Integer[] { -120, 0 };
        Map<String, String> rcolor = new HashMap<>();
        rcolor.put("color", "#FF5722");
        Map<String, String> ccolor = new HashMap<>();
        ccolor.put("color", "#5FB878");
        Map<String, String> kcolor = new HashMap<>();
        kcolor.put("color", "#01AAED");
        Map<String, String> icolor = new HashMap<>();
        icolor.put("color", "#000000");
        pointDataWithStyleMap.forEach((k, v) -> {
            String type = v.get("type").toString();
            switch (type) {
                case "r":
                    v.put("itemStyle", rcolor);
                    v.put("x", sourcePosition[0]);
                    v.put("y", sourcePosition[1]);
                    sourcePosition[1] += 20;
                    break;
                case "c":
                    v.put("itemStyle", ccolor);
                    v.put("x", channelPosition[0]);
                    v.put("y", channelPosition[1]);
                    channelPosition[1] += 20;
                    break;
                case "k":
                    v.put("itemStyle", kcolor);
                    v.put("x", sinkPosition[0]);
                    v.put("y", sinkPosition[1]);
                    sinkPosition[1] += 20;
                    break;
                default:
                    v.put("itemStyle", icolor);
                    v.put("x", interceptorPosition[0]);
                    v.put("y", interceptorPosition[1]);
                    interceptorPosition[1] += 20;
            }
        });
        return pointDataWithStyleMap;
    }

    /**
     * 获取agent名称
     * 
     * @param input flume配置
     * @return
     */
    public static String getAgentName(String input) {
        Pattern pattern = Pattern.compile("(.+)?\\.channels");
        Matcher mather = pattern.matcher(input);
        if (mather.find()) {
            String name = mather.group(1);
            int idx = name.lastIndexOf("#");
            if (-1 != idx) {
                return name.substring(idx + 1);
            }
            return name;
        }
        return null;
    }

    /**
     * 获取channel
     * 
     * @param agentName agent名称
     * @param input     flume配置
     * @return
     */
    public static String[] getChannels(String agentName, String input) {
        return matchComponentNames(input, "#?" + agentName + "\\.channels\\s*=\\s(.+)");
    }

    /**
     * 获取source
     * 
     * @param agentName agent名称
     * @param input     flume配置
     * @return
     */
    public static String[] getSources(String agentName, String input) {
        return matchComponentNames(input, "#?" + agentName + "\\.sources\\s*=\\s(.+)");
    }

    /**
     * 获取sink
     * 
     * @param agentName agent名称
     * @param input     flume配置
     * @return
     */
    public static String[] getSinks(String agentName, String input) {
        return matchComponentNames(input, "#?" + agentName + "\\.sinks\\s*=\\s(.+)");
    }

    private static String[] matchComponentNames(String input, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher mather = pattern.matcher(input);
        while (mather.find()) {
            String s = mather.group(0);
            if (s.charAt(0) == '#') {
                continue;
            }
            String[] cs = mather.group(1).split("#");
            cs = cs[0].split("\\s");
            for (int i = 0; i < cs.length; i++) {
                cs[i] = cs[i].trim();
            }
            Arrays.sort(cs);
            return cs;
        }
        return new String[0];
    }

    /**
     * 获取拦截器
     * 
     * @param agentName agent名称
     * @param sources   source名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, String[]> getInterceptors(String agentName, String[] sources, String input) {
        Map<String, String[]> map = new TreeMap<>();
        for (String source : sources) {
            String[] cs = matchComponentNames(input,
                    "#?" + agentName + "\\.sources\\." + source + "\\.interceptors\\s*=\\s(.+)");
            if (cs.length > 0) {
                map.put(source, cs);
            }
        }
        return map;
    }

    /**
     * 获取channel配置
     * 
     * @param agentName agent名称
     * @param keys      channel名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, Map<String, String>> getChannelProperties(String agentName, String[] keys, String input) {
        Map<String, Map<String, String>> map = new TreeMap<>();
        for (String key : keys) {
            Map<String, String> map2 = matchProperties(input, "#?" + agentName + "\\.channels\\." + key + "\\.(.+)");
            if (!map2.containsKey("type")) {
                continue;
            }
            map.put(key, map2);
        }
        return map;
    }

    /**
     * 获取source配置
     * 
     * @param agentName agent名称
     * @param keys      source名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, Map<String, String>> getSourceProperties(String agentName, String[] keys, String input) {
        Map<String, Map<String, String>> map = new TreeMap<>();
        for (String key : keys) {
            Map<String, String> map2 = matchProperties(input, "#?" + agentName + "\\.sources\\." + key + "\\.(.+)");
            if (!map2.containsKey("type")) {
                continue;
            }
            map.put(key, map2);
        }
        return map;
    }

    /**
     * 获取sink配置
     * 
     * @param agentName agent名称
     * @param keys      sink名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, Map<String, String>> getSinkProperties(String agentName, String[] keys, String input) {
        Map<String, Map<String, String>> map = new TreeMap<>();
        for (String key : keys) {
            Map<String, String> map2 = matchProperties(input, "#?" + agentName + "\\.sinks\\." + key + "\\.(.+)");
            if (!map2.containsKey("type")) {
                continue;
            }
            map.put(key, map2);
        }
        return map;
    }

    private static Map<String, String> matchProperties(String input, String regexp) {
        Map<String, String> map = new TreeMap<>();
        Pattern pattern = Pattern.compile(regexp);
        Matcher mather = pattern.matcher(input);
        while (mather.find()) {
            String s = mather.group(0);
            if (s.charAt(0) == '#') {
                continue;
            }
            String[] tmp = mather.group(1).split("=");
            if (tmp.length > 1) {
                map.put(tmp[0].trim(), tmp[1].trim());
            }
        }
        return map;
    }

    /**
     * 获取拦截器配置
     * 
     * @param agentName agent名称
     * @param keys      拦截器名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, Map<String, Map<String, String>>> getInterceptorProperties(String agentName,
            Map<String, String[]> keys, String input) {
        Map<String, Map<String, Map<String, String>>> map = new TreeMap<>();
        keys.forEach((k, v) -> {
            Map<String, Map<String, String>> map2 = new TreeMap<>();
            for (String key : v) {
                Map<String, String> map3 = matchProperties(input,
                        "#?" + agentName + "\\.sources\\." + k + "\\.interceptors\\." + key + "\\.(.+)");
                if (!map3.containsKey("type")) {
                    continue;
                }
                map2.put(key, map3);
            }
            map.put(k, map2);
        });
        return map;
    }

    /**
     * 获取source与channel关系
     * 
     * @param agentName agent名称
     * @param sources   source名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, String[]> getSourceChannelRelation(String agentName, String[] sources, String input) {
        Map<String, String[]> map = new TreeMap<>();
        for (String source : sources) {
            String[] cs = matchComponentNames(input,
                    "#?" + agentName + "\\.sources\\." + source + "\\.channels\\s*=\\s(.+)");
            if (cs.length > 0) {
                map.put(source, cs);
            }
        }
        return map;
    }

    /**
     * 获取sink和channel关系
     * 
     * @param agentName agent名称
     * @param sinks     sink名称
     * @param input     flume配置
     * @return
     */
    public static Map<String, String> getSinkChannelRelation(String agentName, String[] sinks, String input) {
        Map<String, String> map = new TreeMap<>();
        for (String sink : sinks) {
            String[] cs = matchComponentNames(input,
                    "#?" + agentName + "\\.sinks\\." + sink + "\\.channel\\s*=\\s(.+)");
            if (cs.length > 0) {
                map.put(sink, cs[0]);
            }
        }
        return map;
    }

    private static List<Map<String, Object>> buildPointDataList(Map<String, Map<String, String>> propertyMap) {
        List<Map<String, Object>> list = new ArrayList<>(propertyMap.size());
        propertyMap.forEach((k, v) -> {
            final String type = k.charAt(0) + "";
            final String pointDataIndex = k.substring(1);
            final String gtype = v.get("type");
            Map<String, Object> map = new HashMap<>();
            map.put("name", getComponentName(gtype, type) + "(" + k + ")");
            map.put("nameIndex", k);
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(v);
            parameters.remove("type");
            parameters.put("G_TYPE", gtype);
            map.put("parameters", parameters);
            map.put("pointDataIndex", Integer.parseInt(pointDataIndex));
            map.put("type", type);
            list.add(map);
        });
        return list;
    }

    private static List<Map<String, Object>> buildPointLinkList(Map<String, String[]> sourceChannelRelaction,
            Map<String, String> channelSinkRelaction, Map<String, String[]> interceptors,
            Map<String, Map<String, Object>> pointDataMap) {
        List<Map<String, Object>> list = new ArrayList<>();
        sourceChannelRelaction.forEach((source, channels) -> {
            for (String channel : channels) {
                if (!(pointDataMap.containsKey(source) && pointDataMap.containsKey(channel))) {
                    continue;
                }
                Map<String, Object> sourceMap = new HashMap<>();
                sourceMap.putAll(pointDataMap.get(source));
                sourceMap.put("source", sourceMap.get("name"));
                Map<String, Object> targetMap = new HashMap<>();
                targetMap.putAll(pointDataMap.get(channel));
                sourceMap.put("target", targetMap.get("name"));
                sourceMap.put("targetObject", targetMap);

                list.add(sourceMap);
            }
            String[] its = interceptors.get(source);
            if (null != its) {
                Map<String, Float> lineStyleMap = new HashMap<>();
                lineStyleMap.put("curveness", 0.2f);
                for (String it : its) {
                    if (!(pointDataMap.containsKey(source) && pointDataMap.containsKey(it))) {
                        continue;
                    }
                    Map<String, Object> sourceMap = new HashMap<>();
                    sourceMap.putAll(pointDataMap.get(source));
                    sourceMap.put("source", sourceMap.get("name"));
                    Map<String, Object> targetMap = new HashMap<>();
                    targetMap.putAll(pointDataMap.get(it));
                    sourceMap.put("target", targetMap.get("name"));
                    sourceMap.put("targetObject", targetMap);
                    sourceMap.put("lineStyle", lineStyleMap);

                    Map<String, Object> fakeMap = new HashMap<>();
                    fakeMap.put("source", targetMap.get("name"));
                    fakeMap.put("target", sourceMap.get("name"));
                    fakeMap.put("lineStyle", lineStyleMap);
                    fakeMap.put("fake", true);

                    list.add(sourceMap);
                    list.add(fakeMap);
                }
            }
        });
        channelSinkRelaction.forEach((sink, channel) -> {
            if (!(pointDataMap.containsKey(sink) && pointDataMap.containsKey(channel))) {
                return;
            }
            Map<String, Object> sourceMap = new HashMap<>();
            sourceMap.putAll(pointDataMap.get(sink));
            sourceMap.put("target", sourceMap.get("name"));
            Map<String, Object> targetMap = new HashMap<>();
            targetMap.putAll(pointDataMap.get(channel));
            sourceMap.put("source", targetMap.get("name"));
            sourceMap.put("sourceObject", targetMap);

            list.add(sourceMap);
        });
        return list;
    }

    private static String getComponentName(String gtype, String type) {
        if ("avro".equals(gtype)) {
            return "r".equals(type) ? "Avro Source" : "Avro Sink";
        }
        if ("i".equals(type)) {
            return currentComponentMap.get(gtype.replaceAll("\\$", ""));
        }
        return currentComponentMap.get(gtype);
    }
}
