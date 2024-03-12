package uk.co.florisbooks.whenwinetastesbest

import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test


class DefaultWineDataManagerTest {

    private lateinit var dataManager: DefaultWineDataManager

    @Before
    fun setUp() {
        dataManager = DefaultWineDataManager(object : CsvDataRepo {
            override val availableYears: List<Int>
                get() = listOf(2016, 2017)

            override fun get(year: Int): String? {
                return when (year) {
                    2016 -> dec2016
                    2017 -> jan2017
                    else -> null
                }
            }
        })
    }

    @Test
    fun `Returns expected data for data with time zone offset`() {
        val start = DateTime(2017, 1, 15, 0, 0, DateTimeZone.forOffsetHours(1))
        assertThat(dataManager.getForDay(start))
                .isEqualTo(
                        mapOf(
                                start.plusHours(0) to WinePeriodType.FRUIT,
                                start.plusHours(1) to WinePeriodType.FRUIT,
                                start.plusHours(2) to WinePeriodType.FRUIT,
                                start.plusHours(3) to WinePeriodType.FRUIT,
                                start.plusHours(4) to WinePeriodType.FRUIT,
                                start.plusHours(5) to WinePeriodType.FRUIT,
                                start.plusHours(6) to WinePeriodType.FRUIT,
                                start.plusHours(7) to WinePeriodType.FRUIT,
                                start.plusHours(8) to WinePeriodType.FRUIT,
                                start.plusHours(9) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(10) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(11) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(12) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(13) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(14) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(15) to WinePeriodType.UNFAVOURABLE,
                                start.plusHours(16) to WinePeriodType.FRUIT,
                                start.plusHours(17) to WinePeriodType.FRUIT,
                                start.plusHours(18) to WinePeriodType.FRUIT,
                                start.plusHours(19) to WinePeriodType.FRUIT,
                                start.plusHours(20) to WinePeriodType.FRUIT,
                                start.plusHours(21) to WinePeriodType.FRUIT,
                                start.plusHours(22) to WinePeriodType.FRUIT,
                                start.plusHours(23) to WinePeriodType.FRUIT
                        ))
    }

    @Test
    fun `Time zone across year boundary`() {
        val start = DateTime(2017, 1, 1, 0, 0, DateTimeZone.forOffsetHours(10))
        assertThat(dataManager.getForDay(start))
                .isEqualTo(
                        mapOf(
                                start.plusHours(0) to WinePeriodType.ROOT,
                                start.plusHours(1) to WinePeriodType.ROOT,
                                start.plusHours(2) to WinePeriodType.ROOT,
                                start.plusHours(3) to WinePeriodType.ROOT,
                                start.plusHours(4) to WinePeriodType.ROOT,
                                start.plusHours(5) to WinePeriodType.ROOT,
                                start.plusHours(6) to WinePeriodType.ROOT,
                                start.plusHours(7) to WinePeriodType.ROOT,
                                start.plusHours(8) to WinePeriodType.ROOT,
                                start.plusHours(9) to WinePeriodType.FRUIT,
                                start.plusHours(10) to WinePeriodType.FRUIT,
                                start.plusHours(11) to WinePeriodType.FRUIT,
                                start.plusHours(12) to WinePeriodType.FRUIT,
                                start.plusHours(13) to WinePeriodType.FRUIT,
                                start.plusHours(14) to WinePeriodType.FRUIT,
                                start.plusHours(15) to WinePeriodType.ROOT,
                                start.plusHours(16) to WinePeriodType.ROOT,
                                start.plusHours(17) to WinePeriodType.ROOT,
                                start.plusHours(18) to WinePeriodType.ROOT,
                                start.plusHours(19) to WinePeriodType.ROOT,
                                start.plusHours(20) to WinePeriodType.ROOT,
                                start.plusHours(21) to WinePeriodType.ROOT,
                                start.plusHours(22) to WinePeriodType.ROOT,
                                start.plusHours(23) to WinePeriodType.ROOT
                        ))
    }

