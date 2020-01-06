package util;

import controller.NotesDataManager;
import java.util.function.Supplier;

public class IntervalRecorder extends Thread {
    private static final long DELAY_MILLIS = 300L;

    private final NotesDataManager notesDataManager;
    private final Supplier<String> textSupplier;

    public IntervalRecorder(Supplier<String> textSupplier, NotesDataManager notesDataManager) {
        this.textSupplier = textSupplier;
        this.notesDataManager = notesDataManager;
    }

    @Override
    public void run() {
        String recentText = "";
        while (true) {
            _wait();

            if (!recentText.equals(textSupplier.get())) {
                recentText = textSupplier.get();
                notesDataManager.updateEverywhere(recentText.replace("\r\n", "\n"));
            }
        }
    }

    private void _wait() {
        try {
            Thread.sleep(IntervalRecorder.DELAY_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
