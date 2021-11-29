package com.glia.widgets.helper;

import java.util.List;

public class ListUtil {
    public static <T> T getLast(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }
}
