package ModBusTester;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;

public class MessageParser {

    public List<Byte> data;
    final int dataLength = 16;
//    private ModbusHandler modbusHandler;

    public MessageParser() {

        data = new ArrayList<>();
    }

    public Byte[] listToArray(List<Byte> lst) {
        Byte[] rezData = new Byte[dataLength];
        lst.toArray(rezData);
        return rezData;

    }


    public List<Byte> getData() {
        return data;
    }

    //TODO HERE I ASSUMING THAT THE LENGTH IS 6 bYTE. WHAT IF IT WILL BE LARGER?
    public int parseData(List<Byte> data) throws TooManyListenersException {

        this.data.addAll(data);
        if (data.size() < 6) {
            return -1;
        }
        finalParsing();
        return 0;
    }

    public class ModbusMessage {
        int address;
        int command;
        int byteCount;
        int dataLength;
        byte[] data;
        int crc;
    }

    ModbusMessage message;

    private void finalParsing() {
        ModbusMessage modbusMessage = new ModbusMessage();
        Iterator<Byte> iter = data.iterator();
        modbusMessage.address = iter.next();
        modbusMessage.command = iter.next();
        modbusMessage.byteCount = iter.next();
        int lengthOfData = modbusMessage.byteCount;
        modbusMessage.data = new byte[lengthOfData];

        for (int idx = 0; idx < lengthOfData; idx++) {
            modbusMessage.data[idx] = iter.next();
        }
        modbusMessage.crc = ((iter.next() << 8) | iter.next());
        this.message = modbusMessage;
    }


    public ModbusMessage getMessage() {
        return this.message;
    }
}
