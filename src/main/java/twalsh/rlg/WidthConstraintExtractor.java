package twalsh.rlg;

/**
 * Created by thomaswalsh on 09/10/2015.
 */
public class WidthConstraintExtractor implements Runnable {

    @Override
    public void run() {
        System.out.println("I am a running thread");
    }

    public static void main(String[] args) {
        WidthConstraintExtractor wce = new WidthConstraintExtractor();
        Thread t = new Thread(wce);
        t.start();
    }
}
