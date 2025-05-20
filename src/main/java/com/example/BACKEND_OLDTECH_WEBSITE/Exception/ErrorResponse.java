package com.example.BACKEND_OLDTECH_WEBSITE.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Date timestamp;
    private Map<String, String> details;
    private String path;
}
