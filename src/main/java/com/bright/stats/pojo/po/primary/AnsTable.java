package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-03 17:42:27 
 * @Description 
 */
@Data
@Entity
@Table ( name ="Ans_table")
public class AnsTable implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "sno")
	private Integer sno;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "months")
	private Integer months;

 	@Column(name = "repno")
	private Integer repNo;

 	@Column(name = "lx")
	private String lx;

 	@Column(name = "item")
	private Integer item;

 	@Column(name = "C1")
	private String c1;

 	@Column(name = "C2")
	private String c2;

 	@Column(name = "C3")
	private String c3;

 	@Column(name = "C4")
	private String c4;

 	@Column(name = "C5")
	private String c5;

 	@Column(name = "C6")
	private String c6;

 	@Column(name = "C7")
	private String c7;

 	@Column(name = "C8")
	private String c8;

 	@Column(name = "C9")
	private String c9;

 	@Column(name = "C10")
	private String c10;

 	@Column(name = "C11")
	private String c11;

 	@Column(name = "C12")
	private String c12;

 	@Column(name = "C13")
	private String c13;

 	@Column(name = "C14")
	private String c14;

 	@Column(name = "C15")
	private String c15;

 	@Column(name = "C16")
	private String c16;

 	@Column(name = "C17")
	private String c17;

 	@Column(name = "C18")
	private String c18;

 	@Column(name = "C19")
	private String c19;

 	@Column(name = "C20")
	private String c20;

 	@Column(name = "c21")
	private String c21;

 	@Column(name = "c22")
	private String c22;

 	@Column(name = "c23")
	private String c23;

 	@Column(name = "c24")
	private String c24;

 	@Column(name = "c25")
	private String c25;

 	@Column(name = "c26")
	private String c26;

 	@Column(name = "c27")
	private String c27;

 	@Column(name = "c28")
	private String c28;

 	@Column(name = "c29")
	private String c29;

 	@Column(name = "c30")
	private String c30;

}
