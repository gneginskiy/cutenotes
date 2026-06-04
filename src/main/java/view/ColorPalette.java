package view;

import java.awt.Color;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;

import model.Theme;

final class ColorPalette {

  private final JColorChooser chooser;
  private final ColorField bgField = new ColorField("Background", true);
  private final ColorField fgField = new ColorField("Text", false);
  private final ColorField caretField = new ColorField("Caret", false);
  private final ColorField codeField = new ColorField("Code", false);
  private final Runnable onChange;
  private Color bg = Theme.DEFAULT.bg();
  private Color fg = Theme.DEFAULT.fg();
  private Color caret = Theme.DEFAULT.caret();
  private Color code = Theme.DEFAULT.codeColor();
  private boolean syncing;

  ColorPalette(JColorChooser chooser, Runnable onChange) {
    this.chooser = chooser;
    this.onChange = onChange;
    ButtonGroup grp = new ButtonGroup();
    grp.add(bgField.radio());
    grp.add(fgField.radio());
    grp.add(caretField.radio());
    grp.add(codeField.radio());
    wire();
  }

  ColorField bgField() {
    return bgField;
  }

  ColorField fgField() {
    return fgField;
  }

  ColorField caretField() {
    return caretField;
  }

  ColorField codeField() {
    return codeField;
  }

  Color bg() {
    return bg;
  }

  Color fg() {
    return fg;
  }

  Color caret() {
    return caret;
  }

  Color code() {
    return code;
  }

  void load(Color bgArg, Color fgArg, Color caretArg, Color codeArg) {
    syncing = true;
    bg = bgArg;
    fg = fgArg;
    caret = caretArg;
    code = codeArg;
    refreshHex();
    bgField.radio().setSelected(true);
    chooser.setColor(bg);
    syncing = false;
  }

  private void wire() {
    bgField.radio().addActionListener(e -> showInChooser(bg));
    fgField.radio().addActionListener(e -> showInChooser(fg));
    caretField.radio().addActionListener(e -> showInChooser(effectiveCaret()));
    codeField.radio().addActionListener(e -> showInChooser(code));
    bgField.onTyped(c -> assign(bgField, c == null ? Theme.DEFAULT.bg() : c));
    fgField.onTyped(c -> assign(fgField, c == null ? Theme.DEFAULT.fg() : c));
    caretField.onTyped(c -> assign(caretField, c));
    codeField.onTyped(c -> assign(codeField, c == null ? Theme.DEFAULT.codeColor() : c));
    chooser.getSelectionModel().addChangeListener(e -> onChooserChange());
  }

  private void onChooserChange() {
    if (!syncing) {
      assign(selectedField(), chooser.getColor());
    }
  }

  private ColorField selectedField() {
    if (bgField.isSelected()) {
      return bgField;
    }
    if (fgField.isSelected()) {
      return fgField;
    }
    return caretField.isSelected() ? caretField : codeField;
  }

  private void assign(ColorField f, Color c) {
    if (f == bgField) {
      bg = c;
    } else if (f == fgField) {
      fg = c;
    } else if (f == caretField) {
      caret = c;
    } else {
      code = c;
    }
    refreshHex();
    showInChooser(f == caretField ? effectiveCaret() : c);
    onChange.run();
  }

  private void showInChooser(Color c) {
    if (c != null && !c.equals(chooser.getColor())) {
      syncing = true;
      chooser.setColor(c);
      syncing = false;
    }
  }

  private void refreshHex() {
    bgField.show(bg);
    fgField.show(fg);
    caretField.show(effectiveCaret());
    codeField.show(code);
  }

  private Color effectiveCaret() {
    return caret != null ? caret : Colors.inverse(bg);
  }
}
