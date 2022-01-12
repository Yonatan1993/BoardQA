package ModBusTester;

import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Testim {

    public enum RelayStatus {Relay1, Relay2, Relay3, Relay4, outPutError, otherError, ok}

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";


    public Testim(){}

        public  void RelayOutputTest(ModbusMessage modbusMessage) throws InterruptedException {
            for (int i = 0; i < 2; i++) {
                modbusMessage.relayShort((byte) i, 1000);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Com.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    public  void RelayOutputAndInputTest(ModbusMessage modbusMessage) throws InterruptedException, TimeoutException, TooManyListenersException {
        for (int i = 0; i < 4; i++) {


            modbusMessage.com.getQueue().clear();
            byte[] setRelayCommand = modbusMessage.setRelayState(false, (byte) i); // make sure the relay is off
            modbusMessage.write(setRelayCommand,"We shouldn't use this");
            ModbusMessage message = modbusMessage.listenToModbusResponse(); // response msg from salve that the coil if off, TODO:NEED TO CHECK THE RESPONSE
            System.out.println(message);
            ModbusMessage messageBeforeOutput = modbusMessage.readInputs();
            System.out.println("Message before input: " + messageBeforeOutput);

            //Give pulse to relay number i
            modbusMessage.relayShort((byte) i, 1000);

            System.out.println("Message after write code: " + message);

            ModbusMessage messageAfterOutput = modbusMessage.readInputs();
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


    public  RelayStatus cheackInputsArrays(ModbusMessage before, ModbusMessage after, int i) {
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

    }

