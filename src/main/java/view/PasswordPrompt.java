package view;

import static view.Messages.tr;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

/** Asks for a password; a new one is typed twice. Returns {@code null} when cancelled. */
final class PasswordPrompt {

  private PasswordPrompt() {}

  /** A new password, typed twice, with the warning that a forgotten one cannot be recovered. */
  static char[] askNew(Component parent, String title) {
    String error = null;
    while (true) {
      JPasswordField first = new JPasswordField(18);
      JPasswordField second = new JPasswordField(18);
      JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
      panel.add(new JLabel(tr("lock.password")));
      panel.add(first);
      panel.add(new JLabel(tr("lock.repeat")));
      panel.add(second);
      panel.add(new JLabel(error != null ? error : tr("lock.warning")));
      if (!confirmed(parent, panel, first, title)) {
        return null;
      }
      char[] password = first.getPassword();
      char[] repeated = second.getPassword();
      boolean same = Arrays.equals(password, repeated);
      Arrays.fill(repeated, '\0');
      if (password.length > 0 && same) {
        return password;
      }
      error = tr(password.length == 0 ? "lock.empty" : "lock.mismatch");
    }
  }

  /** An existing password; {@code error} (may be null) says why it is asked again. */
  static char[] ask(Component parent, String title, String error) {
    JPasswordField field = new JPasswordField(18);
    JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
    panel.add(new JLabel(error != null ? error : tr("lock.unlock.prompt")));
    panel.add(field);
    return confirmed(parent, panel, field, title) ? field.getPassword() : null;
  }

  private static boolean confirmed(
      Component parent, JPanel panel, JPasswordField focus, String title) {
    SwingUtilities.invokeLater(focus::requestFocusInWindow);
    return JOptionPane.showConfirmDialog(
            parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION;
  }
}
