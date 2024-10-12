package com.witcherbb.bettersound.music.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BinaryFileReader {
    private final FileInputStream fis;
    private final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

    public BinaryFileReader(String path) throws FileNotFoundException {
        this.fis = new FileInputStream(path);
    }

    public BinaryFileReader(File file) throws FileNotFoundException {
        this.fis = new FileInputStream(file);
    }

    public short readShort() throws IOException {
        buffer.clear().put(readBytes(Short.BYTES));
        return buffer.rewind().getShort();
    }

    public byte readByte() throws IOException {
        return (byte) this.fis.read();
    }

    public byte[] readBytes(int len) throws IOException {
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    public String readString() throws IOException {
        return new String(readBytes(readInt()), StandardCharsets.UTF_8);
    }

    public int readInt() throws IOException {
        buffer.clear().put(readBytes(Integer.BYTES));
        return buffer.rewind().getInt();
    }
}
