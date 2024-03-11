package com.safe4.DeviceAPI.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Device {

  @Id
  @Column(columnDefinition = "varchar(36)")
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Nonnull private String manufacturer;

  private String name;

  @Nonnull private Instant lastEditTimestamp;
}
