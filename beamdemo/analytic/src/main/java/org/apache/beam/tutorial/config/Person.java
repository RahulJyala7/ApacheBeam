package org.apache.beam.tutorial.config;

import java.io.Serializable;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "beam", name = "person")
public class Person implements Serializable {  
    @Column( name = "personid" )
    private int id;
    @Column( name = "personcity" )
    private String accountId;
    @Column( name = "personname" )
    private String name;
    

    public int getId() {
        return id;
    }

    public void setId(int patientid) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(int patientid) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(int patientid) {
        this.name = name;
    }
}