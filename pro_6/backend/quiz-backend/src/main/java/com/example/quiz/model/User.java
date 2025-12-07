package com.example.quiz.model;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long id;
    private String userName;
    private String userPassword;
    private Integer isDelete;
    private Integer userRole;
    private Date createTime;
    private Date updateTime;
}

