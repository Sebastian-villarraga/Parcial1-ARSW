package edu.eci.arsw.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * An implementation of the Bailey-Borwein-Plouffe formula for calculating
 * hexadecimal digits of pi.
 * https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
 * *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
 */
public class PiDigits {

    private static Scanner sc = new Scanner(System.in);

    /**
     * calcula los digitos de pi en un intervalo dado
     * @param start inicio del intervalo
     * @param count cantidad de digitos
     * @param N cantidad de threads
     * @return arreglo de bytes con los digitos de pi
     */
    public static byte[] getDigits(int start, int count, int N) {

        List<PiDigitsThread> threads = new ArrayList<>();
        Object lockObject = new Object();
        byte[] digits = new byte[count];
        int digitsPerThread = count / N;
        boolean anyThreadRunning = true;

        isValidInterval(start, count);
        createThreads(start, N, threads, digitsPerThread, lockObject);

        getResults(threads, digits, digitsPerThread);
        getTotalDigits(threads);
        return digits;
    }

    /**
     * detiene y ejecuta los threads
     * @param threads lista de threads
     * @param lockObject objeto de sincronizacion
     * @throws InterruptedException
     * @author Sebastian julian Villarraga Guerrrero
     *
     */
    private static void stopAndExecute(List<PiDigitsThread> threads, Object lockObject) throws InterruptedException {
        for (PiDigitsThread thread : threads) {
            thread.setExecution(false);
        }
        System.out.println("==================== Stopping threads... ====================");
        getTotalDigits(threads);
        System.out.println("Press ENTER to continue...");
        sc.nextLine();
        System.out.println("==================== Resuming threads... ====================");
        for (PiDigitsThread thread : threads) {
            thread.setExecution(true);
            synchronized (lockObject) {
                lockObject.notifyAll();
            }
        }
    }

    /**
     * imprime el total de digitos procesados
     * @param threads Lista de threads
     */
    private static void getTotalDigits(List<PiDigitsThread> threads) {
        int totalDigits = 0;
        for (PiDigitsThread thread : threads) {
            totalDigits += thread.getProcessedDigits();
        }
        System.out.println("Total of processed digits: " + totalDigits);
    }

    /**
     * ubtiene los resultados de los threads
     * @param threads lista de threads
     * @param digits arreglo de bytes
     * @param digitsPerThread cantidad de digitos por thread
     */
    private static void getResults(List<PiDigitsThread> threads, byte[] digits, int digitsPerThread) {
        for (PiDigitsThread thread : threads) {
            try {
                thread.join();
                getThreadDigits(digits, digitsPerThread, thread);
            } catch (InterruptedException ex) {
                System.out.println("Thread interrupted");
            }
        }
    }

    /**
     * crea los threads
     * @param start inicio del intervalo
     * @param N cantidad de threads
     * @param threads lista de threads
     * @param digitsPerThread cantidad de digitos por thread
     * @param lockObject objeto de sincronizacion
     */
    private static void createThreads(int start, int N, List<PiDigitsThread> threads, int digitsPerThread, Object lockObject) {
        for (int i = 0; i < N; i++) {
            PiDigitsThread thread = new PiDigitsThread(start, digitsPerThread, i, lockObject);
            start += digitsPerThread;
            threads.add(thread);
            thread.start();
        }
    }

    /**
     * obtiene los digitos de un thread
     * @param digits arreglo de bytes
     * @param digitsPerThread cantidad de digitos por thread
     * @param thread thread
     */
    private static void getThreadDigits(byte[] digits, int digitsPerThread, PiDigitsThread thread) {
        byte[] threadDigits = thread.getDigits();
        for (int i = 0; i < threadDigits.length; i++) {
            digits[i + thread.getThreadId() * digitsPerThread] = threadDigits[i];
        }
    }

    /**
     * valida el intervalo
     * @param start inicio del intervalo
     * @param count cantidad de digitos
     */
    private static void isValidInterval(int start, int count) {
        if (start < 0 || count < 0) {
            throw new RuntimeException("Invalid Interval");
        }
    }


}