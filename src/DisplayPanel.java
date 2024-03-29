/*
 * Made by Earl Kennedy
 * https://github.com/Mnenmenth
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;

public class DisplayPanel extends JPanel {

    private static JLabel status;
    private JTextField keyCodeInput;
    private static JTextField timeInput;
    public static int keyCode;
    private static long timeLeft;
    private JButton startStop;
    private static JLabel timerDisplay;

    private static Timer timer;
    private Thread t;

    private final String STATUS_STOPPED = "<html>Key Listener Status: <font color='red'>STOPPED</font></html>";
    private final String STATUS_RUNNING = "<html>Key Listener Status: <font color='green'>RUNNING</font></html>";

    public DisplayPanel() {

        t = new Thread(new HookRunnable());

        setMinimumSize(EidolonAssistant.MinDimension);

        // parent layout
        BorderLayout parentLayout;
        parentLayout = new BorderLayout();
        setLayout(parentLayout);

        // north layout
        JPanel north = new JPanel();
        BorderLayout northLayout = new BorderLayout();
        north.setLayout(northLayout);

        // center layout
        JPanel center = new JPanel();
        GridBagLayout centerLayout;
        GridBagConstraints centerLayoutConstraints;
        centerLayout = new GridBagLayout();
        centerLayoutConstraints = new GridBagConstraints();
        center.setLayout(centerLayout);

        // south layout
        JPanel south = new JPanel();
        BorderLayout southLayout = new BorderLayout();
        south.setLayout(southLayout);

        // NORTH
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        status = new JLabel(STATUS_STOPPED);
        status.setFont(f);
        status.setHorizontalAlignment(JLabel.CENTER);
        north.add(status, BorderLayout.CENTER);

        // CENTER
        centerLayoutConstraints.insets = new Insets(4, 8, 4, 2);
        centerLayoutConstraints.gridx = 0;
        centerLayoutConstraints.gridy++;
        center.add(new JLabel("Key Code: "), centerLayoutConstraints);

        centerLayoutConstraints.insets = new Insets(8, 0, 4, 4);
        centerLayoutConstraints.gridx++;
        keyCodeInput = new JTextField("51",  4);
        center.add(keyCodeInput, centerLayoutConstraints);

        centerLayoutConstraints.insets = new Insets(8, 4, 4, 2);
        centerLayoutConstraints.gridx++;
        center.add(new JLabel("Ability Time (In Seconds): "), centerLayoutConstraints);

        centerLayoutConstraints.insets = new Insets(8, 2, 4, 8);
        centerLayoutConstraints.gridx++;
        timeInput = new JTextField("30.00", 4);
        center.add(timeInput, centerLayoutConstraints);

        centerLayoutConstraints.insets = new Insets(0, 8, 4, 8);
        centerLayoutConstraints.gridwidth = centerLayoutConstraints.gridx+1;
        centerLayoutConstraints.gridx = 0;
        centerLayoutConstraints.gridy++;
        centerLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        startStop = new JButton("Start/Stop Keyboard Listener");
        startStop.addActionListener(this::startStopCallback);
        center.add(startStop, centerLayoutConstraints);

        // SOUTH
        timerDisplay = new JLabel("00.00s Left on Ability");
        timerDisplay.setHorizontalAlignment(JLabel.CENTER);
        south.add(timerDisplay);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

    }

    private void startStopCallback(ActionEvent e) {
        // parse key code input field
        keyCode = Integer.parseInt(keyCodeInput.getText());
        // stop timer if its already running
        if(timer != null) {
            if(timer.isRunning()) timer.stop();
        }
        // If key listener isn't running
        if(EidolonAssistant.quit) {
            // start key listener
            t = new Thread(new HookRunnable());
            EidolonAssistant.quit = false;
            t.start();
            status.setText(STATUS_RUNNING);
        // if key listener is running
        } else {
            // stop key listener
            EidolonAssistant.quit = true;
            status.setText(STATUS_STOPPED);
            try {
                // there's no graceful way to stop the thread with how HookRunnable is currently written.
                // the last while loop never actually is able to finish so thread join isn't possible
                t.interrupt();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void startCountdown() {
        // parse ability time
        timeLeft = (long)(Double.parseDouble(timeInput.getText()) * 1000);
        // stop timer if already running
        if(timer != null) {
            if(timer.isRunning()) timer.stop();
        }
        // recreate timer
        timer = new Timer(10, (ActionEvent e) -> {
            // run code in swing invoke later since this is called from another thread
            SwingUtilities.invokeLater(() -> {
                // format miliseconds to seconds
                SimpleDateFormat df = new SimpleDateFormat("ss.SS");
                timerDisplay.setText(df.format(timeLeft -= 10) + "s Left on Ability");
                // if less than 10 seconds of timer left, make text red
                if(timeLeft <= 10*1000) {
                    timerDisplay.setForeground(Color.RED);
                }
                // reset displays if timer is done
                if(EidolonAssistant.quit || timeLeft <= 0) {
                    timerDisplay.setText("00.00s Left on Ability");
                    timerDisplay.setForeground(Color.BLACK);
                }
            });
            // stop timer if ability time is done
            if(timeLeft <= 0) timer.stop();
        });
        // start timer
        timer.setInitialDelay(0);
        timer.start();

    }
}
