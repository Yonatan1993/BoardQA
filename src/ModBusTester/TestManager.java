package ModBusTester;

import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestManager {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";


    public TestManager() {
    }

    public void relayOutputTest(ModbusHandler modbusHandler) throws InterruptedException {//TODO: NEED TO ADD RESPONSE CHECKS
        for (int i = 0; i < 4; i++) {
            modbusHandler.relayShort((byte) i, 1000);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void relayOutputAndInputTest(ModbusHandler modbusHandler) throws InterruptedException, TimeoutException, TooManyListenersException{
        boolean input1State;
        for (int i = 0; i < 4; i++) {

            modbusHandler.cleanAllMessagesInQueue();
            switchRelayOff(modbusHandler,(byte) i);

            modbusHandler.cleanAllMessagesInQueue();
            switchRelayOn(modbusHandler,(byte) i);

//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
//            }

            int inputsState = modbusHandler.readInputs();
            input1State = (inputsState & (1 << (4-i))) == 0;
            if(!input1State)
                System.out.println(ANSI_BLUE + "Realay and input number " + (i + 1) + " is valid" + ANSI_RESET);
            else
                System.out.println(ANSI_RED + "Input Output Error in Relay number "+(i+1)  + ANSI_RESET);
            System.out.println("*******************************************");

            modbusHandler.setRelayState(false, (byte) i); //TODO: make sure the relay is off

//            ModbusHandler messageBeforeOutput = (ModbusHandler) modbusHandler.clone();
//            System.out.println("Message before input: " + messageBeforeOutput);
//
//            //Give pulse to relay number i
//            modbusHandler.relayShort((byte) i, 1000);
//
//            modbusHandler.readInputs();
//            ModbusHandler messageAfterOutput = (ModbusHandler) modbusHandler.clone();
//            System.out.println("Message after status code: " + messageAfterOutput);
//            RelayStatus status_e = cheackInputsArrays(messageBeforeOutput, messageAfterOutput, i);
//            if (status_e == RelayStatus.ok)
//                System.out.println(ANSI_BLUE + "Realay and input number" + (i + 1) + "is valid" + ANSI_RESET);
//            else if (status_e == RelayStatus.outPutError)
//                System.out.println("Out Put Relay Error");
//            else
//                System.out.println(ANSI_RED + "Input Output Error in " + status_e.toString() + ANSI_RESET);
//            System.out.println("*******************************************");



        }


    }

    private void switchRelayOn(ModbusHandler modbusHandler, byte i) throws TooManyListenersException, InterruptedException, TimeoutException {
        modbusHandler.setRelayState(true, (byte) i); //TODO: make sure the relay is off
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void switchRelayOff(ModbusHandler modbusHandler, byte i) throws TooManyListenersException, InterruptedException, TimeoutException {
        modbusHandler.setRelayState(false, (byte) i); //TODO: make sure the relay is off
        try {
               Thread.sleep(1000);
            } catch (InterruptedException ex) {
               Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
           }

    }


//    public RelayStatus cheackInputsArrays(ModbusHandler before, ModbusHandler after, int i) {
//        int relayIndex = i + 1;
//        if ((before.getData()) != 0) {
//            System.out.println("Relay Data befote Pulse wasn`t 00 ");
//            return RelayStatus.outPutError;
//
//        } else {
//
//            switch (relayIndex) {
//                case 1:
//
//                    if (after.getData() == 8)
//                        return RelayStatus.ok;
//                    else
//                        return RelayStatus.Relay1;
//
//
//                case 2:
//                    if (after.getData() == 4)
//                        return RelayStatus.ok;
//                    else
//                        return RelayStatus.Relay2;
//
//
//                case 3:
//                    if (after.getData() == 2)
//                        return RelayStatus.ok;
//                    else
//                        return RelayStatus.Relay3;
//
//
//                case 4:
//                    if (after.getData() == 1)
//                        return RelayStatus.ok;
//                    else
//                        return RelayStatus.Relay4;
//                default:
//                    return RelayStatus.otherError;
//
//            }
//        }


    //}

}

