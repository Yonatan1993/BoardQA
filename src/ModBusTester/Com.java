package ModBusTester;

import javax.comm.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Com implements SerialPortEventListener {

   public CommPortIdentifier portId;
    public SerialPort serialPort;
    public OutputStream outputStream;

    final String PORT = "COM4";
    final int BOUDRATE = 9600;
    final int capacityOfQueue = 16;

    BlockingQueue<List<Byte>> messageBuffer ;



    public Com() throws TooManyListenersException {

        this.messageBuffer = new LinkedBlockingQueue();
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

    public void write(byte[] output, String msg) {                      // this method prints out to the buffer.
        System.out.println(msg + " :  " + bytesToHexStr(output).toLowerCase());
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

    public static String bytesToHexStr(byte[] bytes) {
        return bytesToStr(bytes, true, bytes.length);
    }

    public static String bytesToStr(byte[] bytes, boolean isHex, int length) {
        String rs = "";
        for (int i = 0; i < length; i++)
            rs += (isHex ? String.format("%02X ", bytes[i]) : bytes[i]);

        return rs;
    }




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
