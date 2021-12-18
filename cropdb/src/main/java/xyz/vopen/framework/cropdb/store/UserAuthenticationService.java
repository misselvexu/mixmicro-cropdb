/*
 * Copyright (c) 2017-2021 Crop author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xyz.vopen.framework.cropdb.store;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.common.util.SecureString;
import xyz.vopen.framework.cropdb.exceptions.CropSecurityException;
import xyz.vopen.framework.cropdb.common.util.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static xyz.vopen.framework.cropdb.common.Constants.*;
import static xyz.vopen.framework.cropdb.common.util.StringUtils.isNullOrEmpty;

/**
 * User authentication service for crop.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Slf4j
public class UserAuthenticationService {
  private final SecureRandom random;
  private final CropStore<?> store;

  /**
   * Instantiates a new {@link UserAuthenticationService}.
   *
   * @param store the store
   */
  public UserAuthenticationService(CropStore<?> store) {
    this.store = store;
    this.random = new SecureRandom();
  }

  /**
   * Authenticates a user if the authentication data already exists in the database.
   *
   * @param username the username
   * @param password the password
   * @param existing indicates if authentication data is already existing
   */
  public void authenticate(String username, String password, boolean existing) {
    if (!StringUtils.isNullOrEmpty(password) && !StringUtils.isNullOrEmpty(username)) {
      if (!existing) {
        byte[] salt = getNextSalt();
        byte[] hash = hash(password.toCharArray(), salt);
        UserCredential userCredential = new UserCredential();
        userCredential.setPasswordHash(hash);
        userCredential.setPasswordSalt(salt);

        CropMap<String, UserCredential> userMap =
            store.openMap(USER_MAP, String.class, UserCredential.class);
        userMap.put(username, userCredential);
      } else {
        CropMap<String, UserCredential> userMap =
            store.openMap(USER_MAP, String.class, UserCredential.class);
        UserCredential userCredential = userMap.get(username);

        if (userCredential != null) {
          byte[] salt = userCredential.getPasswordSalt();
          byte[] expectedHash = userCredential.getPasswordHash();

          if (notExpectedPassword(password.toCharArray(), salt, expectedHash)) {
            throw new CropSecurityException("username or password is invalid");
          }
        } else {
          throw new CropSecurityException("username or password is invalid");
        }
      }
    } else if (existing) {
      if (store.hasMap(USER_MAP)) {
        throw new CropSecurityException("username or password is invalid");
      }
    }
  }

  /**
   * Adds or updates the password for a user in the authentication data.
   *
   * @param update the update
   * @param username the username
   * @param oldPassword the old password
   * @param newPassword the new password
   */
  public void addOrUpdatePassword(
      boolean update, String username, SecureString oldPassword, SecureString newPassword) {
    CropMap<String, UserCredential> userMap = null;

    if (update) {
      userMap = store.openMap(USER_MAP, String.class, UserCredential.class);
      UserCredential credential = userMap.get(username);

      if (credential != null) {
        byte[] salt = credential.getPasswordSalt();
        byte[] expectedHash = credential.getPasswordHash();

        if (notExpectedPassword(oldPassword.asString().toCharArray(), salt, expectedHash)) {
          throw new CropSecurityException("username or password is invalid");
        }
      }
    } else {
      if (store.hasMap(USER_MAP)) {
        throw new CropSecurityException("cannot add new credentials");
      }
    }

    if (userMap == null) {
      userMap = store.openMap(USER_MAP, String.class, UserCredential.class);
    }

    byte[] salt = getNextSalt();
    byte[] hash = hash(newPassword.asString().toCharArray(), salt);

    UserCredential userCredential = new UserCredential();
    userCredential.setPasswordHash(hash);
    userCredential.setPasswordSalt(salt);
    userMap.put(username, userCredential);
  }

  private byte[] getNextSalt() {
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  private byte[] hash(char[] password, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_KEY_LENGTH);
    Arrays.fill(password, Character.MIN_VALUE);
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return skf.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      log.error("Error while hashing password", e);
      throw new CropSecurityException("error while hashing a password: " + e.getMessage());
    } finally {
      spec.clearPassword();
    }
  }

  private boolean notExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
    byte[] pwdHash = hash(password, salt);
    Arrays.fill(password, Character.MIN_VALUE);
    if (pwdHash.length != expectedHash.length) return true;
    for (int i = 0; i < pwdHash.length; i++) {
      if (pwdHash[i] != expectedHash[i]) return true;
    }
    return false;
  }
}
