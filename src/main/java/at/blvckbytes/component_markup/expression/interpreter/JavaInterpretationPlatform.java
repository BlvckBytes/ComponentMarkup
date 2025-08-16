/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.util.AsciiCasing;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class JavaInterpretationPlatform implements InterpretationPlatform {

  public static final JavaInterpretationPlatform INSTANCE = new JavaInterpretationPlatform();

  private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
  private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
  private static final Pattern NON_WORDS = Pattern.compile("[^\\p{IsAlphabetic}\\d]+");
  private static final Pattern DASHES = Pattern.compile("(^-+)(|-+$)");

  private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final Map<String, Locale> LOCALE_BY_NAME_LOWER;

  private static final Map<String, Pattern> patternCache = new HashMap<>();
  private static final Map<String, DateTimeFormatter> dateTimeFormatterCache = new HashMap<>();


  static {
    LOCALE_BY_NAME_LOWER = new HashMap<>();

    for (Locale locale : Locale.getAvailableLocales()) {
      String localeName = locale.toString();

      for (int i = 0; i < localeName.length(); ++i) {
        if (!Character.isWhitespace(localeName.charAt(i))) {
          LOCALE_BY_NAME_LOWER.put(AsciiCasing.lower(localeName), locale);
          break;
        }
      }
    }
  }

  private JavaInterpretationPlatform() {}

  @Override
  public String[] split(String input, String delimiter, boolean regex) {
    Pattern pattern;

    try {
      if (!regex)
        pattern = Pattern.compile(delimiter, Pattern.LITERAL);
      else
        pattern = patternCache.computeIfAbsent(delimiter, Pattern::compile);
    } catch (Throwable e) {
      return null;
    }

    return pattern.split(input);
  }

  @Override
  public TriState matchesPattern(String input, String pattern) {
    try {
      return patternCache.computeIfAbsent(pattern, Pattern::compile).matcher(input).find()
        ? TriState.TRUE
        : TriState.FALSE;
    } catch (Throwable e) {
      return TriState.NULL;
    }
  }

  @Override
  public String toTitleCase(String input) {
    if (input.isEmpty())
      return input;

    Locale locale = Locale.ROOT;
    StringBuilder result = new StringBuilder(input.length());

    BreakIterator wordIterator = BreakIterator.getWordInstance(locale);
    wordIterator.setText(input);

    int start = wordIterator.first();
    for (int end = wordIterator.next(); end != BreakIterator.DONE; start = end, end = wordIterator.next()) {
      String word = input.substring(start, end);

      if (Character.isLetterOrDigit(word.codePointAt(0))) {
        int firstCodepoint = word.codePointAt(0);
        int firstCharLength = Character.charCount(firstCodepoint);

        result.appendCodePoint(Character.toTitleCase(firstCodepoint));
        result.append(word.substring(firstCharLength).toLowerCase(locale));
        continue;
      }

      result.append(word);
    }

    return result.toString();
  }

  @Override
  public FormatDateResult formatDate(String format, @Nullable String locale, @Nullable String timeZone, long timestamp) {
    EnumSet<FormatDateWarning> errors = EnumSet.noneOf(FormatDateWarning.class);
    Instant stamp = Instant.ofEpochMilli(timestamp);

    ZoneId zone = SYSTEM_ZONE;

    if (timeZone != null) {
      try {
        zone = ZoneId.of(timeZone);
      } catch (Throwable e1) {
        try {
          zone = ZoneId.of(timeZone, ZoneId.SHORT_IDS);
        } catch (Throwable e2) {
          errors.add(FormatDateWarning.INVALID_TIMEZONE);
        }
      }
    }

    Locale formatLocale = null;

    if (locale != null) {
      formatLocale = LOCALE_BY_NAME_LOWER.get(AsciiCasing.lower(locale));

      if (formatLocale == null)
        errors.add(FormatDateWarning.INVALID_LOCALE);
    }

    DateTimeFormatter formatter = DEFAULT_FORMATTER;

    if (format != null) {
      String formatIdentifier = format + (formatLocale == null ? "" : ("___" + formatLocale));
      DateTimeFormatter cachedFormatter = dateTimeFormatterCache.get(formatIdentifier);

      if (cachedFormatter != null)
        formatter = cachedFormatter;

      else {
        try {
          formatter = locale == null ? DateTimeFormatter.ofPattern(format) : DateTimeFormatter.ofPattern(format, formatLocale);
          dateTimeFormatterCache.put(formatIdentifier, formatter);
        } catch (Throwable e) {
          errors.add(FormatDateWarning.INVALID_FORMAT);
        }
      }
    }

    return new FormatDateResult(formatter.format(stamp.atZone(zone)), errors);
  }

  @Override
  public String slugify(String input) {
    String slug = NON_WORDS.matcher(input).replaceAll("-");
    slug = DASHES.matcher(slug).replaceAll("");
    return slug.toLowerCase();
  }

  @Override
  public String asciify(String input) {
    String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
    String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
    return NON_ASCII.matcher(withoutDiacritics).replaceAll("");
  }
}