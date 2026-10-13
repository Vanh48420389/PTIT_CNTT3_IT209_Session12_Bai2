package org.example.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.example.student.entity.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String studentCode;
    private String fullName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;
    private String phoneNumber;
    private Double gpa;
}
