package be.wegenenverkeer.rxhttpclient;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Response;
import java.nio.charset.Charset;
import java.util.*;

import static org.asynchttpclient.util.MiscUtils.isNonEmpty;

/**
 * A set of utility functions to assist in the migrations from
 * AHC 1.9 to AHC 2.6
 * Created by Karel Maesen, Geovise BVBA on 2018-12-13.
 */
public class CompatUtilities {


    /**
     * Returns the headers as an immutable map
     *
     * @param headers
     * @return
     */
    public static Map<String, List<String>> headersToMap(HttpHeaders headers) {
        //We need the FluentCaseInsensitiveMap from AHC 1.0 to keep same result in AwsSigner. (esp. w.r.t.
        // duplicate headers
        Map<String, List<String>> result = new FluentCaseInsensitiveStringsMap();
        for (String header : headers.names() ) {
            result.put(header, headers.getAll(header));
        }
        return result;
    }

    public static String bodyExcerpt(Response response, int maxLength) {
        return bodyExcerpt(response, maxLength, "UTF-8");
    }

    public static String bodyExcerpt(Response response, int maxLength, String charset) {
        String responseText = response.getResponseBody(from(charset));
        return responseText.length() <= maxLength ? responseText : responseText.substring(0, maxLength);
    }

    private static Charset from(String charset) {
        return Charset.forName(charset);
    }
}

/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/**
 * An implementation of a {@code String -> List<String>} map that adds a fluent interface, i.e. methods that
 * return this instance. This class differs from {@code FluentStringsMap} in that keys are treated in an
 * case-insensitive matter, i.e. case of the key doesn't matter when retrieving values or changing the map.
 * However, the map preserves the key case (of the first insert or replace) and returns the keys in their
 * original case in the appropriate methods (e.g. {@link FluentCaseInsensitiveStringsMap#keySet()}).
 */
class FluentCaseInsensitiveStringsMap implements Map<String, List<String>>, Iterable<Map.Entry<String, List<String>>> {
    private final Map<String, List<String>> values = new LinkedHashMap<>();
    private final Map<String, String> keyLookup = new LinkedHashMap<>();

    public FluentCaseInsensitiveStringsMap() {
    }

    public FluentCaseInsensitiveStringsMap(FluentCaseInsensitiveStringsMap src) {
        if (src != null) {
            for (Map.Entry<String, List<String>> header : src) {
                add(header.getKey(), header.getValue());
            }
        }
    }

    public FluentCaseInsensitiveStringsMap(Map<String, Collection<String>> src) {
        if (src != null) {
            for (Map.Entry<String, Collection<String>> header : src.entrySet()) {
                add(header.getKey(), header.getValue());
            }
        }
    }

    public FluentCaseInsensitiveStringsMap add(String key, String value) {
        if (key != null) {
            String lcKey = key.toLowerCase(Locale.ENGLISH);
            String realKey = keyLookup.get(lcKey);

            List<String> curValues = null;
            if (realKey == null) {
                keyLookup.put(lcKey, key);
                curValues = new ArrayList<>();
                values.put(key, curValues);
            } else {
                curValues = values.get(realKey);
            }

            String nonNullValue = value != null? value : "";
            curValues.add(nonNullValue);
        }
        return this;
    }

