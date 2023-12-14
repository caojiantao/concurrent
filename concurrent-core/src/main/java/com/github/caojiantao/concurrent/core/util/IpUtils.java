package com.github.caojiantao.concurrent.core.util;

import lombok.SneakyThrows;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IpUtils {

    private static String ip = null;

    @SneakyThrows
    public static String getHostIp() {
        if (ip == null) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address
                            && inetAddress.isSiteLocalAddress()
                            && !"127.0.0.1".equals(inetAddress.getHostAddress())) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        }
        return ip;
    }
}
