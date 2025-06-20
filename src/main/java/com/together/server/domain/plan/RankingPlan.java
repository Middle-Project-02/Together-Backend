package com.together.server.domain.plan;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 요금제 랭킹 정보를 담는 엔티티입니다.
 * 실제 DB 구조: ageGroup이 1,2,3,4,5,6 숫자 코드
 *
 * @author ihyeeun
 */
@Entity
@Getter
@Table(
        name = "ranking_plans",
        indexes = {
                @Index(name = "idx_age_group_rank", columnList = "age_group, `rank`"),
                @Index(name = "idx_data_amount_gb", columnList = "data_amount_gb")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 연령대 구분 - 실제 DB 값
     * 1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상
     */
    @Column(name = "age_group", nullable = false)
    private Integer ageGroup;

    /**
     * 해당 연령대 내 랭킹 순위
     */
    @Column(name = "`rank`", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;

    @Column(name = "data_amount_gb")
    private Integer dataAmountGb;

    @Column(name = "regular_price")
    private String regularPrice;

    @Column(name = "data_amount")
    private String dataAmount;

    @Column(name = "shared_data")
    private String sharedData;

    @Column(name = "basic_benefits")
    private String basicBenefits;

    @Column(name = "smart_device")
    private String smartDevice;

    @Column(name = "speed_limit")
    private String speedLimit;

    @Column(name = "target_types")
    private String targetTypes;

    @Column(name = "all_benefits", columnDefinition = "TEXT")
    private String allBenefits;
}