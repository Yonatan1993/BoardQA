package ModBusTester;

public class ModbusMessage {

    byte address;
    byte command;
    byte byteCount;
    //int registerAdress;
    int data = 0;
    int crc;


    public ModbusMessage() {}

    public byte getAddress() {
        return address;
    }

    public byte getCommand() {
        return command;
    }

    public byte getByteCount() {
        return byteCount;
    }

    public int getData() {
        return data;
    }


    public int getCrc() {
        return crc;
    }


    @Override
    public String toString() {
        return "ModbusMessage{" +
                "address=" + address +
                ", command=" + command +
                ", byteCount=" + byteCount +
                ", data=" + data +
                ", crc=" + crc +
                '}';
    }
}
