package org.wspcgir.strong_giraffe

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll
import org.wspcgir.strong_giraffe.model.Group
import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.set.SetSummary
import org.wspcgir.strong_giraffe.model.WeekRange
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.SetId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class Properties: DescribeSpec({
    describe("Week Ranges") {
        it("should correctly give start and end dates") {
            forAll(Arb.int(0..6)) { n ->
                val now = OffsetDateTime.of(2024,9,22 + n,15,43,3,4, ZoneOffset.of("-07:00"))
                val expectedStart = OffsetDateTime.of(2024,9,22,0,0,0,0, ZoneOffset.of("-07:00"))
                val expectedEnd = OffsetDateTime.of(2024,9,29,0,0,0,0, ZoneOffset.of("-07:00"))
                val range = WeekRange.forInstant(
                    now.toInstant(),
                    TimeZone.getTimeZone(now.toZonedDateTime().zone)
                )
                assertSoftly {
                    expectedStart.dayOfMonth shouldBe range.start.dayOfMonth
                    expectedEnd.dayOfMonth shouldBe range.end.dayOfMonth
                }
                true
            }
        }
    }
    describe("Set Summary Groups"){
        it("should form correctly") {
            val now = OffsetDateTime.now()
            val template = SetSummary(
                id = SetId("a") ,
                exerciseName = "Bicep Curl",
                exerciseId = ExerciseId("a"),
                reps = Reps(1),
                weight = Weight(20f),
                time =  now,
                intensity = Intensity.Normal,
                variationName = "",
                variationId = ExerciseVariationId("")
            )
            val sets = listOf(template, template.copy(time = now.minusNanos(100)))
            val groups = Group.fromList(sets) { it.time }
            groups.size shouldBe 1
        }
        it("should split groups properly") {
            val now = OffsetDateTime.now()
            val template = SetSummary(
                id = SetId("a") ,
                exerciseName = "Bicep Curl",
                exerciseId = ExerciseId("a"),
                reps = Reps(1),
                weight = Weight(20f),
                time = now,
                intensity = Intensity.Normal,
                variationName = "",
                variationId = ExerciseVariationId("")
            )
            val sets = listOf(
                template,
                template.copy(time = now.plusSeconds(5)),
                template.copy(
                    time = now.plusSeconds(10),
                    exerciseId = ExerciseId("b")
                ),
            )
            val groups = Group.fromList(sets) { it.time }
            groups.size shouldBe 2
            groups[0].first.exerciseId shouldBe ExerciseId("b")
            groups[0].rest.size shouldBe 0
            groups[1].first.exerciseId shouldBe ExerciseId("a")
            groups[1].rest.size shouldBe 1
        }
    }
})
