package com.bright.stats.pojo.po.second;

import com.bright.stats.pojo.po.primary.Dist;
import com.bright.stats.pojo.po.primary.RoleFunction;
import com.bright.stats.pojo.po.primary.RoleMain;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.util.DataConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;


/**
 * @Author txf
 * @Date 2022-07-27 14:40:43
 * @Description
 */
@Data
@Entity
@Table(name = "usermanager")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "distno")
    private String distNo;

    @Column(name = "distname")
    private String distName;

    @Column(name = "ztId")
    private String ztId;

    @Column(name = "pmain")
    private Integer main;

    @Column(name = "module")
    private Integer module;

    @Column(name = "accSets")
    private String accSets;

    @Column(name = "phone")
    private String phone;

    @Column(name = "phoneType")
    private String phoneType;

    @Column(name = "tj_distno")
    private String tjDistNo;

    @Column(name = "tj_distname")
    private String tjDistName;

    @Column(name = "tj_ztId")
    private String tjZtId;

    @Column(name = "tj_role_rno")
    private Integer tjRoleRno;

    @Column(name = "tj_role_rname")
    private String tjRoleName;

    @Transient
    private Set<RoleMain> roleMains;

    @Transient
    private Set<RoleFunction> roleFunctions;

    @Transient
    private TableType tableType;

    @Transient
    private List<Dist> dists;

    @Transient
    private List<String> ztIds;

    @Transient
    private Integer distNoNextLength;


    public String getZtSql(String head) {
        String rvalue = "";

        head = StringUtils.isEmpty(head) ? "" : head + ".";

        if(ztIds != null) {
            for(String ztId:ztIds) {
                if(StringUtils.isNotEmpty(ztId)) {
                    rvalue += StringUtils.isNotEmpty(rvalue) ? " or " : "";
                    rvalue += "ztId='" + ztId + "'";
                }
            }
        }

        if(StringUtils.isNotEmpty(rvalue)) {
            rvalue = " and " + head + "ztName in (select ztName from zt where " + rvalue + ")";
        }

        return rvalue;
    }

    public void setDists(List<Dist> dists) {
        this.dists = dists;

        setDistNoNextLength();
    }

    public void setDistNoNextLength() {
        int index = - 1;
        if(dists.size()==0) return;

        Dist dist = dists.get(0);

        for(int i = 0; i< DataConstants.distGrades.length; i++) {
            if(DataConstants.distGrades[i] == dist.getDistId().length()) {
                index = i;
            }
        }
        index += 1;

        if(index > DataConstants.distGrades.length -1) index = DataConstants.distGrades.length -1;

        distNoNextLength = DataConstants.distGrades[index];
    }

}
