package app.burmistrov.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/** Патент автора в блоке на главной. Скана может не быть — карточка отрисуется бланком. */
@Entity
@Table(name = "patents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patent implements Ordered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Номер и название остались от старых записей: на карточке показывается скан. */
    private String number;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(name = "scan_url")
    private String scanUrl;

    /** Порядок в ленте задаёт тренер, поэтому он хранится, а не выводится из данных. */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