    @Test
    fun `Time zone with partial hours`() {
        val start = DateTime(2017, 1, 1, 0, 0, DateTimeZone.forOffsetHoursMinutes(0, 30))
        assertThat(dataManager.getForDay(start))
                .isEqualTo(
                        mapOf(
                                start.plusHours(0) to WinePeriodType.FRUIT,
                                start.plusHours(1) to WinePeriodType.FRUIT,
                                start.plusHours(2) to WinePeriodType.FRUIT,
                                start.plusHours(3) to WinePeriodType.FRUIT,
                                start.plusHours(4) to WinePeriodType.FRUIT,
                                start.plusHours(5) to WinePeriodType.FRUIT,
                                start.plusHours(6) to WinePeriodType.ROOT,
                                start.plusHours(7) to WinePeriodType.ROOT,
                                start.plusHours(8) to WinePeriodType.ROOT,
                                start.plusHours(9) to WinePeriodType.ROOT,
                                start.plusHours(10) to WinePeriodType.ROOT,
                                start.plusHours(11) to WinePeriodType.ROOT,
                                start.plusHours(12) to WinePeriodType.ROOT,
                                start.plusHours(13) to WinePeriodType.ROOT,
                                start.plusHours(14) to WinePeriodType.ROOT,
                                start.plusHours(15) to WinePeriodType.ROOT,
                                start.plusHours(16) to WinePeriodType.ROOT,
                                start.plusHours(17) to WinePeriodType.ROOT,
                                start.plusHours(18) to WinePeriodType.ROOT,
                                start.plusHours(19) to WinePeriodType.ROOT,
                                start.plusHours(20) to WinePeriodType.ROOT,
                                start.plusHours(21) to WinePeriodType.ROOT,
                                start.plusHours(22) to WinePeriodType.ROOT,
                                start.plusHours(23) to WinePeriodType.ROOT
                        ))
    }
}

private const val dec2016 = """1-12,st,L,L,L,L,L,L,L,F,F,F,F,F,F,F,F,F,R,R,R,R,R,R,R,R
2-12,nd,R,R,R,R,R,R,R,R,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
3-12,rd,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,R,R,R,R,R,R,R
4-12,th,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
5-12,th,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,W
6-12,th,W,W,W,W,W,W,W,W,W,W,W,W,W,W,U,U,U,U,U,U,U,U,U,U
7-12,th,U,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,L,L,L,L
8-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
9-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
10-12,th,L,L,L,L,L,L,L,L,L,L,L,L,F,F,F,F,F,F,F,F,F,F,F,F
11-12,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,L,L,L
12-12,th,L,L,L,L,L,L,L,L,L,L,L,U,U,U,U,U,U,U,U,U,U,U,U,U
13-12,th,U,U,U,U,U,U,U,U,U,U,U,U,U,R,R,R,R,R,R,R,R,R,R,R
14-12,th,R,R,R,R,R,R,R,R,R,R,R,R,W,W,W,W,W,W,W,W,W,W,W,W
15-12,th,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
16-12,th,W,W,W,W,W,W,W,W,W,W,L,L,L,L,L,L,L,L,L,L,L,L,L,L
17-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,F,F
18-12,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
19-12,th,F,U,U,U,U,U,U,U,U,F,F,F,F,F,F,F,F,F,F,F,F,F,F,U
20-12,th,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U
21-12,st,U,U,U,U,U,U,U,U,U,U,U,U,R,R,R,R,R,R,R,R,R,R,R,R
22-12,nd,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
23-12,rd,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
24-12,th,R,R,R,R,R,R,R,R,R,R,W,W,W,L,L,L,L,L,L,L,L,L,L,L
25-12,th,L,L,L,L,L,W,W,W,R,R,R,R,R,R,R,R,R,R,W,W,W,W,W,L
26-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
27-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
28-12,th,L,L,L,L,L,L,L,L,L,L,L,L,L,F,F,F,F,F,F,F,F,F,F,F
29-12,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
30-12,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,R
31-12,st,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,F"""

private const val jan2017 = """1-1,st,F,F,F,F,F,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
2-1,nd,R,R,R,R,W,W,W,W,W,W,W,W,W,W,W,U,U,U,U,U,U,U,W,W,W
3-1,rd,W,W,U,U,U,U,U,U,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
4-1,th,W,W,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
5-1,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
6-1,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,F,F,F,F,F
7-1,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
8-1,th,F,F,F,F,F,F,F,F,F,F,F,F,R,R,R,R,R,R,R,R,R,R,R,R,R
9-1,th,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,U,U,U,U,U,U
10-1,th,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,R,R,R,R,R,W,W
11-1,th,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
12-1,th,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,L,L,L,L
13-1,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
14-1,th,L,L,L,L,L,L,L,L,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
15-1,th,F,F,F,F,F,F,F,F,U,U,U,U,U,U,U,F,F,F,F,F,F,F,F,F,F
16-1,th,F,F,F,F,F,F,F,F,F,F,F,U,U,U,U,U,U,U,U,U,U,U,U,U,U
17-1,th,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U
18-1,th,U,U,U,U,U,U,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
19-1,th,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R
20-1,th,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,W,W,W,W,W,W,W
21-1,st,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
22-1,nd,W,W,W,W,W,W,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
23-1,rd,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L
24-1,th,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,F,F,F,F,F
25-1,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
26-1,th,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F,F
27-1,th,F,F,F,F,F,F,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U
28-1,th,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,U,R,R,R,R,R,R
29-1,th,R,R,R,R,R,R,R,R,R,R,W,W,W,W,W,W,W,W,W,U,U,U,U,U,U
30-1,th,U,U,U,W,W,W,W,W,W,W,U,U,U,U,W,W,W,W,W,W,W,W,W,W,W
31-1,st,W,W,W,W,W,W,W,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L,L"""