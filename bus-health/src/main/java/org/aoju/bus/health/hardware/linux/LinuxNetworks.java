/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.linux;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.hardware.AbstractNetworks;
import org.aoju.bus.health.hardware.NetworkIF;

/**
 * <p>
 * LinuxNetworks class.
 * </p>
 *
 * @author Kimi Liu
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 * @since JDK 1.8+
 */
public class LinuxNetworks extends AbstractNetworks {

    /**
     * Updates interface network statistics on the given interface. Statistics
     * include packets and bytes sent and received, and interface speed.
     *
     * @param netIF The interface on which to update statistics
     */
    public static void updateNetworkStats(NetworkIF netIF) {
        String txBytesPath = String.format("/sys/class/net/%s/statistics/tx_bytes", netIF.getName());
        String rxBytesPath = String.format("/sys/class/net/%s/statistics/rx_bytes", netIF.getName());
        String txPacketsPath = String.format("/sys/class/net/%s/statistics/tx_packets", netIF.getName());
        String rxPacketsPath = String.format("/sys/class/net/%s/statistics/rx_packets", netIF.getName());
        String txErrorsPath = String.format("/sys/class/net/%s/statistics/tx_errors", netIF.getName());
        String rxErrorsPath = String.format("/sys/class/net/%s/statistics/rx_errors", netIF.getName());
        String speed = String.format("/sys/class/net/%s/speed", netIF.getName());

        netIF.setTimeStamp(System.currentTimeMillis());
        netIF.setBytesSent(Builder.getUnsignedLongFromFile(txBytesPath));
        netIF.setBytesRecv(Builder.getUnsignedLongFromFile(rxBytesPath));
        netIF.setPacketsSent(Builder.getUnsignedLongFromFile(txPacketsPath));
        netIF.setPacketsRecv(Builder.getUnsignedLongFromFile(rxPacketsPath));
        netIF.setOutErrors(Builder.getUnsignedLongFromFile(txErrorsPath));
        netIF.setInErrors(Builder.getUnsignedLongFromFile(rxErrorsPath));
        long netSpeed = Builder.getUnsignedLongFromFile(speed) * 1024 * 1024;
        netIF.setSpeed(netSpeed < 0 ? 0 : netSpeed);
    }

}
