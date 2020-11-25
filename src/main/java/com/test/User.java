package com.test;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.couchbase.core.index.CompositeQueryIndex;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;

@ToString
@Data
@Document
@TypeAlias("user")
@CompositeQueryIndex(fields = {"id", "username", "email"})
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private List<String> roles;
    private List<Preference> preferences;
    private Address address;
    private String _class;
    private int credits;
    private List<Submission> submissions;
}

