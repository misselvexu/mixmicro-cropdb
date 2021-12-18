/*
 * Copyright (c) 2021-2022. CropDB author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.vopen.framework.cropdb.common.util;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This is not a string but a CharSequence that can be cleared of its memory. Important for handling
 * passwords. Represents text that should be kept confidential, such as by deleting it from computer
 * memory when no longer needed or garbage collected.
 *
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class SecureString implements CharSequence {

  private final int[] chars;
  private final int[] pad;

  public SecureString(final CharSequence original) {
    this(0, original.length(), original);
  }

  public SecureString(final int start, final int end, final CharSequence original) {
    final int length = end - start;
    pad = new int[length];
    chars = new int[length];
    scramble(start, length, original);
  }

  @Override
  public char charAt(final int i) {
    return (char) (pad[i] ^ chars[i]);
  }

  @Override
  public int length() {
    return chars.length;
  }

  @Override
  public CharSequence subSequence(final int start, final int end) {
    return new SecureString(start, end, this);
  }

  /**
   * Convert array back to String but not using toString(). See toString() docs below.
   *
   * @return the string
   */
  public String asString() {
    final char[] value = new char[chars.length];
    for (int i = 0; i < value.length; i++) {
      value[i] = charAt(i);
    }
    return new String(value);
  }

  /** Manually clear the underlying array holding the characters */
  public void clear() {
    Arrays.fill(chars, '0');
    Arrays.fill(pad, 0);
  }

  /**
   * Protect against using this class in log statements.
   *
   * <p>{@inheritDoc}
   */
  @Override
  public String toString() {
    return "Secure:XXXXX";
  }

  /**
   * Randomly pad the characters to not store the real character in memory.
   *
   * @param start start of the {@code CharSequence}
   * @param length length of the {@code CharSequence}
   * @param characters the {@code CharSequence} to scramble
   */
  private void scramble(final int start, final int length, final CharSequence characters) {
    final SecureRandom random = new SecureRandom();
    for (int i = start; i < length; i++) {
      final char charAt = characters.charAt(i);
      pad[i] = random.nextInt();
      chars[i] = pad[i] ^ charAt;
    }
  }
}
