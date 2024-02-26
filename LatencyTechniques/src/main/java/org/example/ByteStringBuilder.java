package org.example;

import static java.lang.System.arraycopy;

public class ByteStringBuilder {

    private byte[] nextline = new byte[256];
    private byte[] copyArray;
    private int index = 0;

    public void reset() {
        index = 0;
    }

    public void appendByte(byte b){
        nextline[index] = b;
        index++;
    }

    public byte[] getNextline() {
        return nextline;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getTrimmedNextLineArray() {
        copyArray = new byte[index];
        arraycopy(nextline,0,copyArray,0, index);
        return copyArray;
    }
}
