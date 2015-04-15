package numbers

import org.junit.Test
import org.junit.Test as test
import kotlin.test.*

class CoercionTest {

    Test fun coercionsInt() {
        expect(5) { 5.atLeast(1) }
        expect(5) { 1.atLeast(5) }
        expect(1) { 5.atMost(1) }
        expect(1) { 1.atMost(5) }

        for (value in 0..10) {
            expect(value) { value.coerce(null, null) }
            expect(value.atLeast(2)) { value.coerce(2, null) }
            expect(value.atMost(5)) { value.coerce(null, 5) }
            expect(value.atLeast(2).atMost(5)) { value.coerce(2, 5) }
            expect(value.atMost(5).atLeast(2)) { value.coerce(2..5) }
        }

        fails { 1.coerce(1, 0) }
        fails { 1.coerce(1..0) }
    }

    Test fun coercionsLong() {
        expect(5L) { 5L.atLeast(1L) }
        expect(5L) { 1L.atLeast(5L) }
        expect(1L) { 5L.atMost(1L) }
        expect(1L) { 1L.atMost(5L) }

        for (value in 0L..10L) {
            expect(value) { value.coerce(null, null) }
            expect(value.atLeast(2L)) { value.coerce(2L, null) }
            expect(value.atMost(5L)) { value.coerce(null, 5L) }
            expect(value.atLeast(2L).atMost(5L)) { value.coerce(2L, 5L) }
            expect(value.atMost(5L).atLeast(2L)) { value.coerce(2L..5L) }
        }

        fails { 1L.coerce(1L, 0L) }
        fails { 1L.coerce(1L..0L) }

    }

    Test fun coercionsDouble() {
        expect(5.0) { 5.0.atLeast(1.0) }
        expect(5.0) { 1.0.atLeast(5.0) }
        expect(1.0) { 5.0.atMost(1.0) }
        expect(1.0) { 1.0.atMost(5.0) }

        for (value in 0.0..10.0) {
            expect(value) { value.coerce(null, null) }
            expect(value.atLeast(2.0)) { value.coerce(2.0, null) }
            expect(value.atMost(5.0)) { value.coerce(null, 5.0) }
            expect(value.atLeast(2.0).atMost(5.0)) { value.coerce(2.0, 5.0) }
            expect(value.atMost(5.0).atLeast(2.0)) { value.coerce(2.0..5.0) }
        }

        fails { 1.0.coerce(1.0, 0.0) }
        fails { 1.0.coerce(1.0..0.0) }
    }

    Test fun coercionsComparable() {
        val v = 0..10 map { ComparableNumber(it) }

        expect(5) { v[5].atLeast(v[1]).value }
        expect(5) { v[1].atLeast(v[5]).value }
        expect(v[5]) { v[5].atLeast(ComparableNumber(5)) }

        expect(1) { v[5].atMost(v[1]).value }
        expect(1) { v[1].atMost(v[5]).value }
        expect(v[1]) { v[1].atMost(ComparableNumber(1)) }

        for (value in v) {
            expect(value) { value.coerce(null, null) }
            expect(value.atLeast(v[2])) { value.coerce(v[2], null) }
            expect(value.atMost(v[5])) { value.coerce(null, v[5]) }
            expect(value.atLeast(v[2]).atMost(v[5])) { value.coerce(v[2], v[5]) }
            expect(value.atMost(v[5]).atLeast(v[2])) { value.coerce(v[2]..v[5]) }
        }

        fails { v[1].coerce(v[1], v[0]) }
        fails { v[1].coerce(v[1]..v[0]) }
    }
}

private class ComparableNumber(public val value: Int) : Comparable<ComparableNumber> {
    override fun compareTo(other: ComparableNumber): Int = this.value - other.value
    override fun toString(): String = "CV$value"
}