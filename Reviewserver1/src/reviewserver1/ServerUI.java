
package reviewserver1;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

public class ServerUI extends JFrame {
    private JTextArea logArea;

    public ServerUI() {
        setTitle("Movie Review Server");
        setSize(400, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logArea);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerUI serverUI = new ServerUI();
            serverUI.setVisible(true);

            // Example usage
            serverUI.appendLog("Server started...");
            serverUI.appendLog("Waiting for incoming connections...");
        });
    }
}
