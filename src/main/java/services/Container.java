package services;

import java.util.HashMap;

/**
 * Container to help out with obj. ref. problem
 */
public class Container {
    private static HashMap<String, Object> map;

    private static Container container = null;

    /**
     * Container constructor
     */
    private Container() {
        map = new HashMap<String, Object>();
    }

    /**
     * Singleton to get the instance of the container
     * #dirty and #notSOLID
     * @return Container
     */
    public static Container getInstance() {
        if (container == null) {
            container = new Container();
        }
        return container;
    }

    /**
     * Set anything into the container
     * @param key Strin
     * @param o Object
     */
    public void set(String key, Object o) {
        map.put(key.toLowerCase(), o);
    }

    /**
     * Get anything from the container
     * @param key String
     * @return Object
     */
    public Object get(String key) {
        return map.get(key.toLowerCase());
    }
}
