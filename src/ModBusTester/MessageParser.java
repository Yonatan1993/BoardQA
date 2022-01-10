package ModBusTester;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageParser {

    public List<Byte> data;
    final int dataLength = 16;
    private ModbusMessage modbusMessage;

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
    public int parseData(List<Byte> data) {

        this.data.addAll(data);
        if (data.size() < 6) {
            return -1;
        }
        finalParsing();
        return 0;
    }

    private void finalParsing() {
        Iterator<Byte> iter = data.iterator();
        modbusMessage = new ModbusMessage();
        modbusMessage.address = iter.next();
        modbusMessage.command = iter.next();
        modbusMessage.byteCount = iter.next();
        int lengthOfData = modbusMessage.byteCount;
        modbusMessage.data = 0;

        for (int idx = 0; idx < lengthOfData; idx++) {
            modbusMessage.data |= iter.next() << (8 * (lengthOfData - idx - 1));
        }
        modbusMessage.crc = ((iter.next() << 8) | iter.next());
    }


    public ModbusMessage getMessage() {
        return modbusMessage;
    }
}