    /**
     * Adds the specified values and returns this object.
     *
     * @param key    The key
     * @param values The value(s); if the array is null then this method has no effect. Individual null values are turned into empty strings
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap add(String key, String... values) {
        if (isNonEmpty(values)) {
            add(key, Arrays.asList(values));
        }
        return this;
    }

    private List<String> fetchValues(Collection<String> values) {
        List<String> result = null;

        if (values != null) {
            for (String value : values) {
                if (value == null) {
                    value = "";
                }
                if (result == null) {
                    // lazy initialization
                    result = new ArrayList<>();
                }
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Adds the specified values and returns this object.
     *
     * @param key    The key
     * @param values The value(s); if null then this method has no effect. Use an empty collection
     *               to generate an empty value
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap add(String key, Collection<String> values) {
        if (key != null) {
            List<String> nonNullValues = fetchValues(values);

            if (nonNullValues != null) {
                String lcKey = key.toLowerCase(Locale.ENGLISH);
                String realKey = keyLookup.get(lcKey);
                List<String> curValues = null;

                if (realKey == null) {
                    realKey = key;
                    keyLookup.put(lcKey, key);
                } else {
                    curValues = this.values.get(realKey);
                }

                if (curValues == null) {
                    curValues = new ArrayList<>();
                    this.values.put(realKey, curValues);
                }
                curValues.addAll(nonNullValues);
            }
        }
        return this;
    }

    /**
     * Adds all key-values pairs from the given object to this object and returns this object.
     *
     * @param src The source object
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap addAll(FluentCaseInsensitiveStringsMap src) {
        if (src != null) {
            for (Map.Entry<String, List<String>> header : src) {
                add(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Adds all key-values pairs from the given map to this object and returns this object.
     *
     * @param src The source map
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap addAll(Map<String, Collection<String>> src) {
        if (src != null) {
            for (Map.Entry<String, Collection<String>> header : src.entrySet()) {
                add(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Replaces the values for the given key with the given values.
     *
     * @param key    The key
     * @param values The new values
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap replaceWith(final String key, final String... values) {
        return replaceWith(key, Arrays.asList(values));
    }

    /**
     * Replaces the values for the given key with the given values.
     *
     * @param key    The key
     * @param values The new values
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap replaceWith(final String key, final Collection<String> values) {
        if (key != null) {
            List<String> nonNullValues = fetchValues(values);
            String lcKkey = key.toLowerCase(Locale.ENGLISH);
            String realKey = keyLookup.get(lcKkey);

            if (nonNullValues == null) {
                keyLookup.remove(lcKkey);
                if (realKey != null) {
                    this.values.remove(realKey);
                }
            } else {
                if (!key.equals(realKey)) {
                    keyLookup.put(lcKkey, key);
                    this.values.remove(realKey);
                }
                this.values.put(key, nonNullValues);
            }
        }
        return this;
    }

    /**
     * Replace the values for all keys from the given map that are also present in this object, with the values from the given map.
     * All key-values from the given object that are not present in this object, will be added to it.
     *
     * @param src The source object
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap replaceAll(FluentCaseInsensitiveStringsMap src) {
        if (src != null) {
            for (Map.Entry<String, List<String>> header : src) {
                replaceWith(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Replace the values for all keys from the given map that are also present in this object, with the values from the given map.
     * All key-values from the given object that are not present in this object, will be added to it.
     *
     * @param src The source map
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap replaceAll(Map<? extends String, ? extends Collection<String>> src) {
        if (src != null) {
            for (Map.Entry<? extends String, ? extends Collection<String>> header : src.entrySet()) {
                replaceWith(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> put(String key, List<String> value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }

        List<String> oldValue = get(key);

        replaceWith(key, value);
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends String, ? extends List<String>> values) {
        replaceAll(values);
    }

    /**
     * Removes the values for the given key if present and returns this object.
     *
     * @param key The key
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap delete(String key) {
        if (key != null) {
            String lcKey = key.toLowerCase(Locale.ENGLISH);
            String realKey = keyLookup.remove(lcKey);

            if (realKey != null) {
                values.remove(realKey);
            }
        }
        return this;
    }

    /**
     * Removes the values for the given keys if present and returns this object.
     *
     * @param keys The keys
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap deleteAll(String... keys) {
        if (keys != null) {
            for (String key : keys) {
                remove(key);
            }
        }
        return this;
    }

    /**
     * Removes the values for the given keys if present and returns this object.
     *
     * @param keys The keys
     * @return This object
     */
    public FluentCaseInsensitiveStringsMap deleteAll(Collection<String> keys) {
        if (keys != null) {
            for (String key : keys) {
                remove(key);
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> remove(Object key) {
        if (key == null) {
            return null;
        } else {
            List<String> oldValues = get(key.toString());

            delete(key.toString());
            return oldValues;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        keyLookup.clear();
        values.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Map.Entry<String, List<String>>> iterator() {
        return Collections.unmodifiableSet(values.entrySet()).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        return new LinkedHashSet<>(keyLookup.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return values.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return values.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return key == null ? false : keyLookup.containsKey(key.toString().toLowerCase(Locale.ENGLISH));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    /**
     * Returns the value for the given key. If there are multiple values for this key,
     * then only the first one will be returned.
     *
     * @param key The key
     * @return The first value
     */
    public String getFirstValue(String key) {
        List<String> values = get(key);

        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }

    /**
     * Returns the values for the given key joined into a single string using the given delimiter.
     *
     * @param key The key
     * @return The value as a single string
     */
    public String getJoinedValue(String key, String delimiter) {
        List<String> values = get(key);

        if (values.isEmpty()) {
            return null;
        } else if (values.size() == 1) {
            return values.get(0);
        } else {
            StringBuilder result = new StringBuilder();

            for (String value : values) {
                if (result.length() > 0) {
                    result.append(delimiter);
                }
                result.append(value);
            }
            return result.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> get(Object key) {
        if (key == null)
            return Collections.emptyList();

        String lcKey = key.toString().toLowerCase(Locale.ENGLISH);
        String realKey = keyLookup.get(lcKey);

        return realKey != null ? values.get(realKey) : Collections.<String> emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<List<String>> values() {
        return values.values();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final FluentCaseInsensitiveStringsMap other = (FluentCaseInsensitiveStringsMap) obj;

        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values == null ? 0 : values.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append("\"");
            result.append(entry.getKey());
            result.append("=");

            boolean needsComma = false;

            for (String value : entry.getValue()) {
                if (needsComma) {
                    result.append(", ");
                } else {
                    needsComma = true;
                }
                result.append(value);
            }
            result.append("\"");
        }
        return result.toString();
    }
}