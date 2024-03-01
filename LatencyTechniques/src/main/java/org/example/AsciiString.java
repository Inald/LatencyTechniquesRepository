package org.example;

import java.util.Arrays;

public class AsciiString {

    byte[] data;

    public AsciiString(byte[] data) {
        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsciiString that = (AsciiString) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
