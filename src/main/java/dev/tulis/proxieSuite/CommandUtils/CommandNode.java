package dev.tulis.proxieSuite.CommandUtils;

import java.util.HashMap;
import java.util.Map;

public class CommandNode {

    public Map<String, CommandNode> children = new HashMap<>();
    public String name;
    public String permission;
    public ParamType paramType;

    public static enum ParamType {
        PLAYERS,
        STATIC,
        DUMMY,
    }
}
