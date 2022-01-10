package ModBusTester;

import com.sun.xml.internal.bind.v2.TODO;

import javax.comm.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Com implements SerialPortEventListener {


    static CommPortIdentifier portId;
    static SerialPort serialPort;
    static OutputStream outputStream;
    final boolean isWithCRC = true;
    final String PORT = "COM5";
    final int BOUDRATE = 9600;
    final int capacityOfQueue = 16;


    public Com() throws TooManyListenersException {


        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (!portId.getName().equalsIgnoreCase(PORT))
                continue;
            try {

                serialPort = (SerialPort) portId.open("UARTApp", 2000);
                outputStream = serialPort.getOutputStream();
                serialPort.setSerialPortParams(BOUDRATE,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

            } catch (UnsupportedCommOperationException e) {
                e.printStackTrace();
                return;
            } catch (PortInUseException | IOException e) {
                e.printStackTrace();
                System.out.println("Port number " + portId.getName() + " not available");
            }

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            break;
        }


    }


    public void relayShort(byte relayNumber, int timeout) throws InterruptedException {
        byte[] status = setRelayState(true, (byte) relayNumber);
        write(status, " relay " + relayNumber + " ON ");
        List<Byte> poll = messageBuffer.poll(50, TimeUnit.MILLISECONDS); //TODO: WHY ASAF PUT HERE A POLL
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                byte[] status = setRelayState(false, (byte) relayNumber);
                write(status, " relay " + relayNumber + " OFF ");
                try {
                    List<Byte> poll = messageBuffer.poll(50, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, timeout);
    }

    public byte[] setRelayState(boolean power, byte relayNumber) {
        byte[] dataBytes = {0x01,
                0x05,
                0x00,
                relayNumber,
                power ? (byte) 0xFF : (byte) 0x00,
                //               power ? (byte) 0x01 : (byte) 0x00,
                (byte) 0x00};
        return isWithCRC ? writeDataWithCRC(dataBytes) : dataBytes;
    }

    public void write(byte[] output, String msg) {                      // this method prints out to the buffer.
        System.out.println(msg + " Send : " + bytesToHexStr(output).toLowerCase());
        try {
            outputStream.write(output);
            outputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            System.out.println("write method error");
        }
    }


    private static final Com.CRC16Modbus crc = new Com.CRC16Modbus();

    public  byte[] writeDataWithCRC(byte[] dataBytes) {
        crc.reset();
        crc.update(dataBytes, 0, dataBytes.length);
        byte[] crcBytes = new byte[2];
        crcBytes[0] = (byte) ((crc.getValue() & 0x000000ff));
        crcBytes[1] = (byte) ((crc.getValue() & 0x0000ff00) >>> 8);
        int dataBytesLen = dataBytes.length;
        int crcBytesLen = crcBytes.length;
        byte[] combinedData = new byte[dataBytes.length + crcBytes.length];
        System.arraycopy(dataBytes, 0, combinedData, 0, dataBytesLen);
        System.arraycopy(crcBytes, 0, combinedData, dataBytesLen, crcBytesLen);

        return combinedData;

    }

    static class CRC16Modbus {
        private final int[] TABLE = {
                0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241,
                0xc601, 0x06c0, 0x0780, 0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440,
                0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40,
                0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841,
                0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81, 0x1a40,
                0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41,
                0x1400, 0xd4c1, 0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641,
                0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040,
                0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240,
                0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0, 0x3480, 0xf441,
                0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41,
                0xfa01, 0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840,
                0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41,
                0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40,
                0xe401, 0x24c0, 0x2580, 0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640,
                0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041,
                0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240,
                0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480, 0xa441,
                0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41,
                0xaa01, 0x6ac0, 0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840,
                0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41,
                0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40,
                0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1, 0xb681, 0x7640,
                0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041,
                0x5000, 0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241,
                0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440,
                0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40,
                0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841,
                0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40,
                0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41,
                0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680, 0x8641,
                0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040
        };
        private int sum = 0xFFFF;

        public long getValue() {
            return sum;
        }

        public void reset() {
            sum = 0xFFFF;
        }

        public void update(byte[] b, int off, int len) {
            for (int i = off; i < off + len; i++)
                update(b[i]);
        }

        public void update(int b) {
            sum = (sum >> 8) ^ TABLE[((sum) ^ (b & 0xff)) & 0xff];
        }
    }


    public static String bytesToHexStr(byte[] bytes) {
        return bytesToStr(bytes, true, bytes.length);
    }

    public static String bytesToStr(byte[] bytes, boolean isHex, int length) {
        String rs = "";
        for (int i = 0; i < length; i++)
            rs += (isHex ? String.format("%02X ", bytes[i]) : bytes[i]);

        return rs;
    }


    BlockingQueue<List<Byte>> messageBuffer = new LinkedBlockingQueue();

    @Override
    public synchronized void serialEvent(SerialPortEvent event) {

        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                System.out.println("Got case : " + event.getEventType());
                break;

            case SerialPortEvent.DATA_AVAILABLE:

                try {
                    InputStream inputStream = serialPort.getInputStream();

                    if (inputStream != null) {
                        List<Byte> receivedList = new ArrayList<>();
                        do {
                            int read = inputStream.read();
                            receivedList.add((byte) read );
                        } while (inputStream.available() > 0);
                        messageBuffer.offer(receivedList);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break; // end of case
            default:
                throw new IllegalStateException("Unexpected value: " + event.getEventType());

        }
    }

    public BlockingQueue<List<Byte>> getQueue() {
        return messageBuffer;
    }
}
