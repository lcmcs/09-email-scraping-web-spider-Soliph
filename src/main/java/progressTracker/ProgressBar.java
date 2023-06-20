package progressTracker;

public class ProgressBar extends Thread {

    private final int totalProgress;     // Total progress value
    private int currentProgress;         // Current progress value
    private final int progressBarWidth;  // Width of the progress bar

    public ProgressBar(int totalProgress, int progressBarWidth) {
        this.totalProgress = totalProgress;
        this.currentProgress = 0;
        this.progressBarWidth = progressBarWidth;
    }

    public void updateProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void completeProgress() {
        this.currentProgress = this.totalProgress + 1;
    }

    @Override
    public void run() {
        while (currentProgress <= totalProgress) {
            // Calculate the percentage complete
            int percentage = (currentProgress * 100) / totalProgress;

            // Calculate the number of completed blocks
            int completedBlocks = (currentProgress * progressBarWidth) / totalProgress;

            // Calculate the number of remaining blocks
            int remainingBlocks = progressBarWidth - completedBlocks;

            // Print the progress bar
            System.out.print("\r[");
            for (int i = 0; i < completedBlocks; i++) {
                System.out.print("#");  // Completed block character
            }
            for (int i = 0; i < remainingBlocks; i++) {
                System.out.print("-");  // Remaining block character
            }
            System.out.print("] " + percentage + "%");

            try {
                // Sleep for a short period to simulate progress
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
