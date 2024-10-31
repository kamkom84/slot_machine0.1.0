package slotGame;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class SlotMachine extends JFrame {

    private JButton[] betButtons;
    private JButton autoButton;
    private JLabel[][] reels;
    private JDialog winDialog;
    private JTable winTable;
    private List<Object[]> winData = new ArrayList<>();
    private String[] tableHeaders = {"N", "Session win logs"};
    private JLabel[] positionLabels;
    private Timer spinTimer;
    private Timer blinkTimer;
    private Timer[] grayTimers = new Timer[240];
    private Random random = new Random();
    private String[] symbols = {"🍒", "🍋", "🍊", "🍇", "⭐", "🔔", "💎", "🍉", "7", "Z"};
    private Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
    private boolean isSpinning = false;
    private boolean autoMode = false;
    private boolean betSelected = false;
    private boolean autoRunning = false;
    private int selectedBetIndex = -1;
    private int logCounter = 1;
    private double initialMoney = 1000;
    private double currentMoney = initialMoney;
    private double betMoney = 0;
    private double sessionHigh = 0;
    private double sessionWin = 0;
    private double sessionLost = 0;
    private int gamesPlayed = 0;
    private JPanel infoPanel;
    private JLabel lblInitialMoney, lblCurrentMoney, lblSessionBet, lblSessionHigh, lblSessionWin, lblSessionLost, lblGames;
    private Map<String, Double> symbolValues = new HashMap<>();
    private static final int SPIN_DURATION = 2000;
    private JLabel lblRTP;
    private double totalBets = 0;
    private double totalPayouts = 0;

    public SlotMachine() {
        setTitle("Slot Machine");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(300, 200));
        infoPanel.setLayout(new GridLayout(9, 2, 5, 5));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setForeground(Color.WHITE);

        addInfoRow("Initial money", lblInitialMoney = new JLabel(String.format("%.2f", initialMoney)));
        addInfoRow("Current money", lblCurrentMoney = new JLabel(String.format("%.2f", currentMoney)));
        addInfoRow("Selected bet", lblSessionBet = new JLabel("0.00"));
        addInfoRow("Session high", lblSessionHigh = new JLabel("0.00"));
        addInfoRow("Session win", lblSessionWin = new JLabel("0.00"));
        addInfoRow("Session lost", lblSessionLost = new JLabel("0.00"));
        addInfoRow("Games", lblGames = new JLabel("0"));
        addInfoRow("RTP%", lblRTP = new JLabel("0.00"));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        JLabel titleLabel = new JLabel("x_periment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("OCR A Extended", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 200));
        rightPanel.setLayout(new GridLayout(3, 5, 5, 5));
        rightPanel.setBackground(Color.BLACK);

        positionLabels = new JLabel[15];
        for (int i = 0; i < 15; i++) {
            positionLabels[i] = new JLabel("", SwingConstants.CENTER);
            positionLabels[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
            positionLabels[i].setOpaque(true);
            positionLabels[i].setBackground(Color.BLACK);
            positionLabels[i].setForeground(new Color(0, 100, 0));
            positionLabels[i].setBorder(BorderFactory.createLineBorder(Color.WHITE));
            rightPanel.add(positionLabels[i]);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.BLACK);
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        JPanel reelPanel = new JPanel();
        reelPanel.setLayout(new GridBagLayout());
        reelPanel.setPreferredSize(new Dimension(1400, 500));
        reelPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        reels = new JLabel[3][5];
        gbc.gridy = 0;
        for (int row = 0; row < 3; row++) {
            gbc.gridy = row;
            for (int col = 0; col < 5; col++) {
                reels[row][col] = new JLabel(getRandomSymbol(), SwingConstants.CENTER);
                reels[row][col].setFont(new Font("Segoe UI Emoji", Font.BOLD, 80));
                reels[row][col].setOpaque(true);
                reels[row][col].setBackground(Color.DARK_GRAY); // Тъмен фон за клетките
                reels[row][col].setForeground(getRandomColor());
                reels[row][col].setPreferredSize(new Dimension(150, 150));
                reels[row][col].setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                gbc.gridx = col;
                reelPanel.add(reels[row][col], gbc);
            }
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setPreferredSize(new Dimension(1200, 100));

        autoButton = new JButton("Auto");
        autoButton.setForeground(new Color(0, 100, 0));
        autoButton.setBackground(Color.GRAY);
        autoButton.setFont(new Font("OCR A Extended", Font.BOLD, 20));
        autoButton.addActionListener(e -> handleAutoButton());
        buttonPanel.add(autoButton);

        String[] buttons = {"x1.00", "x2.00", "x3.00", "x4.00", "x5.00"};
        betButtons = new JButton[5];

        for (int i = 0; i < buttons.length; i++) {
            final JButton button = new JButton(buttons[i]);
            button.setForeground(new Color(0, 100, 0));
            button.setBackground(Color.GRAY);
            button.setFont(new Font("OCR A Extended", Font.BOLD, 20));

            betButtons[i] = button;
            final int index = i;
            button.addActionListener(e -> handleBetButtonClick(index));

            buttonPanel.add(button);
        }

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(reelPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        createWinDialog();
        initializeSymbolValues();
    }

    private void createWinDialog() {
        winDialog = new JDialog(this, "Current Wins", false);
        winDialog.setSize(600, 400);
        winDialog.setLocationRelativeTo(this);

        winTable = new JTable(new Object[0][0], tableHeaders);
        JScrollPane scrollPane = new JScrollPane(winTable);
        winDialog.add(scrollPane);
    }

    private void updateWinTable(List<Object[]> winData) {
        Object[][] tableData = winData.toArray(new Object[0][]);
        winTable.setModel(new javax.swing.table.DefaultTableModel(tableData, tableHeaders));

        if (!winDialog.isVisible()) {
            winDialog.setVisible(true);
        }
    }

    private void processWin(double bet, boolean[][] winningPositions) {
        double winAmount = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                if (winningPositions[row][col]) {
                    String symbol = reels[row][col].getText();
                    Color color = reels[row][col].getForeground();
                    String symbolKey = symbol + "_" + getColorName(color);
                    double pricePerSymbol = symbolValues.getOrDefault(symbolKey, 0.0);
                    winAmount += pricePerSymbol * bet;
                }
            }
        }

        currentMoney += winAmount;
        sessionWin += winAmount;
        totalPayouts += winAmount;
        if (winAmount > sessionHigh) {
            sessionHigh = winAmount;
        }
        updateInfoPanel();
        updatePositionLabels(winningPositions);
        startGoldBlinking(winningPositions);
        updateRTPInfo();
    }

    private String getColorName(Color color) {
        if (color == Color.RED) return "RED";
        else if (color == Color.GREEN) return "GREEN";
        else if (color == Color.BLUE) return "BLUE";
        else if (color == Color.YELLOW) return "YELLOW";
        else if (color == Color.CYAN) return "CYAN";
        else if (color == Color.MAGENTA) return "MAGENTA";
        return "UNKNOWN";
    }

    private void handleAutoButton() {
        if (autoButton.getText().equals("Auto")) {
            autoMode = true;
            autoButton.setBackground(Color.YELLOW);
            autoButton.setText("Select");

            for (JButton betButton : betButtons) {
                betButton.setForeground(Color.YELLOW);
            }

        } else if (autoButton.getText().equals("Start")) {
            autoButton.setBackground(new Color(255, 182, 193));
            autoButton.setForeground(Color.BLACK);
            autoButton.setText("Stop auto");
            startAutoSpin();
        } else if (autoButton.getText().equals("Stop auto")) {
            stopAutoSpin();
        }
    }

    private void handleBetButtonClick(int index) {
        betSelected = true;
        selectedBetIndex = index;

        for (int i = 0; i < betButtons.length; i++) {
            if (i != index) {
                betButtons[i].setForeground(new Color(0, 100, 0));
                betButtons[i].setEnabled(true);
            } else {
                betButtons[i].setForeground(Color.YELLOW);
                betButtons[i].setEnabled(false);
            }
        }

        if (autoMode) {
            autoButton.setBackground(new Color(144, 238, 144));
            autoButton.setText("Start");
        } else {
            makeBetAndSpin(false);
        }

        stopBlinking();
        resetReelBorders();
    }

    private void stopBlinking() {
        if (blinkTimer != null) {
            blinkTimer.stop();
            blinkTimer = null;
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                reels[row][col].setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                reels[row][col].setBackground(Color.DARK_GRAY);
            }
        }
    }

    private void startAutoSpin() {
        if (!autoRunning && selectedBetIndex >= 0) {
            autoRunning = true;
            makeBetAndSpin(true);
        }
    }

    private void stopAutoSpin() {
        autoRunning = false;
        autoMode = false;
        autoButton.setBackground(Color.GRAY);
        autoButton.setForeground(new Color(0, 100, 0));
        autoButton.setText("Auto");

        betSelected = false;
        selectedBetIndex = -1;

        for (JButton betButton : betButtons) {
            betButton.setEnabled(true);
            betButton.setForeground(new Color(0, 100, 0));
        }
    }

    private void makeBetAndSpin(boolean isAutoPlay) {
        double betAmount = getBetAmount();
        if (currentMoney >= betAmount) {
            totalBets += betAmount;
            currentMoney -= betAmount;
            sessionLost += betAmount;
            gamesPlayed++;
            updateInfoPanel();
            stopBlinking();
            startSpinning(betAmount, isAutoPlay);
        }
    }

    private double getBetAmount() {
        String betText = betButtons[selectedBetIndex].getText().substring(1);
        return Double.parseDouble(betText);
    }

    private void startSpinning(double bet, boolean isAutoPlay) {
        if (isSpinning) return;

        isSpinning = true;

        fadeOutWinningPositions();

        spinTimer = new Timer(100, new ActionListener() {
            private int elapsedTime = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime += 100;
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 5; col++) {
                        reels[row][col].setText(getRandomSymbol());
                        reels[row][col].setForeground(getRandomColor());
                    }
                }
                if (elapsedTime >= SPIN_DURATION) {
                    spinTimer.stop();
                    isSpinning = false;

                    if (isAutoPlay) {
                        checkForWin(bet, true);
                    } else {
                        checkForWin(bet, false);
                    }
                }
            }
        });
        spinTimer.start();
    }

    private void checkForWin(double bet, boolean isAutoPlay) {
        boolean[][] winningPositions1 = new boolean[3][5];
        boolean hasWin1 = performWinCheck(bet, winningPositions1);

        boolean[][] winningPositions2 = new boolean[3][5];
        boolean hasWin2 = performWinCheck(bet, winningPositions2);

        if (hasWin1 == hasWin2) {
            if (hasWin1) {
                processWin(bet, winningPositions1);
            } else {
                updateInfoPanel();
            }
        } else {
            boolean[][] winningPositions3 = new boolean[3][5];
            boolean hasWin3 = performWinCheck(bet, winningPositions3);

            if (hasWin3) {
                processWin(bet, winningPositions3);
            } else {
                updateInfoPanel();
            }
        }

        if (isAutoPlay && autoRunning) {
            Timer autoSpinTimer = new Timer(2000, e -> makeBetAndSpin(true));
            autoSpinTimer.setRepeats(false);
            autoSpinTimer.start();
        }
    }

    private boolean performWinCheck(double bet, boolean[][] winningPositions) {
        boolean hasWin = false;
        double winAmount = 0;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                String symbol = reels[row][col].getText();
                int count = 1;
                for (int j = col + 1; j < 5; j++) {
                    if (reels[row][j].getText().equals(symbol)) {
                        count++;
                    } else {
                        break;
                    }
                }
                if (count >= 3) {
                    for (int j = col; j < col + count; j++) {
                        winningPositions[row][j] = true;
                    }
                    winAmount += bet * count;
                    col += count - 1;
                    hasWin = true;
                }
            }
        }

        return hasWin;
    }

    private void startGoldBlinking(boolean[][] winningPositions) {
        blinkTimer = new Timer(500, new ActionListener() {
            private boolean isGold = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 5; col++) {
                        if (winningPositions[row][col]) {
                            reels[row][col].setBackground(isGold ? new Color(255, 255, 153) : Color.DARK_GRAY);
                        }
                    }
                }
                isGold = !isGold;
            }
        });
        blinkTimer.start();
    }

    private void resetReelBorders() {
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
        for (int row = 0; row < reels.length; row++) {
            for (int col = 0; col < reels[row].length; col++) {
                reels[row][col].setBackground(Color.DARK_GRAY);
                reels[row][col].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
            }
        }
    }

    private void fadeOutWinningPositions() {
        for (int i = 0; i < positionLabels.length; i++) {
            if (!positionLabels[i].getText().isEmpty()) {
                positionLabels[i].setForeground(Color.GRAY);

                int finalI = i;
                grayTimers[i] = new Timer(15000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        positionLabels[finalI].setText("");
                        grayTimers[finalI].stop();
                    }
                });
                grayTimers[i].setRepeats(false);
                grayTimers[i].start();
            }
        }
    }

    private void updatePositionLabels(boolean[][] winningPositions) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                if (winningPositions[row][col]) {
                    int index = row * 5 + col;
                    positionLabels[index].setText(reels[row][col].getText());
                    positionLabels[index].setForeground(reels[row][col].getForeground());

                    if (grayTimers[index] != null) {
                        grayTimers[index].stop();
                    }
                }
            }
        }
    }

    private void updateInfoPanel() {
        lblCurrentMoney.setText(String.format(Locale.US, "%.2f", currentMoney));
        lblSessionBet.setText(String.format(Locale.US, "%.2f", getBetAmount()));
        lblSessionHigh.setText(String.format(Locale.US, "%.2f", sessionHigh));
        lblSessionWin.setText(String.format(Locale.US, "%.2f", sessionWin));
        lblSessionLost.setText(String.format(Locale.US, "%.2f", sessionLost));
        lblGames.setText(String.valueOf(gamesPlayed));

        if (currentMoney > initialMoney) {
            lblCurrentMoney.setForeground(Color.GREEN);
        } else if (currentMoney < initialMoney) {
            lblCurrentMoney.setForeground(new Color(255, 99, 71));
        } else {
            lblCurrentMoney.setForeground(Color.WHITE);
        }
    }

    private void updateRTPInfo() {
        double rtp = calculateRTP();
        lblRTP.setText(String.format(Locale.US, "%.2f%%", rtp));
    }

    private double calculateRTP() {
        if (totalBets == 0) {
            return 0.0;
        }
        return (totalPayouts / totalBets) * 100;
    }

    private void initializeSymbolValues() {

        symbolValues.put("🍒_RED", 0.11);
        symbolValues.put("🍒_GREEN", 0.17);
        symbolValues.put("🍒_BLUE", 0.24);
        symbolValues.put("🍒_YELLOW", 0.36);
        symbolValues.put("🍒_CYAN", 0.98);
        symbolValues.put("🍒_MAGENTA", 1.55);

        symbolValues.put("🍋_RED", 0.07);
        symbolValues.put("🍋_GREEN", 0.18);
        symbolValues.put("🍋_BLUE", 0.41);
        symbolValues.put("🍋_YELLOW", 0.89);
        symbolValues.put("🍋_CYAN", 1.45);
        symbolValues.put("🍋_MAGENTA", 2.51);

        symbolValues.put("🍊_RED", 0.59);
        symbolValues.put("🍊_GREEN", 0.94);
        symbolValues.put("🍊_BLUE", 1.71);
        symbolValues.put("🍊_YELLOW", 2.14);
        symbolValues.put("🍊_CYAN", 2.29);
        symbolValues.put("🍊_MAGENTA", 4.20);

        symbolValues.put("🍇_RED", 0.22);
        symbolValues.put("🍇_GREEN", 0.33);
        symbolValues.put("🍇_BLUE", 0.44);
        symbolValues.put("🍇_YELLOW", 0.66);
        symbolValues.put("🍇_CYAN", 0.88);
        symbolValues.put("🍇_MAGENTA", 1.99);

        symbolValues.put("⭐_RED", 0.51);
        symbolValues.put("⭐_GREEN", 0.79);
        symbolValues.put("⭐_BLUE", 1.01);
        symbolValues.put("⭐_YELLOW", 1.26);
        symbolValues.put("⭐_CYAN", 1.44);
        symbolValues.put("⭐_MAGENTA", 2.68);

        symbolValues.put("🔔_RED", 0.56);
        symbolValues.put("🔔_GREEN", 0.98);
        symbolValues.put("🔔_BLUE", 1.01);
        symbolValues.put("🔔_YELLOW", 1.31);
        symbolValues.put("🔔_CYAN", 1.67);
        symbolValues.put("🔔_MAGENTA", 1.96);

        symbolValues.put("💎_RED", 0.80);
        symbolValues.put("💎_GREEN", 0.90);
        symbolValues.put("💎_BLUE", 1.10);
        symbolValues.put("💎_YELLOW", 1.53);
        symbolValues.put("💎_CYAN", 1.99);
        symbolValues.put("💎_MAGENTA", 2.58);

        symbolValues.put("🍉_RED", 0.91);
        symbolValues.put("🍉_GREEN", 1.53);
        symbolValues.put("🍉_BLUE", 2.09);
        symbolValues.put("🍉_YELLOW", 2.59);
        symbolValues.put("🍉_CYAN", 2.90);
        symbolValues.put("🍉_MAGENTA", 3.05);

        symbolValues.put("7_RED", 1.01);
        symbolValues.put("7_GREEN", 2.02);
        symbolValues.put("7_BLUE", 3.03);
        symbolValues.put("7_YELLOW", 4.04);
        symbolValues.put("7_CYAN", 4.44);
        symbolValues.put("7_MAGENTA", 5.15);

        symbolValues.put("Z_RED", 5.01);
        symbolValues.put("Z_GREEN", 5.11);
        symbolValues.put("Z_BLUE", 5.21);
        symbolValues.put("Z_YELLOW", 5.31);
        symbolValues.put("Z_CYAN", 6.41);
        symbolValues.put("Z_MAGENTA", 6.51);
    }

    private String getRandomSymbol() {
        return symbols[random.nextInt(symbols.length)];
    }

    private Color getRandomColor() {
        return colors[random.nextInt(colors.length)];
    }

    private void addInfoRow(String label, JLabel valueLabel) {
        JLabel infoLabel = new JLabel(label, SwingConstants.LEFT);
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("OCR A Extended", Font.BOLD, 16));
        infoPanel.add(infoLabel);

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("OCR A Extended", Font.BOLD, 16));
        infoPanel.add(valueLabel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SlotMachine layout = new SlotMachine();
            layout.setVisible(true);
        });
    }

}