package com.bobon.mypace

// app/src/test/java/com/example/myapp/DistanceFormatterTest.kt

import org.junit.Assert.assertEquals
// app/src/test/java/com/example/myapp/DistanceFormatterSpecificTest.kt

import org.junit.Test
import org.junit.Assert.assertEquals
import java.math.BigDecimal
import java.math.RoundingMode

class DistanceFormatterSpecificTest {

    // Твой оригинальный код
    private fun formatOriginal(totalDistance: Double): String {
        if (totalDistance <= 0) return "0.00"
        if (totalDistance <= 10) return "0.01"
        return BigDecimal(totalDistance / 1000)
            .setScale(2, RoundingMode.DOWN)
            .toString()
    }

    // Фикс через valueOf
    private fun formatFixed(totalDistance: Double): String {
        if (totalDistance <= 0) return "0.00"
        if (totalDistance <= 10) return "0.01"
        return BigDecimal.valueOf(totalDistance / 1000)
            .setScale(2, RoundingMode.DOWN)
            .toString()
    }

    @Test
    fun `debug - what Double looks like for 30 meters`() {
        val d = 30.0 / 1000
        println("30.0 / 1000 = $d")
        println("toString = ${d.toString()}")
        println("BigDecimal(d) = ${BigDecimal(d)}")
        println("BigDecimal.valueOf(d) = ${BigDecimal.valueOf(d)}")
    }

    @Test
    fun `30 meters - original vs fixed`() {
        val orig = formatOriginal(30.0)
        val fixed = formatFixed(30.0)
        println("Original: $orig")
        println("Fixed: $fixed")
        assertEquals("0.03", orig)  // упадёт, покажет реальное значение
    }

    @Test
    fun `60 meters - original vs fixed`() {
        val orig = formatOriginal(60.0)
        val fixed = formatFixed(60.0)
        println("Original: $orig")
        println("Fixed: $fixed")
        assertEquals("0.06", orig)
    }

    @Test
    fun `all problematic values from 1 to 1000`() {
        val expectedErrors = listOf(30, 60, 90, 120, 150, 180, 210, 240, 290, 300)

        for (m in expectedErrors) {
            val orig = formatOriginal(m.toDouble())
            val fixed = formatFixed(m.toDouble())
            println("$m м: original=$orig fixed=$fixed")
        }
    }
}