package com.winthier.minigames.util;

import com.winthier.minigames.game.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public class JsonBuilder {
    private final Object root;
    private final Stack<Object> stack = new Stack<>();

    private JsonBuilder(Object root) {
        this.root = root;
        stack.push(root);
    }

    public static JsonBuilder listBuilder() {
        return new JsonBuilder(new ArrayList<Object>());
    }

    public static JsonBuilder mapBuilder() {
        return new JsonBuilder(new HashMap<String, Object>());
    }

    @SuppressWarnings("unchecked")
    private List<Object> popList() {
        Object o = stack.peek();
        if (!(o instanceof List)) return null;
        return (List)o;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> popMap() {
        Object o = stack.peek();
        if (!(o instanceof Map)) return null;
        return (Map)o;
    }

    public JsonBuilder set(String key, Object value) {
        Map<String, Object> map = popMap();
        map.put(key, value);
        return this;
    }

    public JsonBuilder createMap(String key) {
        Map<String, Object> map = popMap();
        Map<String, Object> newMap = new HashMap<>();
        map.put(key, newMap);
        stack.push(newMap);
        return this;
    }

    public JsonBuilder createList(String key) {
        Map<String, Object> map = popMap();
        List<Object> newList = new ArrayList<>();
        map.put(key, newList);
        stack.push(newList);
        return this;
    }

    public JsonBuilder add(Object value) {
        List<Object> list = popList();
        list.add(value);
        return this;
    }

    public JsonBuilder addMap() {
        List<Object> list = popList();
        Map<String, Object> newMap = new HashMap<>();
        list.add(newMap);
        stack.push(newMap);
        return this;
    }

    public JsonBuilder addList() {
        List<Object> list = popList();
        List<Object> newList = new ArrayList<>();
        list.add(newList);
        stack.push(newList);
        return this;
    }

    public JsonBuilder done() {
        stack.pop();
        return this;
    }

    public Object getRoot() {
        return root;
    }

    public String toJsonString() {
        return JSONValue.toJSONString(getRoot());
    }

    public void announce(Game game) {
        game.announceRaw(getRoot());
    }

    public void send(Player player) {
        Msg.sendRaw(player, getRoot());
    }
}
