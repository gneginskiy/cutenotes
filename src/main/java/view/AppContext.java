package view;

import javax.swing.JFrame;

import dao.GroupStore;
import util.EncryptingRepository;

/** The parts of the main window that commands work on. */
record AppContext(
    JFrame frame,
    NoteSession session,
    TabsPane tabs,
    SearchBar search,
    AppOptions options,
    Toast toast,
    EncryptingRepository repo,
    GroupStore groups) {}
