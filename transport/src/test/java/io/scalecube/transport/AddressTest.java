package io.scalecube.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AddressTest extends BaseTest {

  @Test
  public void testParseHostPort() throws Exception {
    Address address1 = Address.from("localhost:5810");
    assertEquals(5810, address1.port());
    assertEquals(Address.getLocalIpAddress().getHostAddress(), address1.host());

    Address address2 = Address.from("127.0.0.1:5810");
    assertEquals(5810, address1.port());
    assertEquals(Address.getLocalIpAddress().getHostAddress(), address2.host());

    assertEquals(address1, address2);
  }

  @Test
  public void testParseUnknownHostPort() throws Exception {
    Address address = Address.from("host:1111");
    assertEquals(1111, address.port());
    assertEquals("host", address.host());
  }

  @Test
  public void testParseHostPortRange() throws Exception {
    List<Address> addresses = Address.fromRange("localhost:5810..5812");
    assertEquals(3, addresses.size());
    assertTrue(addresses.contains(Address.create("localhost", 5810)));
    assertTrue(addresses.contains(Address.create("localhost", 5811)));
    assertTrue(addresses.contains(Address.create("localhost", 5812)));
  }
}
