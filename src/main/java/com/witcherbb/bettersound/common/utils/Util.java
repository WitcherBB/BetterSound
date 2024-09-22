package com.witcherbb.bettersound.common.utils;

public class Util {
    public enum Status {
        NONE(0),
        FAIL(1),
        NULL(1),
        SUCCESS(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getvalue() {
            return this.value;
        }
    }
}
