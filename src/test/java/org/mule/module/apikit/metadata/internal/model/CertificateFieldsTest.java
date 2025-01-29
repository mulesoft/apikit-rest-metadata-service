/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CertificateFieldsTest {

  @Test
  public void testEnumValues() {
    assertEquals(3, CertificateFields.values().length);
    assertNotNull(CertificateFields.valueOf("CLIENT_CERTIFICATE_ENCODED"));
    assertNotNull(CertificateFields.valueOf("CLIENT_CERTIFICATE_PUBLIC_KEY"));
    assertNotNull(CertificateFields.valueOf("CLIENT_CERTIFICATE_TYPE"));
  }

  @Test
  public void testClientCertificateEncoded() {
    assertEquals("encoded", CertificateFields.CLIENT_CERTIFICATE_ENCODED.getName());
  }

  @Test
  public void testClientCertificatePublicKey() {
    assertEquals("publicKey", CertificateFields.CLIENT_CERTIFICATE_PUBLIC_KEY.getName());
  }

  @Test
  public void testClientCertificateType() {
    assertEquals("type", CertificateFields.CLIENT_CERTIFICATE_TYPE.getName());
  }

  @Test
  public void testGetName() {
    for (CertificateFields field : CertificateFields.values()) {
      assertNotNull(field.getName());
      assertFalse(field.getName().isEmpty());
    }
  }

  @Test
  public void testUniqueness() {
    String[] names = new String[CertificateFields.values().length];
    int i = 0;
    for (CertificateFields field : CertificateFields.values()) {
      assertFalse(contains(names, field.getName()), "Duplicate name found: " + field.getName());
      names[i++] = field.getName();
    }
  }

  private boolean contains(String[] array, String value) {
    for (String s : array) {
      if (value.equals(s)) {
        return true;
      }
    }
    return false;
  }
}
