package com.example.client.dto;

public class ChangeDTO {
    private Long id;

    private String password;

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public ChangeDTO(){

    }

    public ChangeDTO(Long id, String password){
        this.id = id;
        this.password = password;
    }
}
