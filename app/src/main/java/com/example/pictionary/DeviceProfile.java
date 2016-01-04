package com.example.pictionary;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.nfc.Tag;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.UUID;

/**
 * Dave Smith
 * Date: 11/13/14
 * DeviceProfile
 * Service/Characteristic constant for our custom peripheral
 */
public class DeviceProfile {

    /* Unique ids generated for this device by 'uuidgen'. Doesn't conform to any SIG profile. */

    //Service UUID
    public static UUID SERVICE_UUID = UUID.fromString("1706BBC0-88AB-4B8D-877E-2237916EE929");

    //Read-only characteristic
    public static UUID CHARACTERISTIC_COORD_X_UUID = UUID.fromString("4929639e-2f03-4597-8c13-d543e1494e26");
    public static UUID CHARACTERISTIC_COORD_Y_UUID = UUID.fromString("275348FB-C14D-4FD5-B434-7C3F351DEA5F");

    public static UUID CHARACTERISTIC_XY_UUID = UUID.fromString("54bbddb9-2303-4a84-b2c3-59d6ac3260a7");


    public static UUID CHARACTERISTIC_ACTION_UUID = UUID.fromString("ddbe47d3-6881-465a-a0ce-5ba04a18f28c");
    //public static UUID CHARACTERISTIC_PEN_SIZE_UUID = UUID.fromString("ddbe47d3-6881-465a-a0ce-5ba04a18f28c");


    //Read-write characteristic for current offset timestamp
    public static UUID CHARACTERISTIC_WORD_UUID = UUID.fromString("BD28E457-4026-4270-A99F-F9BC20182E15");

    public static String getStateDescription(int state) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return "Unknown State "+state;
        }
    }

    public static String getStatusDescription(int status) {
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                return "SUCCESS";
            default:
                return "Unknown Status "+status;
        }
    }

    public static byte[] getShiftedTimeValue(int timeOffset) {
        int value = Math.max(0,
                (int)(System.currentTimeMillis()/1000) - timeOffset);
        return bytesFromInt(value);
    }

    public static int unsignedIntFromBytes(byte[] raw) {
        if (raw.length < 4) throw new IllegalArgumentException("Cannot convert raw data to int");

        return ((raw[0] & 0xFF)
                + ((raw[1] & 0xFF) << 8)
                + ((raw[2] & 0xFF) << 16)
                + ((raw[3] & 0xFF) << 24));
    }

    public static byte[] bytesFromInt(int value) {
        //Convert result into raw bytes. GATT APIs expect LE order
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    public static byte[] bytesFromInt(float value) {
        //Convert result into raw bytes. GATT APIs expect LE order
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(Math.round(value))
                .array();
    }

    public static byte[] bytesFromFloat(float value){
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(value)
                .array();
    }

    public static byte[] bytesFromArray(float[] array){
        ByteBuffer buf= ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(Math.round(array[0]));
        buf.putInt(Math.round(array[1]));

        return buf.array();
    }

    public static byte[] bytesFromArray(int [] array){
        ByteBuffer buf= ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(array[0]);
        buf.putInt(array[1]);

        return buf.array();
    }

    public static int [] arrayFromBytes(byte [] raw){
        if (raw.length < 8) throw new IllegalArgumentException("Cannot convert raw data to float array");

        int [] array = new int[2];
        array[0] = ((raw[0] & 0xFF)
                + ((raw[1] & 0xFF) << 8)
                + ((raw[2] & 0xFF) << 16)
                + ((raw[3] & 0xFF) << 24));
        array[1] = ((raw[4] & 0xFF)
                + ((raw[5] & 0xFF) << 8)
                + ((raw[6] & 0xFF) << 16)
                + ((raw[7] & 0xFF) << 24));


        return array;

    }

    public static byte[] bytesFromString(String str){
        //ByteBuffer buf = ByteBuffer.allocate(value.length()*2).order(ByteOrder.LITTLE_ENDIAN);
        char[] buffer = str.toCharArray();
        //allocate double length, as char is 2 bytes in java
        byte[] b = new byte[buffer.length<<1];
        CharBuffer cBuffer = ByteBuffer.wrap(b).asCharBuffer();
        for(int i = 0; i < buffer.length; i++)
            cBuffer.put(buffer[i]);
        return b;
    }

    public static String stringFromBytes(byte[] bytes) {
        if (bytes.length < 2) throw new IllegalArgumentException("Cannot convert raw data to string");
        CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
        return cBuffer.toString();

    }
}
