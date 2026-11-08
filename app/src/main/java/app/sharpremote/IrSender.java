package app.sharpremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import app.sharpremote.ir.IrCode;

/** Sends IR codes through the phone's IR blaster on a background thread. */
public final class IrSender {

    private final ConsumerIrManager ir;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicInteger queued = new AtomicInteger();

    public IrSender(Context context) {
        ir = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
    }

    public boolean available() {
        try {
            return ir != null && ir.hasIrEmitter();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void send(IrCode code) {
        if (code == null || !available()) return;
        // Don't let held-down buttons pile up a backlog of signals.
        if (queued.get() > 1) return;
        final int frequency = code.frequency();
        final int[] pattern = code.pattern();
        queued.incrementAndGet();
        worker.execute(() -> {
            try {
                ir.transmit(frequency, pattern);
            } catch (RuntimeException ignored) {
                // Some phones throw if the blaster is busy; the next press will retry.
            } finally {
                queued.decrementAndGet();
            }
        });
    }

    public void shutdown() {
        worker.shutdownNow();
    }
}
