package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import model.Theme;

/**
 * Renders the README screenshots (screenshots/img*.png) from the real UI components of the app,
 * inside a drawn macOS window frame, at Retina (2x) resolution. Nothing is shown on screen.
 *
 * <p>Run through {@code scripts/update-screenshots.sh}. It must not run headless: the native Aqua
 * look (buttons, checkboxes) is only painted with a WindowServer connection.
 */
public final class ReadmeScreenshots {

  private static final int SCALE = 2;
  private static final int TITLE_BAR = 28;
  private static final int RADIUS = 10;
  private static final int MARGIN_X = 24;
  private static final int MARGIN_TOP = 18;
  private static final int MARGIN_BOTTOM = 34;
  private static final String TITLE = "Notes :3";

  private static File out;

  private ReadmeScreenshots() {}

  public static void main(String[] args) throws Exception {
    out = new File(args[0]);
    String readme = Files.readString(Path.of(args[1]));
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            matrix();
            decisionRecord();
            options();
            sticky();
            help();
            readme(readme);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        });
  }

  // ---- the six scenes -------------------------------------------------------------------------

  /** img.png: black and green "Matrix" scratch note. */
  private static void matrix() throws Exception {
    Theme t = theme(Color.BLACK, new Color(0x63D850), "Courier New");
    TabsPane tabs = window(t);
    type(tabs.activeArea(), "Wake up, Neo...\n" + "\n".repeat(9) + "The Matrix has you..");
    save("img.png", frame(tabs.component(), 442, 547, TITLE, t, true));
  }

  /** img2.png: light-blue decision record, window inactive. */
  private static void decisionRecord() throws Exception {
    Theme t = theme(new Color(0xC7F0FD), Color.BLACK, "Comic Sans MS");
    TabsPane tabs = window(t);
    type(
        tabs.activeArea(),
        "Decision:\nWe will adopt event-driven architecture.\n\n\nContext:\n"
            + "One engineer read a blog post.\n\n Alternatives considered:\n"
            + "   - Not doing this (rejected — would not be on the roadmap).\n"
            + "   - Doing this properly (rejected — out of scope).");
    save("img2.png", frame(tabs.component(), 460, 400, TITLE, t, false));
  }

  /** img3.png: the Options dialog editing a pink theme. */
  private static void options() throws Exception {
    Theme t =
        new Theme(
            new Color(0xFFBDBD),
            Color.BLACK,
            new Color(0x004242),
            Theme.DEFAULT.codeColor(),
            "Comic Sans MS",
            14,
            null,
            true);
    JColorChooser chooser = new JColorChooser();
    ColorChoosers.compact(chooser);
    ColorPalette palette = new ColorPalette(chooser, () -> {});
    OptionsFields fields = new OptionsFields(() -> {});
    ThemePreview preview = new ThemePreview();
    PresetRow presets = new PresetRow(p -> {});
    palette.load(t.bg(), t.fg(), t.caret(), t.codeColor());
    fields.load(t);
    preview.render(t);
    presets.highlight(t);
    javax.swing.JTabbedPane content = new javax.swing.JTabbedPane();
    content.addTab(
        Messages.tr("options.tabTheme"),
        OptionsForm.build(
            presets,
            preview,
            new OptionsForm.Colors(
                palette.bgField(), palette.fgField(), palette.caretField(), palette.codeField()),
            chooser,
            fields,
            new OptionsForm.Actions(() -> {}, () -> {}, () -> {})));
    content.addTab(
        Messages.tr("options.tabApp"),
        AppSettingsPanel.build(
            new AppSettingsPanel.Hooks(m -> {}, () -> {}, () -> {}, () -> {}, () -> {})));
    Dimension size = content.getPreferredSize();
    save("img3.png", dialogFrame(content, size.width, size.height + TITLE_BAR, "Options"));
  }

  /** img4.png: yellow sticky with two tabs. */
  private static void sticky() throws Exception {
    Theme t = theme(new Color(250, 250, 180), Color.BLACK, "Comic Sans MS");
    TabsPane tabs = window(t);
    tabs.openTab("b", "security_training", "");
    tabs.openTab("a", "various stuff", "");
    type(
        tabs.activeArea(),
        " Senior Engineer (2019–present)\n"
            + "   - Led migration from monolith to microservices\n"
            + "   - Led migration from microservices to monolith\n\n"
            + "-----BEGIN OPENSSH PRIVATE KEY-----\n"
            + "ZG9udC1ldmVyLXN0b3JlLWFueXRoaW5nLWluLXBsYWluLXRleHQtaXQncy1zdHVwaWQ=\n"
            + "-----END OPENSSH PRIVATE KEY-----");
    order(tabs, "a", "b");
    showTabs(tabs);
    save("img4.png", frame(tabs.component(), 398, 318, TITLE, t, true));
  }

  /** img5.png: the keyboard shortcuts sheet (F1) in the default theme. */
  private static void help() throws Exception {
    ThemeHolder.set(Theme.DEFAULT);
    Method build = HelpDialog.class.getDeclaredMethod("buildContent", UiPalette.class);
    build.setAccessible(true);
    JPanel content = (JPanel) build.invoke(null, UiPalette.current());
    Dimension size = content.getPreferredSize();
    save(
        "img5.png",
        dialogFrame(content, size.width, size.height + TITLE_BAR, "Keyboard Shortcuts"));
  }

  /** img6.png: the README pasted into a black and orange monospace note. */
  private static void readme(String text) throws Exception {
    Theme t = theme(Color.BLACK, new Color(0xE0953A), "Menlo");
    TabsPane tabs = window(t);
    tabs.openTab("r", "readme.md", "");
    type(tabs.activeArea(), text);
    tabs.openTab("s", "b2b-saas-startup-ideas", "");
    tabs.select("r");
    showTabs(tabs);
    save("img6.png", frame(tabs.component(), 665, 655, TITLE, t, true));
  }

  // ---- building the window content --------------------------------------------------------------

  private static Theme theme(Color bg, Color fg, String font) {
    return new Theme(bg, fg, null, Theme.DEFAULT.codeColor(), font, 14, null, true);
  }

  /** The main window's content, as {@link TabbedNotesUI} assembles it, in theme {@code t}. */
  private static TabsPane window(Theme t) {
    ThemeHolder.set(t);
    TabsPane tabs = new TabsPane();
    SearchBar search = new SearchBar(tabs::activeArea);
    tabs.addBelowHeader(search);
    tabs.applyTheme(t);
    search.applyTheme(t);
    return tabs;
  }

  /** Inserts text the way pasting does: literally, not parsed as Markdown. */
  private static void type(JTextComponent area, String text) throws Exception {
    area.getDocument().insertString(area.getDocument().getLength(), text, null);
    area.setCaretPosition(0);
  }

  private static void order(TabsPane tabs, String... ids) {
    TabHeader header = tabs.strip().header();
    for (int i = 0; i < ids.length; i++) {
      header.setComponentZOrder(chip(header, ids[i]), i);
    }
    header.revalidate();
  }

  private static Component chip(TabHeader header, String id) {
    String title = header.titleOf(id);
    for (Component c : header.getComponents()) {
      if (c instanceof TabChip chip && chip.title().equals(title)) {
        return c;
      }
    }
    throw new IllegalStateException("no tab " + id);
  }

  /** The tabs bar is shown after Esc or when pinned; here it is simply laid out as shown. */
  private static void showTabs(TabsPane tabs) {
    JComponent strip = tabs.strip().component();
    strip.setVisible(true);
    strip.setPreferredSize(new Dimension(0, 36));
  }

  // ---- window chrome ----------------------------------------------------------------------------

  /** A main window: the title bar is tinted with the theme's chrome colour, as on macOS. */
  private static BufferedImage frame(
      JComponent content, int w, int h, String title, Theme t, boolean active) {
    UiPalette p = UiPalette.of(t);
    boolean dark = Colors.isDark(p.chrome());
    Color titleFg = dark ? new Color(0xDFDFDF) : new Color(0x4D4D4D);
    return window(content, w, h, title, p.chrome(), active ? titleFg : faded(titleFg, p), active);
  }

  /** A dialog: the standard light macOS title bar. */
  private static BufferedImage dialogFrame(JComponent content, int w, int h, String title) {
    return window(content, w, h, title, new Color(0xECECEC), new Color(0x4D4D4D), true);
  }

  private static Color faded(Color fg, UiPalette p) {
    return Colors.blend(fg, p.chrome(), 0.5f);
  }

  private static BufferedImage window(
      JComponent content, int w, int h, String title, Color bar, Color titleFg, boolean active) {
    content.setSize(w, h - TITLE_BAR);
    layout(content);
    int outW = w + 2 * MARGIN_X;
    int outH = h + MARGIN_TOP + MARGIN_BOTTOM;
    BufferedImage img =
        new BufferedImage(outW * SCALE, outH * SCALE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(shadow(w, h, outW, outH), 0, 0, outW * SCALE, outH * SCALE, null);
    g.scale(SCALE, SCALE);
    Shape shape = new RoundRectangle2D.Float(MARGIN_X, MARGIN_TOP, w, h, RADIUS * 2, RADIUS * 2);
    g.setClip(shape);
    g.setColor(bar);
    g.fillRect(MARGIN_X, MARGIN_TOP, w, TITLE_BAR);
    trafficLights(g, active);
    titleText(g, title, titleFg, w);
    Graphics2D body = (Graphics2D) g.create();
    body.translate(MARGIN_X, MARGIN_TOP + TITLE_BAR);
    content.printAll(body);
    body.dispose();
    g.setClip(null);
    g.setColor(new Color(0, 0, 0, 70));
    g.setStroke(new BasicStroke(0.5f));
    g.draw(shape);
    g.dispose();
    return img;
  }

  private static void trafficLights(Graphics2D g, boolean active) {
    Color[][] colors = {
      {new Color(0xFF5F57), new Color(0xE2463F)},
      {new Color(0xFEBC2E), new Color(0xE1A116)},
      {new Color(0x28C840), new Color(0x1AAB29)}
    };
    for (int i = 0; i < 3; i++) {
      Ellipse2D dot = new Ellipse2D.Float(MARGIN_X + 12 + i * 20, MARGIN_TOP + 8, 12, 12);
      g.setColor(active ? colors[i][0] : new Color(0xDCDCDC));
      g.fill(dot);
      g.setColor(active ? colors[i][1] : new Color(0xC8C8C8));
      g.setStroke(new BasicStroke(0.5f));
      g.draw(dot);
    }
  }

  private static void titleText(Graphics2D g, String title, Color color, int w) {
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(new Font("Helvetica Neue", Font.BOLD, 13));
    FontMetrics fm = g.getFontMetrics();
    g.setColor(color);
    int x = MARGIN_X + (w - fm.stringWidth(title)) / 2;
    int y = MARGIN_TOP + (TITLE_BAR - fm.getHeight()) / 2 + fm.getAscent();
    g.drawString(title, x, y);
  }

  /** A soft drop shadow, blurred at low resolution and scaled up. */
  private static BufferedImage shadow(int w, int h, int outW, int outH) {
    int s = 2;
    BufferedImage img = new BufferedImage(outW / s, outH / s, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setComposite(AlphaComposite.SrcOver.derive(0.38f));
    g.setColor(Color.BLACK);
    g.fill(
        new RoundRectangle2D.Float(
            (float) MARGIN_X / s, (MARGIN_TOP + 8f) / s, (float) w / s, (float) h / s, 10, 10));
    g.dispose();
    return blur(blur(img, true), false);
  }

  private static BufferedImage blur(BufferedImage src, boolean horizontal) {
    int radius = 8;
    float[] weights = new float[radius * 2 + 1];
    float sum = 0;
    for (int i = -radius; i <= radius; i++) {
      weights[i + radius] = (float) Math.exp(-(i * i) / (2.0 * 16));
      sum += weights[i + radius];
    }
    for (int i = 0; i < weights.length; i++) {
      weights[i] /= sum;
    }
    Kernel kernel =
        horizontal ? new Kernel(weights.length, 1, weights) : new Kernel(1, weights.length, weights);
    return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(src, null);
  }

  // ---- utilities --------------------------------------------------------------------------------

  private static void layout(Container c) {
    c.doLayout();
    for (Component child : c.getComponents()) {
      if (child instanceof Container nested) {
        layout(nested);
      }
    }
  }

  private static void save(String name, BufferedImage img) throws Exception {
    ImageIO.write(img, "png", new File(out, name));
    System.out.println("wrote " + name + " " + img.getWidth() + "x" + img.getHeight());
  }
}
