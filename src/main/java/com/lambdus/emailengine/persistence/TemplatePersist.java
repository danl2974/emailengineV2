package com.lambdus.emailengine.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;


@Entity
@XmlRootElement
@Table(name = "templates", uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class TemplatePersist implements Serializable {
   
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull
    @NotEmpty
    @Column(name = "domain")
    private String domain;

    @NotNull
    @NotEmpty
    @Column(name = "creative")
    private String creative;

    @NotNull
    @NotEmpty
    @Column(name = "subjectline")
    private String subjectline;

    @NotNull
    @NotEmpty
    @Column(name = "fromaddress")
    private String fromaddress;
    
    @NotNull
    @NotEmpty
    @Column(name = "fromname")
    private String fromname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public String getSubjectline() {
        return subjectline;
    }

    public void setSubjectline(String subjectline) {
        this.subjectline = subjectline;
    }

    public String getFromaddress() {
        return fromaddress;
    }

    public void setFromaddress(String fromaddress) {
        this.fromaddress = fromaddress;
    }
    
    public String getFromname() {
        return fromname;
    }

    public void setFromname(String fromname) {
        this.fromname = fromname;
    }
    
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


}