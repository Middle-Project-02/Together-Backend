package com.together.server.domain.plan;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "ranking_plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "rank")
    private Integer rank;

    private String name;
    private String description;

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

    @Column(name = "all_benefits")
    private String allBenefits;
}
