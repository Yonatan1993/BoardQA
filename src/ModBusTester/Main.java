package ModBusTester;

import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;


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


    public static ModbusHandler modbusHandler;
    public static TestManager testManager;


    static {
        try {
            modbusHandler = new ModbusHandler();
            testManager = new TestManager();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public static Scanner s = new Scanner(System.in);

//    public enum RelayStatus {Relay1, Relay2, Relay3, Relay4, outPutError, otherError, ok}
//
//    public static final String ANSI_RESET = "\u001B[0m";
//    public static final String ANSI_RED = "\u001B[31m";
//    public static final String ANSI_BLUE = "\u001B[34m";

    ;

    //main
    public static void main(String[] args) throws InterruptedException, TimeoutException, TooManyListenersException, CloneNotSupportedException {

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
                    testManager.relayOutputTest(modbusHandler);
                    break;
                case 3:
                    testManager.relayOutputAndInputTest(modbusHandler);
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

    //TODO  ask asaf how to handle this exception

//
//    public static String[] bytesToStr(Byte[] bytes, boolean isHex, int length) {
//        String[] rs = new String[8];
//        for (int i = 0; i < length; i++)
//            rs[i] = (isHex ? String.format("%02X", bytes[i]) : String.valueOf(bytes[i]));
//        System.out.println("rs=" + Arrays.toString(rs));
//        //return rs.split("");
//        return rs;
//    }




}
