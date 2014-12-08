package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.TitleManager;

import java.util.ArrayList;
import java.util.List;

public class StringAnimation {

    private int i;
    private String[] lines;
    private Object[] componentLines;

    public StringAnimation(List<String> lines) {
        setLines(lines);
    }

    public StringAnimation(String[] lines) {
        setLines(lines);
    }

    public Object nextAndGet() {
        Object componentLine = componentLines[i];
        i = componentLines.length - 1 == i ? 0 : i + 1;
        return componentLine;
    }

    public void setLines(String[] lines) {
        i = 0;
        this.lines = lines;
    }

    public void setLines(List<String> lines) {
        setLines(lines.toArray(new String[lines.size()]));
        List<Object> components = new ArrayList<>();
        for (String line : lines)
            components.add(TitleManager.getReflectionManager().getIChatBaseComponent(line));
        this.componentLines = components.toArray(new Object[components.size()]);
    }
}
