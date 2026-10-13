package org.example.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.student.dto.PageResponse;
import org.example.student.dto.StudentRequest;
import org.example.student.dto.StudentResponse;
import org.example.student.entity.Gender;
import org.example.student.exception.ResourceNotFoundException;
import org.example.student.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @Test
    @DisplayName("POST /api/v1/students - Thành công với dữ liệu hợp lệ")
    void testCreateStudent_Success() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .gpa(3.8)
                .build();

        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .gpa(3.8)
                .build();

        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Tạo mới sinh viên thành công")))
                .andExpect(jsonPath("$.data.studentCode", is("SV001")))
                .andExpect(jsonPath("$.data.fullName", is("Tran Thi B")));
    }

    @Test
    @DisplayName("POST /api/v1/students - Thất bại khi số điện thoại không đúng chuẩn VN")
    void testCreateStudent_InvalidPhoneNumber_BadRequest() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("123456789") // Không hợp lệ
                .gpa(3.8)
                .build();

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.phoneNumber").exists());
    }

    @Test
    @DisplayName("POST /api/v1/students - Thất bại khi GPA vượt quá 4.0")
    void testCreateStudent_InvalidGpa_BadRequest() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .gpa(4.5) // Vượt quá 4.0
                .build();

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.gpa").exists());
    }

    @Test
    @DisplayName("GET /api/v1/students/{id} - Thành công")
    void testGetStudentById_Success() throws Exception {
        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .gpa(3.8)
                .build();

        when(studentService.getStudentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.studentCode", is("SV001")));
    }

    @Test
    @DisplayName("GET /api/v1/students/{id} - Không tìm thấy trả về 404")
    void testGetStudentById_NotFound() throws Exception {
        when(studentService.getStudentById(99L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy sinh viên với ID: 99"));

        mockMvc.perform(get("/api/v1/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Không tìm thấy sinh viên với ID: 99")));
    }

    @Test
    @DisplayName("DELETE /api/v1/students/{id} - Thành công")
    void testDeleteStudent_Success() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Xóa sinh viên thành công")));
    }

    @Test
    @DisplayName("GET /api/v1/students - Lấy danh sách thành công")
    void testGetAllStudents_Success() throws Exception {
        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Tran Thi B")
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .gpa(3.8)
                .build();

        PageResponse<StudentResponse> pageResponse = PageResponse.<StudentResponse>builder()
                .content(List.of(response))
                .pageNo(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(studentService.getAllStudents(null, null, null, 0, 10, "id", "asc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content[0].studentCode", is("SV001")));
    }
}
