import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaPowerShowcase {

    // ANSI Escape Codes for UI
    private static final String RESET = "\033[0m";
    private static final String CLEAR_SCREEN = "\033[2J";
    private static final String CURSOR_HOME = "\033[H";
    private static final String BOLD = "\033[1m";
    private static final String GREEN = "\033[32m";
    private static final String CYAN = "\033[36m";
    private static final String YELLOW = "\033[33m";
    private static final String RED = "\033[31m";
    private static final String MAGENTA = "\033[35m";

    // System State
    private static final ConcurrentLinkedQueue<String> notificationLog = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger completedTasks = new AtomicInteger(0);
    private static final AtomicInteger activeThreads = new AtomicInteger(0);
    private static final Queue<String> taskQueue = new LinkedList<>();
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.print(CLEAR_SCREEN);

        // Pre-load dummy tasks
        String[] tasks = {"Encrypting Payload", "Compressing Logs", "Syncing Database", "Analyzing Metrics", "Purging Cache", "Verifying Checksums"};
        for (int i = 0; i < 15; i++) {
            taskQueue.add(tasks[i % tasks.length] + " #" + (i + 1));
        }

        // Thread 1: The Engine (Processes tasks asynchronously)
        Thread engineThread = new Thread(() -> {
            Random rand = new Random();
            while (isRunning && !taskQueue.isEmpty()) {
                activeThreads.incrementAndGet();
                String currentTask = taskQueue.poll();
                
                try {
                    // Simulate heavy workload
                    Thread.sleep(500 + rand.nextInt(1500)); 
                    
                    // Simulate random failure
                    if (rand.nextInt(10) > 8) {
                        pushNotification(RED + "⚠ FAILED: " + RESET + currentTask);
                    } else {
                        pushNotification(GREEN + "✔ SUCCESS: " + RESET + currentTask);
                        completedTasks.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    activeThreads.decrementAndGet();
                }
            }
        });

        // Thread 2: The UI Renderer (Updates the console at 10 frames per second)
        ScheduledExecutorService renderer = Executors.newSingleThreadScheduledExecutor();
        renderer.scheduleAtFixedRate(JavaPowerShowcase::renderDashboard, 0, 100, TimeUnit.MILLISECONDS);

        // Start processing
        pushNotification(CYAN + "⚙ SYSTEM BOOT: " + RESET + "Initializing concurrent workers...");
        engineThread.start();

        // Wait for engine to finish
        try {
            engineThread.join();
            Thread.sleep(1000); // Let UI catch up
            isRunning = false;
            renderer.shutdown();
            renderDashboard(); // Final render
            System.out.println("\n" + BOLD + GREEN + "All operations completed successfully. Shutting down." + RESET);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void pushNotification(String message) {
        if (notificationLog.size() >= 5) {
            notificationLog.poll(); // Keep only last 5 notifications
        }
        notificationLog.add(message);
    }

    private static void renderDashboard() {
        StringBuilder ui = new StringBuilder();
        ui.append(CURSOR_HOME); // Move cursor to top-left instead of clearing to prevent flickering

        // 1. Header
        ui.append(MAGENTA).append(BOLD)
          .append("====================================================\n")
          .append("             JAVA CONCURRENCY SHOWCASE              \n")
          .append("====================================================\n").append(RESET);

        // 2. Hardware & Memory Metrics (Real JVM Data)
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        int memPercentage = (int) (((double) usedMemory / totalMemory) * 100);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ui.append(BOLD).append("SYSTEM SPECS:\n").append(RESET);
        ui.append(" OS: ").append(osBean.getName()).append(" ").append(osBean.getVersion()).append("\n");
        ui.append(" Arch: ").append(osBean.getArch()).append(" | Cores: ").append(osBean.getAvailableProcessors()).append("\n");
        
        ui.append(" RAM Usage: [");
        int bars = memPercentage / 5;
        for (int i = 0; i < 20; i++) {
            if (i < bars) ui.append(YELLOW).append("█").append(RESET);
            else ui.append("░");
        }
        ui.append("] ").append(usedMemory).append("MB / ").append(totalMemory).append("MB\n\n");

        // 3. Task Processing Stats
        ui.append(BOLD).append("WORKER STATUS:\n").append(RESET);
        ui.append(" Active Threads: ").append(CYAN).append(activeThreads.get()).append(RESET).append("\n");
        ui.append(" Tasks Remaining: ").append(taskQueue.size()).append("\n");
        ui.append(" Completed: ").append(GREEN).append(completedTasks.get()).append(RESET).append("\n\n");

        // 4. Live Notification Feed
        ui.append(BOLD).append("LIVE EVENT LOG:\n").append(RESET);
        ui.append("----------------------------------------------------\n");
        String[] logs = notificationLog.toArray(new String[0]);
        for (int i = 0; i < 5; i++) {
            if (i < logs.length) {
                ui.append(" > ").append(logs[i]).append("\n");
            } else {
                ui.append("\n"); // Empty line padding
            }
        }
        ui.append("----------------------------------------------------\n");

        // Print everything to the console in one atomic operation
        System.out.print(ui.toString());
    }
}
