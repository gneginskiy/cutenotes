package view;

import controller.NotesDataManager;
import util.IntervalRecorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class NotesUI extends JFrame {

    private final NotesDataManager notesDataManager;

    public NotesUI(String title, NotesDataManager notesDataManager) {
        super(title);
        this.notesDataManager = notesDataManager;
        initUI();
    }

    //region swing ui frames
    private void initUI() {
        JTextArea area = getJTextArea(notesDataManager.getLastVersion().replace("\n", "\r\n"));
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(new Dimension(400, 300));
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        new IntervalRecorder(area::getText, notesDataManager).start();
    }

    private JTextArea getJTextArea(String initialText) {
        JPanel panel = new JPanel();
        JTextArea area = new JTextArea();
        JScrollPane pane = new JScrollPane();
        area.setText(initialText);
        area.setFont(new Font("comic sans ms", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        area.setBackground(new Color(250, 250, 180));
        plugUndoManager(area);
        pane.getViewport().add(area);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(pane);
        add(panel);
        return area;
    }
    //endregion

    //region ui listeners
    private void plugUndoManager(JTextArea area) {
        UndoManager undoManager = new UndoManager();
        Document document = area.getDocument();
        document.addUndoableEditListener(event -> undoManager.addEdit(event.getEdit()));
        InputMap inputMap = area.getInputMap();
        ActionMap actionMap = area.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(90, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                "Undo");
        inputMap.put(KeyStroke.getKeyStroke(89, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                "Redo");
        actionMap.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });
        actionMap.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });
    }
    //endregion

}
