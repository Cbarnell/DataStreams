import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataStreams extends JFrame {
    private final JTextArea originalTextArea;
    private final JTextArea filteredTextArea;
    private final JTextField searchField;
    private final JButton searchButton;

    private File currentFile;

    public DataStreams() {
        setTitle("Data Streams Processor - DataStreams");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize text areas with parallel scrolling
        originalTextArea = new JTextArea();
        originalTextArea.setEditable(false);
        filteredTextArea = new JTextArea();
        filteredTextArea.setEditable(false);

        JScrollPane originalScroll = new JScrollPane(originalTextArea);
        originalScroll.setBorder(BorderFactory.createTitledBorder("Original File"));

        JScrollPane filteredScroll = new JScrollPane(filteredTextArea);
        filteredScroll.setBorder(BorderFactory.createTitledBorder("Filtered Results (Java Streams)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, originalScroll, filteredScroll);
        splitPane.setResizeWeight(0.5);

        // Initialize controls
        searchField = new JTextField(15);
        JButton loadButton = new JButton("Load File");
        searchButton = new JButton("Search File");
        searchButton.setEnabled(false); // Disabled until a file is loaded
        JButton quitButton = new JButton("Quit");

        // Top Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.add(loadButton);
        controlPanel.add(new JLabel("Search String:"));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(quitButton);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        loadButton.addActionListener(_ -> loadFile());
        searchButton.addActionListener(_ -> searchFile());
        quitButton.addActionListener(_ -> System.exit(0));

        // Trigger search on pressing Enter in the text field
        searchField.addActionListener(_ -> {
            if (searchButton.isEnabled()) {
                searchFile();
            }
        });
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                // Read original file using modern Java Files class
                String content = Files.readString(currentFile.toPath());
                originalTextArea.setText(content);
                filteredTextArea.setText(""); // Reset filtered view
                searchField.setText("");
                searchButton.setEnabled(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + e.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
                searchButton.setEnabled(false);
                currentFile = null;
            }
        }
    }

    private void searchFile() {
        if (currentFile == null) return;

        String searchTerm = searchField.getText();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a search string.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        try (Stream<String> lines = Files.lines(Paths.get(currentFile.getAbsolutePath()))) {

            // Filter lines containing the search string using a lambda expression
            String filteredContent = lines
                    .filter(line -> line.contains(searchTerm))
                    .collect(Collectors.joining("\n"));

            filteredTextArea.setText(filteredContent);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error processing file stream: " + e.getMessage(),
                    "Stream Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    static void main() {
        // Ensure GUI runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new DataStreams().setVisible(true));
    }
}