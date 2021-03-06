package com.contentful.java.cda;

import com.contentful.java.cda.lib.Enqueue;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.contentful.java.cda.ResourceFactory.fromArrayToItems;
import static com.google.common.truth.Truth.assertThat;

public class LocaleFallbackTest extends BaseTest {
  @Test
  @Enqueue(
      defaults = {"locales_fallback/fetch_all_locales.json", "locales_fallback/content_types.json"},
      value = "locales_fallback/fetch_all_locales.json"
  )
  public void fetchLocales() throws Exception {
    final List<CDALocale> locales = fromArrayToItems(client.fetch(CDALocale.class).all());
    Map<String, CDALocale> localesByCodeMap = listToMapByNamer(locales, localeNamer);

    assertThat(localesByCodeMap).hasSize(4);
    assertThat(localesByCodeMap.get("default").fallbackLocaleCode()).isNull();
    assertThat(localesByCodeMap.get("default").isDefaultLocale()).isTrue();

    assertThat(localesByCodeMap.get("first").fallbackLocaleCode()).isEqualTo("inbetween");

    assertThat(localesByCodeMap.get("inbetween").fallbackLocaleCode()).isEqualTo("default");

    assertThat(localesByCodeMap.get("null").fallbackLocaleCode()).isNull();
  }

  @Test
  @Enqueue(
      defaults = {"locales_fallback/fetch_all_locales.json", "locales_fallback/content_types.json"},
      value = "locales_fallback/entries.json"
  )
  public void testFallbackLocaleQueueToDefaultOneHop() throws Exception {
    final CDAArray all = client.fetch(CDAEntry.class).all();
    final Map<String, CDAEntry> entries = all.entries();

    final CDAEntry nofirst = entries.get("no-first");
    assertThat(nofirst.getField("title")).isEqualTo("no-first");

    assertThat(nofirst.getField("first", "title")).isEqualTo("inbetween");

    assertThat(nofirst.getField("inbetween", "title")).isEqualTo("inbetween");

    assertThat(nofirst.getField("default", "title")).isEqualTo("no-first");
  }

  @Test
  @Enqueue(
      defaults = {"locales_fallback/fetch_all_locales.json", "locales_fallback/content_types.json"},
      value = "locales_fallback/entries.json"
  )
  public void testFallbackLocaleQueueToDefaultTwoHops() throws Exception {
    final CDAArray all = client.fetch(CDAEntry.class).all();
    final Map<String, CDAEntry> entries = all.entries();

    final CDAEntry subject = entries.get("no-first-and-no-inbetween");
    assertThat(subject.getField("title")).isEqualTo("no-first-and-no-inbetween");

    assertThat(subject.getField("first", "title")).isEqualTo("no-first-and-no-inbetween");

    assertThat(subject.getField("inbetween", "title")).isEqualTo("no-first-and-no-inbetween");

    assertThat(subject.getField("default", "title")).isEqualTo("no-first-and-no-inbetween");
  }

  @Test
  @Enqueue(
      defaults = {"locales_fallback/fetch_all_locales.json", "locales_fallback/content_types.json"},
      value = "locales_fallback/entries.json"
  )
  public void testFallbackLocaleQueueToNull() throws Exception {
    final CDAArray all = client.fetch(CDAEntry.class).all();
    final Map<String, CDAEntry> entries = all.entries();

    final CDAEntry toNull = entries.get("no-null");
    assertThat(toNull.getField("title")).isEqualTo("no-null");

    assertThat(toNull.getField("null", "title")).isNull();
  }


  private static Namer<CDALocale> localeNamer = new Namer<CDALocale>() {
    @Override public String name(CDALocale cdaLocale) {
      return cdaLocale.code();
    }
  };

  private interface Namer<T> {
    String name(T t);
  }

  private <T> Map<String, T> listToMapByNamer(List<T> list, Namer<T> namer) {
    final HashMap<String, T> map = new HashMap<String, T>();

    for (T item : list) {
      final String key = namer.name(item);
      if (map.containsKey(key)) {
        throw new IllegalStateException("Locale Code should not be present twice!");
      }

      map.put(key, item);
    }

    return map;
  }

}
