/*
 * Made by Earl Kennedy
 * https://github.com/Mnenmenth
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EidolonAssistant {

    private static JFrame frame;
    private static DisplayPanel disp;
    public static boolean quit = true;
    public static Dimension MinDimension = new Dimension(200, 0);

    public static void main(String[] args) {

        frame = new JFrame();
        frame.setTitle("Eidolon Assistant v0.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit = true;
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(MinDimension);

        disp = new DisplayPanel();
        frame.add(disp);
        frame.pack();
        frame.setVisible(true);
    }

}