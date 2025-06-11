package com.admissioncrm.authenticationservice.DTO;

import lombok.Data;

@Data
public class loginRequestViaEmail {

    String email;
    String password;
}
