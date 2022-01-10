package ModBusTester;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
    https://www.digitspace.com/modbus-rtu-4-channel-relay-module-12v-rs485
    set addres =01  0x00 0x06 0x40 0x00 0x00 0x01 0x5c 0x1b

    0x01 0x03 0x00 0x06 0x00 0x02

    open  relay   0x01 0x05 0x00 0x01 0xFF 0x00 0x9d 0x9a
    close relay  0x01 0x05 0x00 0x01 0x00 0x00 0x9c 0x0a
    read Inputs 0x01, 0x02, 0x00, 0x00, 0x00, 0x08

    0x01 0x02 0x00 0x00 0x00 0x08
    0x01 0x03 0x00 0x00 0x00 0x04
*/


public class Main {

    public static Com com;

    static {
        try {
            com = new Com();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public static Scanner s = new Scanner(System.in);

    public enum RelayStatus {Relay1, Relay2, Relay3, Relay4, outPutError, otherError, ok}

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";

    ;

    //main
    public static void main(String[] args) throws InterruptedException, TimeoutException {

        boolean quit = false;
        int choose;


        menuDisplay();
        while (!quit) {
            choose = s.nextInt();
            switch (choose) {

                case 0:
                    quit = true;
                    break;
                case 1:
                    menuDisplay();
                    break;

                case 2:
                    RelayOutputTest();
                    break;
                case 3:
                    RelayOutputAndInputTest();
                    break;

                default:
                    menuDisplay();

            }

        }

    }
//end main


    public static void menuDisplay() {
        System.out.println("********Test Options********* ");
        System.out.println("1-Show Menu");
        System.out.println("2-Relay Out Put Test");
        System.out.println("3-Input And Output Check");
        System.out.println("0- Quit MODBUSTESTER");

    }


    public static void RelayOutputTest() throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            com.relayShort((byte) i, 1000);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    
    //TODO  ask asaf how to handle this exception
    public static void RelayOutputAndInputTest() throws InterruptedException, TimeoutException {
        for (int i = 0; i < 4; i++) {



            com.getQueue().clear();
            byte[] setRelayCommand = com.setRelayState(false, (byte) i); // make sure the relay is off
            com.write(setRelayCommand,"We shouldn't use this");
            ModbusMessage message = listenToModbusResponse(); // response msg from salve that the coil if off
            System.out.println(message);
            ModbusMessage messageBeforeOutput = readInputs();
            System.out.println("Message before input: " + messageBeforeOutput);

            //Give pulse to relay number i
            com.relayShort((byte) i, 1000);
//            message = listenToModbusResponse();
            System.out.println("Message after write code: " + message);



            ModbusMessage messageAfterOutput = readInputs();
            System.out.println("Message after status code: " + messageAfterOutput);

            RelayStatus status_e = cheackInputsArrays(messageBeforeOutput, messageAfterOutput, i);
            if (status_e == RelayStatus.ok)
                System.out.println(ANSI_BLUE + "Realay and input number" + (i + 1) + "is valid" + ANSI_RESET);
            else if (status_e == RelayStatus.outPutError)
                System.out.println("Out Put Relay Error");
            else
                System.out.println(ANSI_RED + "Input Output Error in " + status_e.toString() + ANSI_RESET);
            System.out.println("*******************************************");


            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    /**
     * This function checks for corralation between the input state before the relay short, and the input state after the relay short
     * @param before: message from inputs before we changed the relay state.
     * @param after
     * @param i
     * @return
     */
    public static RelayStatus cheackInputsArrays(ModbusMessage before, ModbusMessage after, int i) {
        int relayIndex = i + 1;
        if ((before.getData()) != 0) {
            System.out.println("Relay Data befote Pulse wasn`t 00 ");
            return RelayStatus.outPutError;

        } else {

            switch (relayIndex) {
                case 1:

                    if (after.getData() == 8)
                        return RelayStatus.ok;
                    else
                        return RelayStatus.Relay1;


                case 2:
                    if (after.getData() == 4)
                        return RelayStatus.ok;
                    else
                        return RelayStatus.Relay2;


                case 3:
                    if (after.getData() == 2)
                        return RelayStatus.ok;
                    else
                        return RelayStatus.Relay3;


                case 4:
                    if (after.getData() == 1)
                        return RelayStatus.ok;
                    else
                        return RelayStatus.Relay4;
                default:
                    return RelayStatus.otherError;

            }
        }


    }

    public static String[] bytesToStr(Byte[] bytes, boolean isHex, int length) {
        String[] rs = new String[8];
        for (int i = 0; i < length; i++)
            rs[i] = (isHex ? String.format("%02X", bytes[i]) : String.valueOf(bytes[i]));
        System.out.println("rs=" + Arrays.toString(rs));
        //return rs.split("");
        return rs;
    }

    public static ModbusMessage readInputs() throws InterruptedException, TimeoutException {
        byte[] dataBytes = {0x01, 0x02, 0x00, 0x00, 0x00, 0x08};
        com.write(com.writeDataWithCRC(dataBytes), " Read Inputs Before Pulse ");
        ModbusMessage messageBeforeOutput = listenToModbusResponse();
        return messageBeforeOutput;

    }

    private static ModbusMessage listenToModbusResponse() throws InterruptedException, TimeoutException {
        MessageParser messageParser = new MessageParser();
        int status = 0;
        do {
            List<Byte> newBytes = com.getQueue().poll(50, TimeUnit.MILLISECONDS);
            if (newBytes != null) {
                status = messageParser.parseData(newBytes);
                //   System.out.println("waiting because not big enough");
            } else {
                status = -1;
                try {
                    throw new TimeoutException("No Response from modbus");
                } catch (TimeoutException e) {
                    continue;

                }

            }

            //System.out.println("waiting because null");
        } while (status != 0);

        ModbusMessage messageBeforeOutput = messageParser.getMessage();
        return messageBeforeOutput;
    }


}
