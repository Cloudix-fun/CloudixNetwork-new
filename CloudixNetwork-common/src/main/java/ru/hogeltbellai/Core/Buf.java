package ru.hogeltbellai.Core;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.StandardCharsets;

public class Buf {
    private final IoBuffer buffer;

    public Buf(IoBuffer buffer) {
        this.buffer = buffer;
    }

    public static Buf create() {
        return new Buf(IoBuffer.allocate(128).setAutoExpand(true));
    }

    public byte read() {
        return this.buffer.get();
    }

    public void read(byte[] out) {
        this.buffer.get(out);
    }

    public int readInt() {
        return this.buffer.getInt();
    }

    public short readShort() {
        return this.buffer.getShort();
    }

    public long readLong() {
        return this.buffer.getLong();
    }

    public float readFloat() {
        return this.buffer.getFloat();
    }

    public double readDouble() {
        return this.buffer.getDouble();
    }

    public String readString() {
        byte[] bytes = new byte[readVarInt()];
        this.buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String readStringNullable() {
        return (this.buffer.get() == 40) ? readString() : null;
    }

    public byte[] readByteArray() {
        byte[] bytes = new byte[readVarInt()];
        this.buffer.get(bytes);
        return bytes;
    }

    public int readVarInt() {
        int result = 0;
        int shift = 0;
        byte b;
        do {
            b = this.buffer.get();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    public int readSignedVarInt() {
        int raw = readVarInt();
        int temp = (raw << 31 >> 31 ^ raw) >> 1;
        return temp ^ raw & Integer.MIN_VALUE;
    }

    public void write(byte val) {
        this.buffer.put(val);
    }

    public void write(byte[] val) {
        this.buffer.put(val);
    }

    public void writeInt(int val) {
        this.buffer.putInt(val);
    }

    public void writeShort(short val) {
        this.buffer.putShort(val);
    }

    public void writeLong(long val) {
        this.buffer.putLong(val);
    }

    public void writeFloat(float val) {
        this.buffer.putFloat(val);
    }

    public void writeDouble(double val) {
        this.buffer.putDouble(val);
    }

    public void writeString(String val) {
        byte[] bytes = val.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        this.buffer.put(bytes);
    }

    public void writeStringNullable(String val) {
        if (val != null) {
            this.buffer.put((byte) 40);
            writeString(val);
        } else {
            this.buffer.put((byte) 11);
        }
    }

    public void writeByteArray(byte[] arr) {
        writeByteArray(arr, arr.length);
    }

    public void writeByteArray(byte[] arr, int length) {
        writeVarInt(length);
        this.buffer.put(arr, 0, length);
    }

    public void writeVarInt(int val) {
        while (true) {
            int bits = val & 0x7F;
            val >>>= 7;
            if (val == 0) {
                this.buffer.put((byte) bits);
                return;
            }
            this.buffer.put((byte) (bits | 0x80));
        }
    }

    public void writeSignedVarInt(int val) {
        writeVarInt(val << 1 ^ val >> 31);
    }

    public IoBuffer getBuffer() {
        return this.buffer;
    }

    public int readableBytes() {
        return this.buffer.remaining();
    }
}