package net.runelite.client.plugins.deecat;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import net.runelite.api.Client;
import net.runelite.api.Player;


class Gui extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Client client;
	/*private JTextField txtPlayerUsername;
	private final Action action = new SwingAction();
	private JButton btnBan;*/

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {}
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    Gui frame = new Gui();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Gui() {
        setTitle("TunScape's Server Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 611, 361);
        contentPane = new JPanel();
        contentPane.setBorder(new MatteBorder(2, 2, 2, 2, (Color) new Color(0, 0, 0)));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setBackground(Color.CYAN);
        progressBar.setToolTipText("");
        progressBar.setOrientation(SwingConstants.VERTICAL);
        progressBar.setIndeterminate(true);
        progressBar.setBounds(541, 11, 42, 295);
        contentPane.add(progressBar);

        /*
        JTextPane jpane = new JTextPane();

        Player self = client.getLocalPlayer();
        int anim = self.getAnimation();
        int graph = self.getGraphic();
        String text = "Animation: " + anim + "Gra: " + graph;
        jpane.setText(text);
        */
        JButton btnNewButton = new JButton("Kick");
        btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnNewButton.setForeground(Color.BLACK);
        btnNewButton.setBounds(10, 11, 151, 23);
        contentPane.add(btnNewButton);
        btnNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {Kick(evt); }
        });

        JTextArea textField = new JTextArea();
        textField.setBounds(541, 11, 42, 295);
        contentPane.add(textField);


    }
    private void playerInfo(ActionEvent evt){
        Player self = client.getLocalPlayer();
    }
    private void Kick(ActionEvent evt) {//GEN-FIRST:event_banButtonActionPerformed
        {
            Client client = null;
            Player self = client.getLocalPlayer();
            String name = self.getSkullIcon().toString();
            if (self != null) {

                System.out.println("clicked");
                JOptionPane.showMessageDialog(null, self.getWorldArea().toString());
            } else {
                JOptionPane.showMessageDialog(null,  name +" was not found");
            }
        }
    }      // TODO add your handling code here:



    class SwingAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public SwingAction() {
            putValue(NAME, "SwingAction");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }
        public void actionPerformed(ActionEvent e) {
        }
    }
}