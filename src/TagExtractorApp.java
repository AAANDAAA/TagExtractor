import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class TagExtractorApp extends JFrame {

    private JTextArea textArea;
    private JButton loadFileButton, loadStopWordsButton, processButton, saveButton;
    private JLabel fileLabel;
    private Path documentPath, stopWordsPath;
    private Map<String, Integer> wordFrequencyMap;
    private Set<String> stopWords;

    public TagExtractorApp() {
        super("Tag Extractor");
        wordFrequencyMap = new HashMap<>();
        stopWords = new TreeSet<>();
        setupGUI();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        loadFileButton = new JButton("Load Document");
        loadStopWordsButton = new JButton("Load Stop Words");
        processButton = new JButton("Process Tags");
        saveButton = new JButton("Save Results");
        fileLabel = new JLabel("No file loaded");

        topPanel.add(fileLabel);
        topPanel.add(loadFileButton);
        topPanel.add(loadStopWordsButton);
        topPanel.add(processButton);
        topPanel.add(saveButton);

        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        loadFileButton.addActionListener(this::loadFile);
        loadStopWordsButton.addActionListener(this::loadStopWords);
        processButton.addActionListener(this::processTags);
        saveButton.addActionListener(this::saveResults);

        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void loadFile(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            documentPath = fileChooser.getSelectedFile().toPath();
            fileLabel.setText("Document: " + documentPath.getFileName());
        }
    }

    private void loadStopWords(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordsPath = fileChooser.getSelectedFile().toPath();
            try {
                stopWords.clear();
                Files.lines(stopWordsPath)
                        .map(String::trim)
                        .forEach(stopWords::add);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading stop words: " + e.getMessage());
            }
        }
    }

    private void processTags(ActionEvent event) {
        if (documentPath == null || stopWordsPath == null) {
            JOptionPane.showMessageDialog(this, "Please load both document and stop words files.");
            return;
        }

        wordFrequencyMap.clear();
        try {
            Files.lines(documentPath)
                    .flatMap(line -> Arrays.stream(line.split("\\s+")))
                    .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase())
                    .filter(word -> !word.isEmpty() && !stopWords.contains(word))
                    .forEach(word -> wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1));

            displayTags();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading document: " + e.getMessage());
        }
    }

    private void displayTags() {
        StringBuilder builder = new StringBuilder();
        wordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));

        textArea.setText(builder.toString());
    }

    private void saveResults(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path savePath = fileChooser.getSelectedFile().toPath();
            try (BufferedWriter writer = Files.newBufferedWriter(savePath)) {
                wordFrequencyMap.forEach((word, count) -> {
                    try {
                        writer.write(word + ": " + count);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                JOptionPane.showMessageDialog(this, "Results saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TagExtractorApp().setVisible(true));
    }
}
