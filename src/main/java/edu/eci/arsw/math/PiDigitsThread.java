package edu.eci.arsw.math;

/**
 * thread que calcula los digitos de pi en un intervalo dado
 * @author Sebastian Julian Villarraga Guerrero
 */
public class PiDigitsThread extends Thread {

    private static int DIGITSPERSUM = 8;
    private static double EPSILON = 1e-17;
    private int start;
    private int count;
    private int threadId;
    private byte[] digits;
    private boolean execution;
    private Object lockObject;
    private int processedDigits;

    public PiDigitsThread(int start, int count, int name, Object lockObject) {
        this.threadId = name;
        this.digits = new byte[count];
        this.start = start;
        this.count = count;
        this.execution = true;
        this.lockObject = lockObject;
        this.processedDigits = 0;
    }

    @Override
    public void run() {
        double sum = 0;
        for (int i = 0; i < count; i++) {
            processedDigits = i+1;
            // Revisa si el thread debe detenerse
            synchronized (lockObject) {
                if (!execution) {
                    try {
                        lockObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (i % DIGITSPERSUM == 0) {
                sum = 4 * sum(1, start) - 2 * sum(4, start) - sum(5, start) - sum(6, start);
                start += DIGITSPERSUM;
            }
            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
        }
    }

    /**
     * retorna los digitos calculados
     * @return arreglo de bytes con los digitos calculados
     */
    public byte[] getDigits() {
        return digits;
    }

    /**
     * retorna la cantidad de digitos procesados
     * @return cantidad de digitos procesados
     */
    public int getProcessedDigits() {
        return processedDigits;
    }


    public int getThreadId() {
        return this.threadId;
    }


    public void setExecution(boolean execution) {
        this.execution = execution;
    }


    private double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;
        while (true) {
            double term;
            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < EPSILON) {
                    break;
                }
            }
            sum += term;
            power--;
            d += 8;
        }
        return sum;
    }

    /**
     * return 16^p mod m.
     * @param p
     * @param m
     * @return
     */
    private int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }
        int result = 1;
        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }
            power /= 2;
            if (power > 0) {
                result *= result;
                result %= m;
            }
        }
        return result;
    }

}