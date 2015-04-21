package kotlin

//
// NOTE THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

import java.util.*

import java.util.Collections // TODO: it's temporary while we have java.util.Collections in js

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun <T: Comparable<T>> T.coerceAtLeast(minimumValue: T): T {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Byte.coerceAtLeast(minimumValue: Byte): Byte {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Double.coerceAtLeast(minimumValue: Double): Double {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Float.coerceAtLeast(minimumValue: Float): Float {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Int.coerceAtLeast(minimumValue: Int): Int {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Long.coerceAtLeast(minimumValue: Long): Long {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Short.coerceAtLeast(minimumValue: Short): Short {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun <T: Comparable<T>> T.coerceAtMost(maximumValue: T): T {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Byte.coerceAtMost(maximumValue: Byte): Byte {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Double.coerceAtMost(maximumValue: Double): Double {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Float.coerceAtMost(maximumValue: Float): Float {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Int.coerceAtMost(maximumValue: Int): Int {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Long.coerceAtMost(maximumValue: Long): Long {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 * @return this value if it's greater than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Short.coerceAtMost(maximumValue: Short): Short {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun <T: Comparable<T>> T.coerceIn(minimumValue: T?, maximumValue: T?): T {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Byte.coerceIn(minimumValue: Byte?, maximumValue: Byte?): Byte {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Double.coerceIn(minimumValue: Double?, maximumValue: Double?): Double {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Float.coerceIn(minimumValue: Float?, maximumValue: Float?): Float {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Int.coerceIn(minimumValue: Int?, maximumValue: Int?): Int {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Long.coerceIn(minimumValue: Long?, maximumValue: Long?): Long {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Short.coerceIn(minimumValue: Short?, maximumValue: Short?): Short {
    if (minimumValue !== null && maximumValue !== null) {
        if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
        if (this < minimumValue) return minimumValue
        if (this > maximumValue) return maximumValue
    }
    else {
        if (minimumValue !== null && this < minimumValue) return minimumValue
        if (maximumValue !== null && this > maximumValue) return maximumValue
    }
    return this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun <T: Comparable<T>> T.coerceIn(range: Range<T>): T {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Byte.coerceIn(range: ByteRange): Byte {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Double.coerceIn(range: DoubleRange): Double {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Float.coerceIn(range: FloatRange): Float {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Int.coerceIn(range: IntRange): Int {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Long.coerceIn(range: LongRange): Long {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

/**
 * Ensures that this value lies in the specified [range].
 * @return this value if it's in the [range], or range.start if this value is less than range.start, or range.end if this value is greater than range.end.
 */
public fun Short.coerceIn(range: ShortRange): Short {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return if (this < range.start) range.start else if (this > range.end) range.end else this
}

