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

//    public ModbusMessage(List<Byte> data) {
//        Iterator<Byte> iter = data.iterator();
//
//        this.address = iter.next();
//        this.command = iter.next();
//        this.regidterAdress = 0;
//        int lengthOfAddress = 2;
//        for(int idx=0;idx<lengthOfAddress;idx++){
//            this.regidterAdress|=iter.next()<<(8*(lengthOfAddress-idx-1));
//        }
//        this.regidterAdress|=byte[0]<<8*(2-0-1);
//        this.regidterAdress|=byte[1];
//        this.data = ((iter.next()<<8)|iter.next());
//        this.crc = ((iter.next()<<8)|iter.next());
//    }


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
