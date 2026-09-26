package markdown;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code #tags} in note text. A tag starts with a letter or underscore right after a {@code #} that
 * does not follow a word character, another {@code #} or a slash — so {@code C#}, {@code ##
 * heading} and {@code site.com/#anchor} are not tags.
 */
public final class Tags {

  private static final Pattern TAG =
      Pattern.compile("(?<![\\p{L}\\p{N}_#/&])#([\\p{L}_][\\p{L}\\p{N}_-]*)");

  private Tags() {}

  /** {@code [start, end)} of every tag including its {@code #}. */
  public static List<int[]> find(String text) {
    List<int[]> tags = new ArrayList<>();
    Matcher m = TAG.matcher(text);
    while (m.find()) {
      tags.add(new int[] {m.start(), m.end()});
    }
    return tags;
  }

  /** The distinct tags of {@code text}, lower-cased, without {@code #}, in order of appearance. */
  public static Set<String> of(String text) {
    Set<String> tags = new LinkedHashSet<>();
    Matcher m = TAG.matcher(text);
    while (m.find()) {
      tags.add(m.group(1).toLowerCase(Locale.ROOT));
    }
    return tags;
  }
}
