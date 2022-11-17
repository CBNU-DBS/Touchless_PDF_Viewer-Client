package com.example.client.dto;

import java.util.List;

/**
 * 회원 관련 DTO
 */
public class UserDTO {
    private Long id;

    private String name;

    private String email;

    private String password;

    private String phone;

    private List<MotionFunctionDTO> motionFunctionDTOList;

    public List<MotionFunctionDTO> getMotionFunctionList() {
        return motionFunctionDTOList;
    }

    public void setMotionFunctionList(
            List<MotionFunctionDTO> motionFunctionList) {
        this.motionFunctionDTOList = motionFunctionList;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public UserDTO() {
    }

    public UserDTO(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
}
