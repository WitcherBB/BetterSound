package com.witcherbb.bettersound.custom.registries;

import com.witcherbb.bettersound.custom.RecordItemSupporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RecordItemRegistry {
    private List<RecordItemSupporter> recordItemSupporters = new ArrayList<>();

    public void register(RecordItemSupporter supporter) {
        recordItemSupporters.add(supporter);
    }

    public void freeze() {
        recordItemSupporters = List.copyOf(recordItemSupporters);
    }

    public List<RecordItemSupporter> all() {
        return recordItemSupporters;
    }

    public void forEach(Consumer<RecordItemSupporter> consumer) {
        Objects.requireNonNull(consumer);

        for (RecordItemSupporter supporter : recordItemSupporters) {
            consumer.accept(supporter);
        }
    }
}
