package it.gromov.zscore.platform

import java.net.InetSocketAddress
import java.net.SocketAddress

object IpUtil {

    private val IPV4_PATTERN = Regex("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$")
    private const val IPV4_MAPPED_PREFIX = "::ffff:"
    private const val MAX_ADDRESS_LENGTH = 45

    fun hostAddressOf(address: SocketAddress?): String? {
        val inetSocketAddress = address as? InetSocketAddress ?: return null
        val inetAddress = inetSocketAddress.address ?: return null
        val normalized = normalize(inetAddress.hostAddress ?: return null)
        return if (isValid(normalized)) normalized else null
    }

    fun normalize(rawAddress: String): String {
        val withoutZone = stripZoneId(rawAddress)
        return if (withoutZone.startsWith(IPV4_MAPPED_PREFIX, ignoreCase = true)) {
            withoutZone.substring(IPV4_MAPPED_PREFIX.length)
        } else {
            withoutZone
        }
    }

    fun isValid(address: String): Boolean {
        if (address.isBlank() || address.length > MAX_ADDRESS_LENGTH) {
            return false
        }
        return if (address.contains('.')) isValidIpv4(address) else isPlausibleIpv6(address)
    }

    private fun stripZoneId(rawAddress: String): String {
        val zoneIndex = rawAddress.indexOf('%')
        return if (zoneIndex >= 0) rawAddress.substring(0, zoneIndex) else rawAddress
    }

    private fun isValidIpv4(address: String): Boolean {
        val match = IPV4_PATTERN.matchEntire(address) ?: return false
        return match.groupValues.drop(1).all { octet ->
            val value = octet.toIntOrNull()
            value != null && value in 0..255 && (octet == "0" || !octet.startsWith("0"))
        }
    }

    private fun isPlausibleIpv6(address: String): Boolean {
        val body = address.removePrefix("[").removeSuffix("]")
        if (!body.contains(':')) {
            return false
        }
        val allowedChars = "0123456789abcdefABCDEF:"
        return body.all { it in allowedChars }
    }
}
