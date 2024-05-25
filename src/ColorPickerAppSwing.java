import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ColorPickerAppSwing {
    private JFrame mainFrame;
    private JFrame settingsFrame;
    private JFrame colorFrame;
    private Color chosenColor = Color.WHITE;
    private Color previousColor = Color.WHITE;
    private Timer timer;
    private int intervalSeconds = 1;
    private JButton chooseColorButton;
    private JButton startButton;
    private JButton stopButton;
    private JRadioButton countdownRadio;
    private JRadioButton enterTimeRadio;
    private JTextField timeField;
    private JTextField secondsField;
    private JComboBox<Integer> intervalComboBox;
    private JPanel colorPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ColorPickerAppSwing app = new ColorPickerAppSwing();
            app.createAndShowMainFrame();
        });
    }

    private void createAndShowMainFrame() {
        mainFrame = new JFrame("Main Frame");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(200, 100);
        mainFrame.setLocationRelativeTo(null);

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
                createAndShowSettingsFrame();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
            }
        });

        JPanel panel = new JPanel();
        panel.add(settingsButton);
        panel.add(closeButton);
        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }

    private void createAndShowSettingsFrame() {
        settingsFrame = new JFrame("Settings Frame");
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setSize(400, 330);
        settingsFrame.setLocationRelativeTo(mainFrame);

        settingsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1));

        JPanel chooseColorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        chooseColorButton = new JButton("Choose Color");
        chooseColorButton.setFont(new Font("Arial", Font.PLAIN, 15));
        chooseColorButton.setPreferredSize(new Dimension(200, 55));
        chooseColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                previousColor = chosenColor;
                chosenColor = JColorChooser.showDialog(settingsFrame, "Choose a Color", chosenColor);
                colorPanel.setBackground(chosenColor);
            }
        });
        chooseColorPanel.add(chooseColorButton);
        panel.add(chooseColorPanel);

        JPanel countdownPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        countdownRadio = new JRadioButton("Countdown");
        countdownPanel.add(countdownRadio);
        secondsField = new JTextField(10);
        secondsField.setDocument(new JTextFieldLimit(10));
        countdownPanel.add(new JLabel("Seconds:"));
        countdownPanel.add(secondsField);
        panel.add(countdownPanel);

        JPanel enterTimePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        enterTimeRadio = new JRadioButton("Enter Time");
        enterTimePanel.add(enterTimeRadio);
        timeField = new JTextField(10);
        timeField.setDocument(new JTextFieldLimit(5));
        enterTimePanel.add(new JLabel("Time (HH:mm):"));
        enterTimePanel.add(timeField);
        panel.add(enterTimePanel);

        ButtonGroup group = new ButtonGroup();
        group.add(countdownRadio);
        group.add(enterTimeRadio);

        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        intervalPanel.add(new JLabel("Interval (seconds):"));
        intervalComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        intervalPanel.add(intervalComboBox);
        intervalComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intervalSeconds = (int) intervalComboBox.getSelectedItem();
            }
        });
        panel.add(intervalPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        JLabel chosenColorLabel = new JLabel("Chosen Color:");
        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(100, 25));
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorPanel.setBackground(chosenColor);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (countdownRadio.isSelected()) {
                    if (!secondsField.getText().isEmpty()) {
                        int seconds = Integer.parseInt(secondsField.getText());
                        startCountdown(seconds);
                    }
                } else if (enterTimeRadio.isSelected()) {
                    String timeString = timeField.getText();
                    try {
                        LocalTime time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
                        long delay = time.toSecondOfDay() - LocalTime.now().toSecondOfDay();
                        if (delay <= 0) {
                            JOptionPane.showMessageDialog(settingsFrame, "Please enter a future time.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            enableSettingsControls();
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    showColorFrame();
                                }
                            }, delay * 1000);
                            stopButton.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(settingsFrame, "Invalid time format. Please enter time in HH:mm format.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    timer.cancel();
                }
                if (colorFrame != null) {
                    colorFrame.dispose();
                }
                stopButton.setEnabled(false);
                resetSettingsFrame();
            }
        });

        buttonsPanel.add(startButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(chosenColorLabel);
        buttonsPanel.add(colorPanel);
        panel.add(buttonsPanel);

        settingsFrame.add(panel);
        settingsFrame.setVisible(true);
    }

    private void startCountdown(int seconds) {
        showColorFrame();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (colorFrame != null) {
                    colorFrame.dispose();
                    enableSettingsControls();
                    stopButton.setEnabled(false);
                    resetSettingsFrame();
                }
            }
        }, seconds * 1000);
        disableSettingsControls();
        stopButton.setEnabled(true);
    }

    private void showColorFrame() {
        Point settingsLocation = settingsFrame.getLocation();
        colorFrame = new JFrame("Color Frame");
        colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        colorFrame.setSize(400, 300);
        colorFrame.getContentPane().setBackground(chosenColor);
        colorFrame.setLocation((int) settingsLocation.getX() + settingsFrame.getWidth(), (int) settingsLocation.getY());
        colorFrame.setVisible(true);

        TimerTask closeColorFrameTask = new TimerTask() {
            @Override
            public void run() {
                colorFrame.dispose();
                resetSettingsFrame();
                stopButton.setEnabled(false);
                chosenColor = Color.WHITE;
                colorPanel.setBackground(chosenColor);
            }
        };
        Timer closeColorFrameTimer = new Timer();
        closeColorFrameTimer.schedule(closeColorFrameTask, 10000);
        startColorChange();
        TimerTask stopColorChangeTask = new TimerTask() {
            @Override
            public void run() {
                colorFrame.dispose();
                resetSettingsFrame();
                stopButton.setEnabled(false);
                chosenColor = Color.WHITE;
                colorPanel.setBackground(chosenColor);
            }
        };
        Timer stopColorChangeTimer = new Timer();
        stopColorChangeTimer.schedule(stopColorChangeTask, 10000); // 10 sekundi
    }

    private void disableSettingsControls() {
        chooseColorButton.setEnabled(false);
        countdownRadio.setEnabled(true);
        enterTimeRadio.setEnabled(true);
        timeField.setEnabled(false);
        secondsField.setEnabled(false);
        startButton.setEnabled(false);
        intervalComboBox.setEnabled(false);
    }

    private void enableSettingsControls() {
        chooseColorButton.setEnabled(true);
        countdownRadio.setEnabled(true);
        enterTimeRadio.setEnabled(true);
        if (countdownRadio.isSelected()) {
            secondsField.setEnabled(true);
        } else if (enterTimeRadio.isSelected()) {
            timeField.setEnabled(true);
        }
        startButton.setEnabled(true);
        intervalComboBox.setEnabled(true);
    }

    private void startColorChange() {
        TimerTask task = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                if (count % 2 == 0) {
                    colorFrame.getContentPane().setBackground(chosenColor);
                } else {
                    colorFrame.getContentPane().setBackground(Color.WHITE);
                }
                count++;
                if (count >= intervalSeconds * 10) {
                    cancel();
                }
            }
        };
        Timer colorChangeTimer = new Timer();
        colorChangeTimer.schedule(task, 0, 1000 / intervalSeconds);
    }

    private void resetSettingsFrame() {
        chooseColorButton.setEnabled(true);
        countdownRadio.setSelected(true);
        enterTimeRadio.setSelected(true);
        secondsField.setText("");
        secondsField.setEnabled(true);
        timeField.setText("");
        timeField.setEnabled(true);
        intervalComboBox.setSelectedItem(1);
        intervalComboBox.setEnabled(true);
        startButton.setEnabled(true);
        chosenColor = previousColor;
    }
}

class JTextFieldLimit extends PlainDocument {
    private int limit;

    JTextFieldLimit(int limit) {
        super();
        this.limit = limit;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null)
            return;

        if ((getLength() + str.length()) <= limit) {
            super.insertString(offset, str, attr);
        }
    }
}
