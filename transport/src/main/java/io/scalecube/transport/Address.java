package io.scalecube.transport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import reactor.core.Exceptions;

public final class Address {

  private static final Pattern ADDRESS_FORMAT = Pattern.compile("(?<host>^.*):(?<port>\\d+$)");
  private static final Pattern ADDRESS_RANGE_FORMAT = Pattern.compile("(?<host>^.*):(?<ports>\\d+(\\.\\.)?\\d+$)");

  private String host;
  private int port;

  /** Instantiates empty address for deserialization purpose. */
  Address() {}

  private Address(String host, int port) {
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("host must be present");
    }
    if (port < 0) {
      throw new IllegalArgumentException("port must be eq or greater than 0");
    }
    this.host = convertIfLocalhost(host);
    this.port = port;
  }

  /**
   * Parses given host:port string to create Address instance.
   *
   * @param hostandport must come in form {@code host:port}
   */
  public static Address from(String hostandport) {
    if (hostandport == null || hostandport.isEmpty()) {
      throw new IllegalArgumentException("host-and-port string must be present");
    }

    Matcher matcher = ADDRESS_FORMAT.matcher(hostandport);
    if (!matcher.find()) {
      throw new IllegalArgumentException("can't parse host-and-port string from: " + hostandport);
    }

    String host = matcher.group(1);
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("can't parse host from: " + hostandport);
    }

    int port = parsePort(hostandport, matcher.group(2));
    return new Address(host, port);
  }

  private static int parsePort(String source, String portCandidate) {
    int port;
    try {
      port = Integer.parseInt(portCandidate);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("can't parse port from: " + source, ex);
    }
    return port;
  }

  /**
   * Parses given host:port..port string to create range of Address instances.

   * @param hostAndPortRange must come in from {@code host:port..port}
   */
  public static List<Address> fromRange(String hostAndPortRange) {
    if (hostAndPortRange == null || hostAndPortRange.isEmpty()) {
      throw new IllegalArgumentException("");
    }
    Matcher matcher = ADDRESS_RANGE_FORMAT.matcher(hostAndPortRange);
    if (!matcher.find()) {
      throw new IllegalArgumentException("can't parse range of addresses from: " + hostAndPortRange);
    }
    String host = matcher.group(1);
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("can't parse host from: " + hostAndPortRange);
    }
    String portGroup = matcher.group(2);
    if (portGroup.contains("..")) {
      String[] ports = portGroup.split("\\.\\.");
      int left = parsePort(hostAndPortRange, ports[0]);
      int right = parsePort(hostAndPortRange, ports[1]);
      if (left > right) {
        throw new IllegalArgumentException("left port must be lower then right" + hostAndPortRange);
      }
      List<Address> result = new ArrayList<>();
      for (int port = left; port <= right; port++) {
        result.add(new Address(host, port));
      }
      return result;
    } else {
      return Collections.singletonList(new Address(host, parsePort(hostAndPortRange, portGroup)));
    }
  }

  /** Creates address from host and port. */
  public static Address create(String host, int port) {
    return new Address(host, port);
  }

  /**
   * Getting local IP address by the address of local host. <b>NOTE:</b> returned IP address is
   * expected to be a publicly visible IP address.
   *
   * @throws RuntimeException wrapped {@link UnknownHostException} in case when local host name
   *     couldn't be resolved into an address.
   */
  public static InetAddress getLocalIpAddress() {
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      throw Exceptions.propagate(e);
    }
  }

  /**
   * Checks whether given host string is one of localhost variants. i.e. {@code 127.0.0.1}, {@code
   * 127.0.1.1} or {@code localhost}, and if so - then node's public IP address will be resolved and
   * returned.
   *
   * @param host host string
   * @return local ip address if given host is localhost
   */
  private static String convertIfLocalhost(String host) {
    String result;
    switch (host) {
      case "localhost":
      case "127.0.0.1":
      case "127.0.1.1":
        result = getLocalIpAddress().getHostAddress();
        break;
      default:
        result = host;
    }
    return result;
  }

  /**
   * Returns host.
   *
   * @return host
   */
  public String host() {
    return host;
  }

  /**
   * Returns port.
   *
   * @return port
   */
  public int port() {
    return port;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Address that = (Address) other;
    return Objects.equals(host, that.host) && Objects.equals(port, that.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port);
  }

  @Override
  public String toString() {
    return host + ":" + port;
  }
}
