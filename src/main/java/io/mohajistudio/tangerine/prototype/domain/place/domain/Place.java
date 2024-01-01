package io.mohajistudio.tangerine.prototype.domain.place.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mohajistudio.tangerine.prototype.domain.post.domain.PlaceBlock;
import io.mohajistudio.tangerine.prototype.global.common.BaseEntity;
import io.mohajistudio.tangerine.prototype.global.common.PointDTO;
import io.mohajistudio.tangerine.prototype.global.enums.PlaceSearchProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

@Getter
@SuperBuilder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "place")
public class Place extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinates;
    private String thumbnail;
    private String addressProvince;//광역시/도
    private String addressCity;//시/군/구
    private String addressDistrict;//읍/면/동
    private String addressDetail;//이하
    private String roadAddress;
    @Column(length = 500)
    private String description;
    @Setter
    @Column(columnDefinition = "varchar(10)", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceSearchProvider placeSearchProvider;
    @Setter
    private Long providerId;

    @JsonIgnore
    @OneToMany(mappedBy = "place")
    private List<PlaceBlock> placeBlocks;

    public String getAddress() {
        return getAddressProvince() + " " + getAddressCity() + " " + getAddressDistrict() + " " + getAddressDetail();
    }
}