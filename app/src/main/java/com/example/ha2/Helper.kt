package com.example.ha2

import java.math.BigInteger
import java.security.MessageDigest

class Helper {

//    fun sha1(text: String): String {
//        val bytes = text.toByteArray()
//        val md = MessageDigest.getInstance("SHA-1")
//        val digest = md.digest(bytes)
//        var result = ""
//        for (byte in digest) {
//            result = result.plus("%02x".format(byte))
//        }
//        return result
//    }

    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    fun sha1(input: String) = hashString("SHA-1", input)

    fun md5(text: String): String {
        return toMD5Hash(text)
    }

    private fun byteArrayToHexString( array: Array<Byte> ): String {
        var result = StringBuilder(array.size * 2)
        for ( byte in array ) {
            val toAppend =
                String.format("%2X", byte).replace(" ", "0") // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'

        return result.toString()
    }

    private fun toMD5Hash( text: String ): String {
        var result = ""
        try {
            val md5 = MessageDigest.getInstance("MD5")
            val md5HashBytes = md5.digest(text.toByteArray()).toTypedArray()
            result = byteArrayToHexString(md5HashBytes)
        }
        catch ( e: Exception ) {
            result = "error: ${e.message}"
        }
        return result.toLowerCase().replace("-", "")
    }

    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789abcdef"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}