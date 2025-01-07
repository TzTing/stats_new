package com.bright.stats.pojo.po.primary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * <p> Project: stats - LxSummary </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/10/21 9:52
 * @since 1.0.0
 */
@Data
@Entity
@Table( name ="lx_summary")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class LxSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_code")
    private String typeCode;

    @Column(name = "summary_name")
    private String summaryName;

    @Column(name = "summary_formula")
    private String summaryFormula;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "tables")
    private String tables;
}
