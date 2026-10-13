package org.example.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.student.entity.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "Mã sinh viên không được để trống")
    @Size(max = 50, message = "Mã sinh viên không được vượt quá 50 ký tự")
    private String studentCode;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống (MALE, FEMALE, OTHER)")
    private Gender gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(03|05|07|08|09)\\d{8}$",
            message = "Số điện thoại không hợp lệ, phải là số điện thoại Việt Nam gồm 10 chữ số bắt đầu bằng 03, 05, 07, 08 hoặc 09"
    )
    private String phoneNumber;

    @NotNull(message = "Điểm GPA không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Điểm GPA tối thiểu là 0.0")
    @DecimalMax(value = "4.0", inclusive = true, message = "Điểm GPA tối đa là 4.0")
    private Double gpa;
}
