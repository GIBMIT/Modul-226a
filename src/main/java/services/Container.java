package services;

import java.util.HashMap;

public class Container {
    private static HashMap<String, Object> map;

    private static Container container = null;

    private Container() {
        map = new HashMap<String, Object>();
    }

    public static Container getInstance() {
        if (container == null) {
            container = new Container();
        }
        return container;
    }

    public void set(String key, Object o) {
        map.put(key.toLowerCase(), o);
    }

    public Object get(String key) {
        return map.get(key.toLowerCase());
    }
}
